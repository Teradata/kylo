package com.thinkbiganalytics.nifi.v2.sqoop.core;

/**
 * @author jagrut sharma
 */

import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;
import com.thinkbiganalytics.nifi.security.KerberosProperties;
import com.thinkbiganalytics.nifi.security.SpringSecurityContextLoader;
import com.thinkbiganalytics.nifi.v2.sqoop.SqoopConnectionService;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.CompressionAlgorithm;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.HiveDelimStrategy;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.SqoopLoadStrategy;
import com.thinkbiganalytics.nifi.v2.sqoop.process.SqoopProcessResult;
import com.thinkbiganalytics.nifi.v2.sqoop.process.SqoopProcessRunner;
import com.thinkbiganalytics.nifi.v2.sqoop.security.KerberosConfig;

import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Tags({"thinkbig", "ingest", "sqoop", "rdbms", "database", "table"})
@CapabilityDescription("Import data from a relational source into HDFS via Sqoop")
@WritesAttributes({
                      @WritesAttribute(attribute = "sqoop.command.text", description = "The full Sqoop command executed"),
                      @WritesAttribute(attribute = "sqoop.result.code", description = "The exit code from Sqoop command execution"),
                      @WritesAttribute(attribute = "sqoop.run.seconds", description =  "Total seconds taken to run the Sqoop command"),
                      @WritesAttribute(attribute = "sqoop.record.count", description = "Count of records imported"),
                      @WritesAttribute(attribute = "sqoop.output.hdfs", description = "HDFS location where data is written"),
                      @WritesAttribute(attribute = "sqoop.file.delimiter", description = "File delimiter for imported data")
                  })

/*
 * A processor to run a Sqoop import job to fetch data from a relational system into HDFS
 */
public class ImportSqoop extends AbstractNiFiProcessor {

    public static final PropertyDescriptor FEED_CATEGORY = new PropertyDescriptor.Builder()
        .name("System Feed Category")
        .description("System category that this feed belongs to")
        .required(true)
        .defaultValue("${metadata.category.systemName}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor FEED_NAME = new PropertyDescriptor.Builder()
        .name("System Feed Name")
        .description("System name of feed")
        .defaultValue("${metadata.systemFeedName}")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor SQOOP_CONNECTION_SERVICE = new PropertyDescriptor.Builder()
        .name("Sqoop Connection Service")
        .description("Connection service for executing sqoop jobs")
        .required(true)
        .identifiesControllerService(SqoopConnectionService.class)
        .build();

    public static final PropertyDescriptor SOURCE_TABLE_NAME = new PropertyDescriptor.Builder()
        .name("Source Table")
        .description("The table to extract from the source relational system")
        .required(true)
        .defaultValue("${source.table.name}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor SOURCE_TABLE_FIELDS = new PropertyDescriptor.Builder()
        .name("Source Fields")
        .description(
            "Field names (in order) to read from the source table. ie. the SELECT fields. Separate them using commas e.g. field1,field2,field3 "
            + "Inconsistent order will cause corruption of the downstream data. Indicate * to get all columns from table.")
        .required(true)
        .defaultValue("${source.table.fields}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor SOURCE_TABLE_WHERE_CLAUSE = new PropertyDescriptor.Builder()
        .name("Source Table WHERE Clause")
        .description("The WHERE clause to use for the source table. This will act as a filter on source table.")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor SOURCE_LOAD_STRATEGY = new PropertyDescriptor.Builder()
        .name("Source Load Strategy")
        .description("The extraction type (full table, incremental based on last modified time field, incremental based on id field)")
        .required(true)
        .allowableValues(SqoopLoadStrategy.values())
        .defaultValue(SqoopLoadStrategy.FULL_LOAD.toString())
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(false)
        .build();

    public static final PropertyDescriptor SOURCE_CHECK_COLUMN_NAME = new PropertyDescriptor.Builder()
        .name("Source Check Column Name (For Incremental Load)")
        .description("Source column name that contains the last modified time / id for incremental-type load. Not needed for full type of load.")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor SOURCE_PROPERTY_WATERMARK = new PropertyDescriptor.Builder()
        .name("Watermark Property (For Incremental Load)")
        .required(false)
        .defaultValue("water.mark")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor SOURCE_CHECK_COLUMN_LAST_VALUE = new PropertyDescriptor.Builder()
        .name("Source Check Column Last Value (For Incremental Load)")
        .description("Source column value for the last modified time / id for tracking incremental-type load. Not needed for full type of load.")
        .required(false)
        .defaultValue("${water.mark}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor SOURCE_SPLIT_BY_FIELD = new PropertyDescriptor.Builder()
        .name("Source Split By Field")
        .description("A field that has unique values in the source table. "
                     + "By default, the table's primary key will be used. "
                     + "This is used to perform parallel imports.")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor SOURCE_BOUNDARY_QUERY = new PropertyDescriptor.Builder()
        .name("Source Boundary Query")
        .description("A query to get the min and max values for split by field column.\n"
                     + "The min and max values can also be explicitly provided (e.g. SELECT 1, 10).\n"
                     + "Query will be literally run without modification, so please ensure that it is correct.\n"
                     + "Query should return one row with exactly two columns")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor CLUSTER_MAP_TASKS = new PropertyDescriptor.Builder()
        .name("Cluster Map Tasks")
        .description("Number of map tasks to extract data in parallel. Valid values are from 1 to 15. Higher values put more load on the source system.")
        .required(false)
        .expressionLanguageSupported(true)
        .defaultValue("4")
        .addValidator(StandardValidators.createLongValidator(1L, 15L, true))
        .build();

    public static final PropertyDescriptor CLUSTER_UI_JOB_NAME = new PropertyDescriptor.Builder()
        .name("Cluster UI Job Name")
        .description("The job will be shown in the UI via the provided name. This is to allow ease of identification.")
        .required(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor TARGET_HDFS_DIRECTORY = new PropertyDescriptor.Builder()
        .name("Target HDFS Directory")
        .description("Target HDFS directory to land data in. The directory should not pre-exist. "
                     + "Otherwise, job will fail. This behavior is designed to allow accidental overwrites. "
                     + "Also, the directory must have write permissions for user running nifi.")
        .required(true)
        .expressionLanguageSupported(true)
        .defaultValue("/tmp")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor TARGET_HDFS_FILE_DELIMITER = new PropertyDescriptor.Builder()
        .name("Target HDFS File Delimiter")
        .description("Delimiter for data landing in HDFS.")
        .required(true)
        .expressionLanguageSupported(true)
        .defaultValue(",")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor TARGET_HIVE_DELIM_STRATEGY = new PropertyDescriptor.Builder()
        .name("Target Hive Delimiter Strategy")
        .description("Strategy to handle Hive-specific delimiters. Facilitates downstream Hive ingestion.")
        .required(true)
        .expressionLanguageSupported(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .allowableValues(HiveDelimStrategy.values())
        .defaultValue(HiveDelimStrategy.DROP.toString())
        .build();

    public static final PropertyDescriptor TARGET_HIVE_REPLACE_DELIM = new PropertyDescriptor.Builder()
        .name("Target Hive Replacement Delimiter")
        .description("Replacement delimiter for 'REPLACE' strategy. Facilitates downstream Hive ingestion.")
        .required(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    public static final PropertyDescriptor TARGET_COMPRESSION_ALGORITHM = new PropertyDescriptor.Builder()
        .name("Target Compression Algorithm")
        .description("Compression algorithm to use for data landing in HDFS")
        .required(true)
        .expressionLanguageSupported(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .allowableValues(CompressionAlgorithm.values())
        .defaultValue(CompressionAlgorithm.NONE.toString())
        .build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Sqoop import success")
        .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("Sqoop import failure")
        .build();

    /** Property for Kerberos service keytab */
    private PropertyDescriptor kerberosKeytab;

    /** Property for Kerberos service principal */
    private PropertyDescriptor kerberosPrincipal;

    private List<PropertyDescriptor> properties;
    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        super.init(context);

        // Create Kerberos properties
        final SpringSecurityContextLoader securityContextLoader = SpringSecurityContextLoader.create(context);
        final KerberosProperties kerberosProperties = securityContextLoader.getKerberosProperties();
        kerberosKeytab = kerberosProperties.createKerberosKeytabProperty();
        kerberosPrincipal = kerberosProperties.createKerberosPrincipalProperty();

        // Create list of properties
        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(kerberosPrincipal);
        properties.add(kerberosKeytab);
        properties.add(FEED_CATEGORY);
        properties.add(FEED_NAME);
        properties.add(SQOOP_CONNECTION_SERVICE);
        properties.add(SOURCE_TABLE_NAME);
        properties.add(SOURCE_TABLE_FIELDS);
        properties.add(SOURCE_TABLE_WHERE_CLAUSE);
        properties.add(SOURCE_LOAD_STRATEGY);
        properties.add(SOURCE_CHECK_COLUMN_NAME);
        properties.add(SOURCE_CHECK_COLUMN_LAST_VALUE);
        properties.add(SOURCE_PROPERTY_WATERMARK);
        properties.add(SOURCE_SPLIT_BY_FIELD);
        properties.add(SOURCE_BOUNDARY_QUERY);
        properties.add(CLUSTER_MAP_TASKS);
        properties.add(CLUSTER_UI_JOB_NAME);
        properties.add(TARGET_HDFS_DIRECTORY);
        properties.add(TARGET_HDFS_FILE_DELIMITER);
        properties.add(TARGET_HIVE_DELIM_STRATEGY);
        properties.add(TARGET_HIVE_REPLACE_DELIM);
        properties.add(TARGET_COMPRESSION_ALGORITHM);
        this.properties = Collections.unmodifiableList(properties);

        // Create list of relationships
        final Set<Relationship> relationships = new HashSet<>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return properties;
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        final ComponentLog logger = getLog();
        FlowFile flowFile = session.get();

        if (flowFile == null) {
            flowFile = session.create();
            logger.info("Created a flow file having uuid: {}", new Object[] { flowFile.getAttribute(CoreAttributes.UUID.key()) } );
        }
        else {
            logger.info("Using an existing flow file having uuid: {}", new Object[] { flowFile.getAttribute(CoreAttributes.UUID.key()) } );
        }

        final String kerberosPrincipal = context.getProperty(this.kerberosPrincipal).getValue();
        final String kerberosKeyTab = context.getProperty(kerberosKeytab).getValue();
        final String feedCategory = context.getProperty(FEED_CATEGORY).evaluateAttributeExpressions(flowFile).getValue();
        final String feedName = context.getProperty(FEED_NAME).evaluateAttributeExpressions(flowFile).getValue();
        final SqoopConnectionService sqoopConnectionService = context.getProperty(SQOOP_CONNECTION_SERVICE).asControllerService(SqoopConnectionService.class);
        final String sourceTableName = context.getProperty(SOURCE_TABLE_NAME).evaluateAttributeExpressions(flowFile).getValue();
        final String sourceTableFields = context.getProperty(SOURCE_TABLE_FIELDS).evaluateAttributeExpressions(flowFile).getValue();
        final String sourceTableWhereClause = context.getProperty(SOURCE_TABLE_WHERE_CLAUSE).evaluateAttributeExpressions(flowFile).getValue();
        final SqoopLoadStrategy sourceLoadStrategy = SqoopLoadStrategy.valueOf(context.getProperty(SOURCE_LOAD_STRATEGY).getValue());
        final String sourceCheckColumnName = context.getProperty(SOURCE_CHECK_COLUMN_NAME).evaluateAttributeExpressions(flowFile).getValue();
        final String sourcePropertyWatermark = context.getProperty(SOURCE_PROPERTY_WATERMARK).evaluateAttributeExpressions(flowFile).getValue();
        final String sourceCheckColumnLastValue = context.getProperty(SOURCE_CHECK_COLUMN_LAST_VALUE).evaluateAttributeExpressions(flowFile).getValue();
        final String sourceSplitByField = context.getProperty(SOURCE_SPLIT_BY_FIELD).evaluateAttributeExpressions(flowFile).getValue();
        final String sourceBoundaryQuery = context.getProperty(SOURCE_BOUNDARY_QUERY).evaluateAttributeExpressions(flowFile).getValue();
        final Integer clusterMapTasks = context.getProperty(CLUSTER_MAP_TASKS).evaluateAttributeExpressions(flowFile).asInteger();
        final String clusterUIJobName = context.getProperty(CLUSTER_UI_JOB_NAME).evaluateAttributeExpressions(flowFile).getValue();
        final String targetHdfsDirectory = context.getProperty(TARGET_HDFS_DIRECTORY).evaluateAttributeExpressions(flowFile).getValue();
        final String targetHdfsFileDelimiter = context.getProperty(TARGET_HDFS_FILE_DELIMITER).evaluateAttributeExpressions(flowFile).getValue();
        final HiveDelimStrategy targetHiveDelimStrategy = HiveDelimStrategy.valueOf(context.getProperty(TARGET_HIVE_DELIM_STRATEGY).getValue());
        final String targetHiveReplaceDelim = context.getProperty(TARGET_HIVE_REPLACE_DELIM).evaluateAttributeExpressions(flowFile).getValue();
        final CompressionAlgorithm targetCompressionAlgorithm = CompressionAlgorithm.valueOf(context.getProperty(TARGET_COMPRESSION_ALGORITHM).getValue());

        final String COMMAND_SHELL = "/bin/bash";
        final String COMMAND_SHELL_FLAGS = "-c";

        final StopWatch stopWatch = new StopWatch(false);

        KerberosConfig kerberosConfig = new KerberosConfig()
            .setLogger(logger)
            .setKerberosPrincipal(kerberosPrincipal)
            .setKerberosKeytab(kerberosKeyTab);

        SqoopBuilder sqoopBuilder = new SqoopBuilder();
        String sqoopCommand = sqoopBuilder
            .setLogger(logger)
            .setFeedCategory(feedCategory)
            .setFeedName(feedName)
            .setSourceDriver(sqoopConnectionService.getDriver())
            .setSourceConnectionString(sqoopConnectionService.getConnectionString())
            .setSourceUserName(sqoopConnectionService.getUserName())
            .setSourcePasswordHdfsFile(sqoopConnectionService.getPasswordHdfsFile())
            .setSourcePasswordPassphrase(sqoopConnectionService.getPasswordPassphrase())
            .setSourceTableName(sourceTableName)
            .setSourceTableFields(sourceTableFields)
            .setSourceTableWhereClause(sourceTableWhereClause)
            .setSourceLoadStrategy(sourceLoadStrategy)
            .setSourceCheckColumnName(sourceCheckColumnName)
            .setSourceCheckColumnLastValue(sourceCheckColumnLastValue)
            .setSourceSplitByField(sourceSplitByField)
            .setSourceBoundaryQuery(sourceBoundaryQuery)
            .setClusterMapTasks(clusterMapTasks)
            .setClusterUIJobName(clusterUIJobName)
            .setTargetHdfsDirectory(targetHdfsDirectory)
            .setTargetHdfsFileDelimiter(targetHdfsFileDelimiter)
            .setTargetHiveDelimStrategy(targetHiveDelimStrategy)
            .setTargetHiveReplaceDelim(targetHiveReplaceDelim)
            .setTargetCompressionAlgorithm(targetCompressionAlgorithm)
            .build();

        List<String> sqoopExecutionCommand = new ArrayList<>();
        sqoopExecutionCommand.add(COMMAND_SHELL);
        sqoopExecutionCommand.add(COMMAND_SHELL_FLAGS);
        sqoopExecutionCommand.add(sqoopCommand);

        SqoopProcessRunner sqoopProcessRunner = new SqoopProcessRunner(kerberosConfig,
                                                                       sqoopExecutionCommand,
                                                                       logger,
                                                                       sourceLoadStrategy);
        logger.info("Starting execution of Sqoop command");
        stopWatch.start();
        SqoopProcessResult sqoopProcessResult = sqoopProcessRunner.execute();
        long jobDurationSeconds = stopWatch.getElapsed(TimeUnit.SECONDS);
        stopWatch.stop();
        logger.info("Finished execution of Sqoop command");

        int resultStatus = sqoopProcessResult.getExitValue();
        int recordsCount = getSqoopRecordCount(sqoopProcessResult);

        flowFile = session.putAttribute(flowFile, "sqoop.command.text", sqoopCommand);
        flowFile = session.putAttribute(flowFile, "sqoop.result.code", String.valueOf(resultStatus));
        flowFile = session.putAttribute(flowFile, "sqoop.run.seconds", String.valueOf(jobDurationSeconds));
        flowFile = session.putAttribute(flowFile, "sqoop.record.count", String.valueOf(recordsCount));
        flowFile = session.putAttribute(flowFile, "sqoop.output.hdfs", targetHdfsDirectory);
        flowFile = session.putAttribute(flowFile, "sqoop.file.delimiter", targetHdfsFileDelimiter);
        logger.info("Wrote result attributes to flow file");

        if (resultStatus == 0) {
            logger.info("Sqoop Import OK [Code {}]", new Object[] { resultStatus });
            if (sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_APPEND
                || sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_LASTMODIFIED) {

                if ((sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_APPEND) &&  (recordsCount == 0)) {
                    flowFile = session.putAttribute(flowFile, sourcePropertyWatermark, sourceCheckColumnLastValue);
                }
                else {

                    String newHighWaterMark = getNewHighWatermark(sqoopProcessResult);

                    if ((newHighWaterMark == null) || (newHighWaterMark.equals("NO_UPDATE")) || (newHighWaterMark.equals(""))) {
                        flowFile = session.putAttribute(flowFile, sourcePropertyWatermark, sourceCheckColumnLastValue);
                    }
                    else {
                        flowFile = session.putAttribute(flowFile, sourcePropertyWatermark, newHighWaterMark);
                    }
                }
            }
            session.transfer(flowFile, REL_SUCCESS);
        }
        else {
            logger.info("Sqoop Import FAIL [Code {}]", new Object[] { resultStatus });
            session.transfer(flowFile, REL_FAILURE);
        }
    }

    @Override
    protected Collection<ValidationResult> customValidate(ValidationContext validationContext) {

        final List<ValidationResult> results = new ArrayList<>();
        final SqoopLoadStrategy sourceLoadStrategy = SqoopLoadStrategy.valueOf(validationContext.getProperty(SOURCE_LOAD_STRATEGY).getValue());
        final String sourceCheckColumnName = validationContext.getProperty(SOURCE_CHECK_COLUMN_NAME).getValue();
        final String sourceCheckColumnLastValue = validationContext.getProperty(SOURCE_CHECK_COLUMN_LAST_VALUE).getValue();
        final HiveDelimStrategy targetHiveDelimStrategy = HiveDelimStrategy.valueOf(validationContext.getProperty(TARGET_HIVE_DELIM_STRATEGY).getValue());
        final String targetHiveReplaceDelim = validationContext.getProperty(TARGET_HIVE_REPLACE_DELIM).getValue();

        if (sourceLoadStrategy != SqoopLoadStrategy.FULL_LOAD) {
            if ((sourceCheckColumnName == null) || (sourceCheckColumnLastValue == null)) {
                results.add(new ValidationResult.Builder()
                                .subject(this.getClass().getSimpleName())
                                .valid(false)
                                .explanation("Both 'Check Column Name' and 'Check Column Last Value' are required for incremental load")
                                .build());

            }
        }

        if (targetHiveDelimStrategy == HiveDelimStrategy.REPLACE) {
            if (targetHiveReplaceDelim == null) {
                results.add(new ValidationResult.Builder()
                            .subject(this.getClass().getSimpleName())
                            .valid(false)
                            .explanation("Replacement delimiter must be specified for Hive Delimiter REPLACE Strategy")
                            .build());
            }
        }
        return results;
    }

    private int getSqoopRecordCount(SqoopProcessResult sqoopProcessResult) {
        String[] logLines = sqoopProcessResult.getLogLines();

        if ((sqoopProcessResult.getExitValue() != 0) || (logLines[0] == null)) {
            getLog().error("Skipping attempt to retrieve number of records extracted");
            return -1;
        }

        //Example of longLines[0]:
        //16/10/12 21:50:03 INFO mapreduce.ImportJobBase: Retrieved 2 records.
        //16/10/21 02:05:41 INFO tool.ImportTool: No new rows detected since last import.
        String recordCountLogLine = logLines[0];
        if (recordCountLogLine.contains("No new rows detected since last import")) {
            return 0;
        }
        int start = recordCountLogLine.indexOf("Retrieved");
        int end = recordCountLogLine.indexOf("records.");
        String numberString = recordCountLogLine.substring(start+9, end).trim();
        try {
            return Integer.valueOf(numberString);
        }
        catch (Exception e) {
            getLog().error("Unable to parse number of records extracted. " + e.getMessage());
            return -1;
        }
    }

    private String getNewHighWatermark(SqoopProcessResult sqoopProcessResult) {
        String[] logLines = sqoopProcessResult.getLogLines();

        final String NO_UPDATE = "NO_UPDATE";

        if ((sqoopProcessResult.getExitValue() != 0) || (logLines.length <= 1)) {
            return NO_UPDATE;
        }
        else if ((logLines[0] != null) && (logLines[0].contains("No new rows detected since last import"))) {
            return NO_UPDATE;
        }
        else {
            if (logLines[1] == null) return NO_UPDATE;
            //16/10/18 23:37:11 INFO tool.ImportTool:   --last-value 1006
            String newHighWaterMarkLogLine = logLines[1];
            int end = newHighWaterMarkLogLine.length();
            int start = newHighWaterMarkLogLine.indexOf("--last-value");
            return newHighWaterMarkLogLine.substring(start + 12, end).trim();
        }
    }
}
