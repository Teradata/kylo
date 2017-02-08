package com.thinkbiganalytics.nifi.v2.sqoop.core;

/*-
 * #%L
 * thinkbig-nifi-hadoop-processors
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;
import com.thinkbiganalytics.nifi.security.KerberosProperties;
import com.thinkbiganalytics.nifi.security.SpringSecurityContextLoader;
import com.thinkbiganalytics.nifi.v2.sqoop.PasswordMode;
import com.thinkbiganalytics.nifi.v2.sqoop.SqoopConnectionService;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.CompressionAlgorithm;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.ExtractDataFormat;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.HiveDelimStrategy;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.SqoopLoadStrategy;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.TargetHdfsDirExistsStrategy;
import com.thinkbiganalytics.nifi.v2.sqoop.process.SqoopProcessResult;
import com.thinkbiganalytics.nifi.v2.sqoop.process.SqoopProcessRunner;
import com.thinkbiganalytics.nifi.v2.sqoop.security.KerberosConfig;
import com.thinkbiganalytics.nifi.v2.sqoop.utils.SqoopBuilder;
import com.thinkbiganalytics.nifi.v2.sqoop.utils.SqoopUtils;

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

import javax.annotation.Nonnull;

/**
 * NiFi Processor to import data from a relational source into HDFS via Sqoop
 */
@Tags({"thinkbig", "ingest", "sqoop", "rdbms", "database", "table"})
@CapabilityDescription("Import data from a relational source into HDFS via Sqoop")
@WritesAttributes({
                      @WritesAttribute(attribute = "sqoop.command.text", description = "The full Sqoop command executed"),
                      @WritesAttribute(attribute = "sqoop.result.code", description = "The exit code from Sqoop command execution"),
                      @WritesAttribute(attribute = "sqoop.run.seconds", description = "Total seconds taken to run the Sqoop command"),
                      @WritesAttribute(attribute = "sqoop.record.count", description = "Count of records imported"),
                      @WritesAttribute(attribute = "sqoop.output.hdfs", description = "HDFS location where data is written"),
                  })

public class ImportSqoop extends AbstractNiFiProcessor {

    /**
     * Property to provide the connection service for executing sqoop jobs.
     */
    public static final PropertyDescriptor SQOOP_CONNECTION_SERVICE = new PropertyDescriptor.Builder()
        .name("Sqoop Connection Service")
        .description("Connection service for executing sqoop jobs.")
        .required(true)
        .identifiesControllerService(SqoopConnectionService.class)
        .build();

    /**
     * Property to provide the table to extract from the source relational system.
     */
    public static final PropertyDescriptor SOURCE_TABLE_NAME = new PropertyDescriptor.Builder()
        .name("Source Table")
        .description("The table to extract from the source relational system.")
        .required(true)
        .defaultValue("${source.table.name}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property to provide the fields in the source table
     * Field names (in order) to read from the source table. ie. the SELECT fields. Separate them using commas e.g. field1,field2,field3.
     * + Inconsistent order will cause corruption of the downstream data. Indicate * to get all columns from table.
     */
    public static final PropertyDescriptor SOURCE_TABLE_FIELDS = new PropertyDescriptor.Builder()
        .name("Source Fields")
        .description(
            "Field names (in order) to read from the source table. ie. the SELECT fields. Separate them using commas e.g. field1,field2,field3. "
            + "Inconsistent order will cause corruption of the downstream data. Indicate * to get all columns from table.")
        .required(true)
        .defaultValue("${source.table.fields}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property to provide the WHERE clause to use for the source table. This will act as a filter on source table.
     */
    public static final PropertyDescriptor SOURCE_TABLE_WHERE_CLAUSE = new PropertyDescriptor.Builder()
        .name("Source Table WHERE Clause")
        .description("The WHERE clause to use for the source table. This will act as a filter on source table.")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property to provide the load type (full table, incremental based on last modified time field, incremental based on id field).
     */
    public static final PropertyDescriptor SOURCE_LOAD_STRATEGY = new PropertyDescriptor.Builder()
        .name("Source Load Strategy")
        .description("The load type (full table, incremental based on last modified time field, incremental based on id field).")
        .required(true)
        .allowableValues(SqoopLoadStrategy.values())
        .defaultValue(SqoopLoadStrategy.FULL_LOAD.toString())
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(false)
        .build();
    /**
     * Property to provide source column name that contains the last modified time / id for incremental-type load. Only applicable for incremental-type load.
     */
    public static final PropertyDescriptor SOURCE_CHECK_COLUMN_NAME = new PropertyDescriptor.Builder()
        .name("Source Check Column Name (INCR)")
        .description("Source column name that contains the last modified time / id for incremental-type load. Only applicable for incremental-type load.")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property to provide the property containing value of current watermark. Only applicable for incremental-type load.
     */
    public static final PropertyDescriptor SOURCE_PROPERTY_WATERMARK = new PropertyDescriptor.Builder()
        .name("Watermark Property (INCR)")
        .description("Property containing value of current watermark. Only applicable for incremental-type load.")
        .required(false)
        .defaultValue("water.mark")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property to provide the source column value for the last modified time / id for tracking incremental-type load. Only applicable for
     * incremental-type load. This should generally be ${value for Watermark Property (INCR)}.
     */
    public static final PropertyDescriptor SOURCE_CHECK_COLUMN_LAST_VALUE = new PropertyDescriptor.Builder()
        .name("Source Check Column Last Value (INCR)")
        .description("Source column value for the last modified time / id for tracking incremental-type load. Only applicable for incremental-type load. "
                     + "This should generally be ${value for Watermark Property (INCR)}.")
        .required(false)
        .defaultValue("${water.mark}")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property to provide a column of the table that should be used to split work units for parallel imports.  By default,
     * the table's primary key will be used. If this column is not provided, and the table does not have a
     * primary key, one mapper will be used for the import.
     */
    public static final PropertyDescriptor SOURCE_SPLIT_BY_FIELD = new PropertyDescriptor.Builder()
        .name("Source Split By Field")
        .description("A column of the table that should be used to split work units for parallel imports. "
                     + "By default, the table's primary key will be used. "
                     + "If this column is not provided, and the table does not have a primary key, one mapper will be used for the import.")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property to provide a query to get the min and max values for split by field column. This can improve the efficiency of
     * import. The min and max values can also be explicitly provided (e.g. SELECT 1, 10). Query will be
     * literally run without modification, so please ensure that it is correct. Query should return one row
     * with exactly two columns.
     */
    public static final PropertyDescriptor SOURCE_BOUNDARY_QUERY = new PropertyDescriptor.Builder()
        .name("Source Boundary Query")
        .description("A query to get the min and max values for split by field column. This can improve the efficiency of import. "
                     + "The min and max values can also be explicitly provided (e.g. SELECT 1, 10). "
                     + "Query will be literally run without modification, so please ensure that it is correct. "
                     + "Query should return one row with exactly two columns.")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property to provide the number of map tasks to extract data in parallel.  Higher values put more load on the source
     * system.  Also, consider capacity of cluster when setting this property value.
     */
    public static final PropertyDescriptor CLUSTER_MAP_TASKS = new PropertyDescriptor.Builder()
        .name("Cluster Map Tasks")
        .description("Number of map tasks to extract data in parallel. "
                     + "Higher values put more load on the source system. "
                     + "Also, consider capacity of cluster when setting this property value.")
        .required(false)
        .expressionLanguageSupported(true)
        .defaultValue("4")
        .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
        .build();

    /**
     * Property to provide the name for the job in the UI.
     */
    public static final PropertyDescriptor CLUSTER_UI_JOB_NAME = new PropertyDescriptor.Builder()
        .name("Cluster UI Job Name")
        .description("The job will be shown in the UI via the provided name. This is to allow ease of identification.")
        .required(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    /**
     * Property to provide the target HDFS directory to land data in. The directory should
     * not pre-exist. Otherwise, job will fail. This behavior is designed to prevent accidental overwrites.
     * Also, verify that permissions on HDFS are appropriate.
     */
    public static final PropertyDescriptor TARGET_HDFS_DIRECTORY = new PropertyDescriptor.Builder()
        .name("Target HDFS Directory")
        .description("Target HDFS directory to land data in. The directory should not pre-exist. "
                     + "Otherwise, job will fail. This behavior is designed to prevent accidental overwrites. "
                     + "Also, verify that permissions on HDFS are appropriate.")
        .required(true)
        .expressionLanguageSupported(true)
        .defaultValue("/tmp")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    /**
     * Property to provide the strategy for handling the case where target HDFS directory exists. By default, the import job
     * will fail (to prevent accidental data corruption).  This behavior can be modified to delete existing
     * HDFS directory, and create it again during data import.  If delete option is chosen, and the target
     * HDFS directory does not exist, the data import will proceed and create it.
     */
    public static final PropertyDescriptor TARGET_HDFS_DIRECTORY_EXISTS_STRATEGY = new PropertyDescriptor.Builder()
        .name("Target HDFS Directory - If Exists?")
        .description("Strategy for handling the case where target HDFS directory exists. "
                     + "By default, the import job will fail (to prevent accidental data corruption). "
                     + "This behavior can be modified to delete existing HDFS directory, and create it again during data import. "
                     + "If delete option is chosen, and the target HDFS directory does not exist, the data import will proceed and create it.")
        .required(true)
        .expressionLanguageSupported(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .allowableValues(TargetHdfsDirExistsStrategy.values())
        .defaultValue(TargetHdfsDirExistsStrategy.FAIL_IMPORT.toString())
        .build();

    /**
     * Property to provide the format to land the extracted data in on HDFS.
     */
    public static final PropertyDescriptor TARGET_EXTRACT_DATA_FORMAT = new PropertyDescriptor.Builder()
        .name("Target Extract Data Format")
        .description("Format to land the extracted data in on HDFS.")
        .required(true)
        .expressionLanguageSupported(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .allowableValues(ExtractDataFormat.values())
        .defaultValue(ExtractDataFormat.TEXT.toString())
        .build();

    /**
     * Property to provide the delimiter for fields for data landing in HDFS. This is relevant
     * for TEXT and SEQUENCE_FILE target extract data formats.
     */
    public static final PropertyDescriptor TARGET_HDFS_FILE_FIELD_DELIMITER = new PropertyDescriptor.Builder()
        .name("Target HDFS File Field Delimiter")
        .description("Delimiter for fields for data landing in HDFS. This is relevant for TEXT and SEQUENCE_FILE target extract data formats.")
        .required(true)
        .expressionLanguageSupported(true)
        .defaultValue(",")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    /**
     * Property to provide the delimiter for records for data landing in HDFS. This is relevant
     * for TEXT and SEQUENCE_FILE target extract data formats.
     */
    public static final PropertyDescriptor TARGET_HDFS_FILE_RECORD_DELIMITER = new PropertyDescriptor.Builder()
        .name("Target HDFS File Record Delimiter")
        .description("Delimiter for records for data landing in HDFS. This is relevant for TEXT and SEQUENCE_FILE target extract data formats.")
        .required(true)
        .expressionLanguageSupported(true)
        .defaultValue("\\n")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    /**
     * Property to provide the strategy to handle Hive-specific delimiters (\n, \r, \01).
     * Facilitates downstream Hive ingestion.
     */
    public static final PropertyDescriptor TARGET_HIVE_DELIM_STRATEGY = new PropertyDescriptor.Builder()
        .name("Target Hive Delimiter Strategy")
        .description("Strategy to handle Hive-specific delimiters (\\n, \\r, \\01). Facilitates downstream Hive ingestion.")
        .required(true)
        .expressionLanguageSupported(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .allowableValues(HiveDelimStrategy.values())
        .defaultValue(HiveDelimStrategy.DROP.toString())
        .build();

    /**
     * Property to provide the replacement delimiter for 'REPLACE' strategy.
     * Facilitates downstream Hive ingestion.
     */
    public static final PropertyDescriptor TARGET_HIVE_REPLACE_DELIM = new PropertyDescriptor.Builder()
        .name("Target Hive Replacement Delimiter")
        .description("Replacement delimiter for 'REPLACE' strategy. Facilitates downstream Hive ingestion.")
        .required(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    /**
     * Property to provide the compression algorithm to use for data landing in HDFS.
     * For PARQUET data format, compression is not supported.
     */
    public static final PropertyDescriptor TARGET_COMPRESSION_ALGORITHM = new PropertyDescriptor.Builder()
        .name("Target Compression Algorithm")
        .description("Compression algorithm to use for data landing in HDFS. For PARQUET data format, compression is not supported.")
        .required(true)
        .expressionLanguageSupported(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .allowableValues(CompressionAlgorithm.values())
        .defaultValue(CompressionAlgorithm.NONE.toString())
        .build();

    /**
     * Property to provide the mapping to use for source columns (SQL type) to target (Java type). This
     * will override Sqoop's default mapping. Only provide mappings that you wish to override. The
     * source must contain the specified column/s. Also, this is REQUIRED for source columns that Sqoop
     * is unable to map (An example is Oracle NCLOB data type). Mappings can be entered as COLUMN=Type
     * pairs separated by comma. Ensure that there are no spaces in entry. Example: PO_ID=Integer,PO_DETAILS=String
     */
    public static final PropertyDescriptor TARGET_COLUMN_TYPE_MAPPING = new PropertyDescriptor.Builder()
        .name("Target Column Type Mapping")
        .description("Mapping to use for source columns (SQL type) to target (Java type). "
                     + "This will override Sqoop's default mapping. Only provide mappings that you wish to override. "
                     + "The source must contain the specified column/s."
                     + "Also, this is REQUIRED for source columns that Sqoop is unable to map (An example is Oracle NCLOB data type). "
                     + "Mappings can be entered as COLUMN=Type pairs separated by comma. Ensure that there are no spaces in entry. "
                     + "Example: PO_ID=Integer,PO_DETAILS=String")
        .required(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    /**
     * Property to provide code generation directory for sqoop.
     * A by-product of importing data is a Sqoop-generated Java class that can encapsulate one row of the
     * imported table. This can help with subsequent MapReduce processing of the data.  This property value
     * specifies the output directory that Sqoop should use to create these artifacts.  Please ensure that
     * the specified directory has appropriate write permissions. DEFAULT VALUE = Working directory from
     * where sqoop is launched (i.e. NiFi installation home directory)
     */
    public static final PropertyDescriptor SQOOP_CODEGEN_DIR = new PropertyDescriptor.Builder()
        .name("Sqoop Code Generation Directory")
        .description("A by-product of importing data is a Sqoop-generated Java class that can encapsulate one row of the imported table. "
                     + "This can help with subsequent MapReduce processing of the data. "
                     + "This property value specifies the output directory that Sqoop should use to create these artifacts. "
                     + "Please ensure that the specified directory has appropriate write permissions. "
                     + "DEFAULT VALUE = Working directory from where sqoop is launched (i.e. NiFi installation home directory)")
        .required(false)
        .expressionLanguageSupported(true)
        .addValidator(new StandardValidators.DirectoryExistsValidator(true, false))
        .build();

    /**
     * This property is for SQL Server only. By default, the source schema is generally
     * 'dbo'. If the source table to be ingested is in another schema, provide that value here.
     */
    public static final PropertyDescriptor SOURCESPECIFIC_SQLSERVER_SCHEMA = new PropertyDescriptor.Builder()
        .name("[SQL Server Only] Source Schema")
        .description("This property is for SQL Server only. By default, the source schema is generally 'dbo'. "
                     + "If the source table to be ingested is in another schema, provide that value here.")
        .required(false)
        .expressionLanguageSupported(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    /**
     * The success relationship (for sqoop job success)
     */
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Sqoop import success")
        .build();

    /**
     * The failure relationship (for sqoop job failure)
     */
    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("Sqoop import failure")
        .build();

    /**
     * Property for Kerberos service principal
     */
    private PropertyDescriptor KERBEROS_PRINCIPAL;

    /**
     * Property for Kerberos service keytab
     */
    private PropertyDescriptor KERBEROS_KEYTAB;

    private List<PropertyDescriptor> properties;
    private Set<Relationship> relationships;

    @Override
    protected void init(@Nonnull final ProcessorInitializationContext context) {
        super.init(context);

        /* Create Kerberos properties */
        final SpringSecurityContextLoader securityContextLoader = SpringSecurityContextLoader.create(context);
        final KerberosProperties kerberosProperties = securityContextLoader.getKerberosProperties();
        KERBEROS_KEYTAB = kerberosProperties.createKerberosKeytabProperty();
        KERBEROS_PRINCIPAL = kerberosProperties.createKerberosPrincipalProperty();

        /* Create list of properties */
        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(KERBEROS_PRINCIPAL);
        properties.add(KERBEROS_KEYTAB);
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
        properties.add(TARGET_HDFS_DIRECTORY_EXISTS_STRATEGY);
        properties.add(TARGET_EXTRACT_DATA_FORMAT);
        properties.add(TARGET_HDFS_FILE_FIELD_DELIMITER);
        properties.add(TARGET_HDFS_FILE_RECORD_DELIMITER);
        properties.add(TARGET_HIVE_DELIM_STRATEGY);
        properties.add(TARGET_HIVE_REPLACE_DELIM);
        properties.add(TARGET_COMPRESSION_ALGORITHM);
        properties.add(TARGET_COLUMN_TYPE_MAPPING);
        properties.add(SQOOP_CODEGEN_DIR);
        properties.add(SOURCESPECIFIC_SQLSERVER_SCHEMA);
        this.properties = Collections.unmodifiableList(properties);

        /* Create list of relationships */
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
            logger.info("Created a flow file having uuid: {}", new Object[]{flowFile.getAttribute(CoreAttributes.UUID.key())});
        } else {
            logger.info("Using an existing flow file having uuid: {}", new Object[]{flowFile.getAttribute(CoreAttributes.UUID.key())});
        }

        final String kerberosPrincipal = context.getProperty(KERBEROS_PRINCIPAL).getValue();
        final String kerberosKeyTab = context.getProperty(KERBEROS_KEYTAB).getValue();
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
        final TargetHdfsDirExistsStrategy targetHdfsDirExistsStrategy = TargetHdfsDirExistsStrategy.valueOf(context.getProperty(TARGET_HDFS_DIRECTORY_EXISTS_STRATEGY).getValue());
        final ExtractDataFormat targetExtractDataFormat = ExtractDataFormat.valueOf(context.getProperty(TARGET_EXTRACT_DATA_FORMAT).getValue());
        final String targetHdfsFileFieldDelimiter = context.getProperty(TARGET_HDFS_FILE_FIELD_DELIMITER).evaluateAttributeExpressions(flowFile).getValue();
        final String targetHdfsFileRecordDelimiter = context.getProperty(TARGET_HDFS_FILE_RECORD_DELIMITER).evaluateAttributeExpressions(flowFile).getValue();
        final HiveDelimStrategy targetHiveDelimStrategy = HiveDelimStrategy.valueOf(context.getProperty(TARGET_HIVE_DELIM_STRATEGY).getValue());
        final String targetHiveReplaceDelim = context.getProperty(TARGET_HIVE_REPLACE_DELIM).evaluateAttributeExpressions(flowFile).getValue();
        final CompressionAlgorithm targetCompressionAlgorithm = CompressionAlgorithm.valueOf(context.getProperty(TARGET_COMPRESSION_ALGORITHM).getValue());
        final String targetColumnTypeMapping = context.getProperty(TARGET_COLUMN_TYPE_MAPPING).evaluateAttributeExpressions(flowFile).getValue();
        final String sqoopCodeGenDirectory = context.getProperty(SQOOP_CODEGEN_DIR).evaluateAttributeExpressions(flowFile).getValue();
        final String sourceSpecificSqlServerSchema = context.getProperty(SOURCESPECIFIC_SQLSERVER_SCHEMA).evaluateAttributeExpressions(flowFile).getValue();

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
            .setSourceConnectionString(sqoopConnectionService.getConnectionString())
            .setSourceUserName(sqoopConnectionService.getUserName())
            .setPasswordMode(sqoopConnectionService.getPasswordMode())
            .setSourcePasswordHdfsFile(sqoopConnectionService.getPasswordHdfsFile())
            .setSourcePasswordPassphrase(sqoopConnectionService.getPasswordPassphrase())
            .setSourceEnteredPassword(sqoopConnectionService.getEnteredPassword())
            .setSourceConnectionManager(sqoopConnectionService.getConnectionManager())
            .setSourceDriver(sqoopConnectionService.getDriver())
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
            .setTargetHdfsDirExistsStrategy(targetHdfsDirExistsStrategy)
            .setTargetExtractDataFormat(targetExtractDataFormat)
            .setTargetHdfsFileFieldDelimiter(targetHdfsFileFieldDelimiter)
            .setTargetHdfsFileRecordDelimiter(targetHdfsFileRecordDelimiter)
            .setTargetHiveDelimStrategy(targetHiveDelimStrategy)
            .setTargetHiveReplaceDelim(targetHiveReplaceDelim)
            .setTargetCompressionAlgorithm(targetCompressionAlgorithm)
            .setTargetColumnTypeMapping(targetColumnTypeMapping)
            .setSqoopCodeGenDirectory(sqoopCodeGenDirectory)
            .setSourceSpecificSqlServerSchema(sourceSpecificSqlServerSchema)
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
        SqoopUtils sqoopUtils = new SqoopUtils();
        long recordsCount = sqoopUtils.getSqoopRecordCount(sqoopProcessResult, logger);

        String sqoopCommandWithCredentialsMasked = sqoopUtils.maskCredentials(sqoopCommand,
                                                                              sqoopUtils.getCredentialsToMask());

        flowFile = session.putAttribute(flowFile, "sqoop.command.text", sqoopCommandWithCredentialsMasked);
        flowFile = session.putAttribute(flowFile, "sqoop.result.code", String.valueOf(resultStatus));
        flowFile = session.putAttribute(flowFile, "sqoop.run.seconds", String.valueOf(jobDurationSeconds));
        flowFile = session.putAttribute(flowFile, "sqoop.record.count", String.valueOf(recordsCount));
        flowFile = session.putAttribute(flowFile, "sqoop.output.hdfs", targetHdfsDirectory);
        logger.info("Wrote result attributes to flow file");

        if (resultStatus == 0) {
            logger.info("Sqoop Import OK [Code {}]", new Object[]{resultStatus});
            if (sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_APPEND
                || sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_LASTMODIFIED) {

                if ((sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_APPEND) && (recordsCount == 0)) {
                    flowFile = session.putAttribute(flowFile, sourcePropertyWatermark, sourceCheckColumnLastValue);
                } else {

                    String newHighWaterMark = sqoopUtils.getNewHighWatermark(sqoopProcessResult);

                    if ((newHighWaterMark == null) || (newHighWaterMark.equals("NO_UPDATE")) || (newHighWaterMark.equals(""))) {
                        flowFile = session.putAttribute(flowFile, sourcePropertyWatermark, sourceCheckColumnLastValue);
                    } else {
                        flowFile = session.putAttribute(flowFile, sourcePropertyWatermark, newHighWaterMark);
                    }
                }
            }
            session.transfer(flowFile, REL_SUCCESS);
        } else {
            logger.error("Sqoop Import FAIL [Code {}]", new Object[]{resultStatus});
            session.transfer(flowFile, REL_FAILURE);
        }
    }

    /**
     * Called by the framework, this method does additional validation on properties
     *
     * @param validationContext used to retrieves the properties to check
     * @return A collection of {@link ValidationResult} which will be checked by the framework
     */
    @Override
    protected Collection<ValidationResult> customValidate(ValidationContext validationContext) {

        final List<ValidationResult> results = new ArrayList<>();
        final SqoopLoadStrategy sourceLoadStrategy = SqoopLoadStrategy.valueOf(validationContext.getProperty(SOURCE_LOAD_STRATEGY).getValue());
        final String sourceCheckColumnName = validationContext.getProperty(SOURCE_CHECK_COLUMN_NAME).evaluateAttributeExpressions().getValue();
        final String sourceCheckColumnLastValue = validationContext.getProperty(SOURCE_CHECK_COLUMN_LAST_VALUE).evaluateAttributeExpressions().getValue();
        final HiveDelimStrategy targetHiveDelimStrategy = HiveDelimStrategy.valueOf(validationContext.getProperty(TARGET_HIVE_DELIM_STRATEGY).getValue());
        final String targetHiveReplaceDelim = validationContext.getProperty(TARGET_HIVE_REPLACE_DELIM).evaluateAttributeExpressions().getValue();
        final SqoopConnectionService sqoopConnectionService = validationContext.getProperty(SQOOP_CONNECTION_SERVICE).asControllerService(SqoopConnectionService.class);
        final PasswordMode passwordMode = sqoopConnectionService.getPasswordMode();
        final ExtractDataFormat targetExtractDataFormat = ExtractDataFormat.valueOf(validationContext.getProperty(TARGET_EXTRACT_DATA_FORMAT).getValue());
        final CompressionAlgorithm targetCompressionAlgorithm = CompressionAlgorithm.valueOf(validationContext.getProperty(TARGET_COMPRESSION_ALGORITHM).getValue());
        final String targetColumnTypeMapping = validationContext.getProperty(TARGET_COLUMN_TYPE_MAPPING).evaluateAttributeExpressions().getValue();

        SqoopUtils sqoopUtils = new SqoopUtils();

        if (sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_LASTMODIFIED || sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_APPEND) {
            if ((sourceCheckColumnName == null) || (sourceCheckColumnLastValue == null)) {
                results.add(new ValidationResult.Builder()
                                .subject(this.getClass().getSimpleName())
                                .valid(false)
                                .explanation("Both 'Check Column Name' and 'Check Column Last Value' are required for incremental load.")
                                .build());

            }
        }

        if (targetHiveDelimStrategy == HiveDelimStrategy.REPLACE) {
            if (targetHiveReplaceDelim == null) {
                results.add(new ValidationResult.Builder()
                                .subject(this.getClass().getSimpleName())
                                .valid(false)
                                .explanation("Replacement delimiter must be specified for Hive Delimiter REPLACE Strategy.")
                                .build());
            }
        }

        if (passwordMode == PasswordMode.ENCRYPTED_ON_HDFS_FILE) {
            if (sqoopConnectionService.getPasswordHdfsFile() == null || sqoopConnectionService.getPasswordPassphrase() == null) {
                results.add(new ValidationResult.Builder()
                                .subject(this.getClass().getSimpleName())
                                .valid(false)
                                .explanation("For encrypted password on HDFS, both encrypted HDFS file location and passphrase are required.")
                                .build());
            }
        } else if (passwordMode == PasswordMode.ENCRYPTED_TEXT_ENTRY) {
            if (sqoopConnectionService.getEnteredPassword() == null || sqoopConnectionService.getPasswordPassphrase() == null) {
                results.add(new ValidationResult.Builder()
                                .subject(this.getClass().getSimpleName())
                                .valid(false)
                                .explanation("For encrypted password entry mode, both the encrypted password and passphrase are required.")
                                .build());
            }
        } else if (passwordMode == PasswordMode.CLEAR_TEXT_ENTRY) {
            if (sqoopConnectionService.getEnteredPassword() == null) {
                results.add(new ValidationResult.Builder()
                                .subject(this.getClass().getSimpleName())
                                .valid(false)
                                .explanation("For clear text password entry mode, the password must be provided.")
                                .build());
            }
        }

        if ((targetExtractDataFormat == ExtractDataFormat.PARQUET) && (targetCompressionAlgorithm != CompressionAlgorithm.NONE)) {
            results.add(new ValidationResult.Builder()
                            .subject(this.getClass().getSimpleName())
                            .valid(false)
                            .explanation("For PARQUET data format, compression is not supported.")
                            .build());

        }

        if ((sqoopUtils.isTeradataDatabase(sqoopConnectionService.getConnectionString()) && (sourceLoadStrategy == SqoopLoadStrategy.INCREMENTAL_LASTMODIFIED))) {
            results.add(new ValidationResult.Builder()
                            .subject(this.getClass().getSimpleName())
                            .valid(false)
                            .explanation("For Teradata source system, INCREMENTAL_LASTMODIFIED mode of load is not supported. This is due to a known issue with Sqoop (SQOOP-2402).")
                            .build());
        }

        if ((targetColumnTypeMapping != null) && (!targetColumnTypeMapping.isEmpty())) {
            if (!sqoopUtils.checkMappingInput(targetColumnTypeMapping)) {
                results.add(new ValidationResult.Builder()
                                .subject(this.getClass().getSimpleName())
                                .valid(false)
                                .explanation("For Target Column Type Mapping, ensure that mappings are provided as COLUMN=Type pairs separated by comma. "
                                             + "Ensure no spaces in entry. "
                                             + "Example: PO_ID=Integer,PO_DETAILS=String")
                                .build());

            }
        }

        return results;
    }
}
