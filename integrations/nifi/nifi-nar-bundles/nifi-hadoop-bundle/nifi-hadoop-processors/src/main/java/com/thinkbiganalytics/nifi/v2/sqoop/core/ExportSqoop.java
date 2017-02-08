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
import com.thinkbiganalytics.nifi.v2.sqoop.SqoopConnectionService;
import com.thinkbiganalytics.nifi.v2.sqoop.enums.ExportNullInterpretationStrategy;
import com.thinkbiganalytics.nifi.v2.sqoop.process.SqoopExportProcessRunner;
import com.thinkbiganalytics.nifi.v2.sqoop.process.SqoopProcessResult;
import com.thinkbiganalytics.nifi.v2.sqoop.security.KerberosConfig;
import com.thinkbiganalytics.nifi.v2.sqoop.utils.SqoopExportBuilder;
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
 * NiFi Processor to export data from HDFS to a relational system via Sqoop
 */
@Tags({"thinkbig", "export", "sqoop", "rdbms", "database", "table"})
@CapabilityDescription("Export data from HDFS to a relational system via Sqoop")
@WritesAttributes({
                      @WritesAttribute(attribute = "sqoop.export.command.text", description = "The full Sqoop export command executed"),
                      @WritesAttribute(attribute = "sqoop.export.result.code", description = "The exit code from Sqoop export command execution"),
                      @WritesAttribute(attribute = "sqoop.export.run.seconds", description = "Total seconds taken to run the Sqoop export command"),
                      @WritesAttribute(attribute = "sqoop.export.record.count", description = "Count of records exported"),
                      @WritesAttribute(attribute = "sqoop.export.output.table", description = "Table name where data is written"),
                  })

public class ExportSqoop extends AbstractNiFiProcessor {

    /**
     * Property to provide connection service for executing sqoop jobs.
     */
    public static final PropertyDescriptor SQOOP_CONNECTION_SERVICE = new PropertyDescriptor.Builder()
        .name("Sqoop Connection Service")
        .description("Connection service for executing sqoop jobs.")
        .required(true)
        .identifiesControllerService(SqoopConnectionService.class)
        .build();

    /**
     * Property to provide source HDFS directory to get the data from for export.
     */
    public static final PropertyDescriptor SOURCE_HDFS_DIRECTORY = new PropertyDescriptor.Builder()
        .name("Source HDFS Directory")
        .description("Source HDFS directory to get the data from for export.")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property to provide delimiter for source data on HDFS.
     */
    public static final PropertyDescriptor SOURCE_HDFS_FILE_DELIMITER = new PropertyDescriptor.Builder()
        .name("Source HDFS File Delimiter")
        .description("Delimiter for source data on HDFS.")
        .required(true)
        .expressionLanguageSupported(true)
        .defaultValue(",")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    /**
     * Property to provide method for identifying nulls in source HDFS data.
     */
    public static final PropertyDescriptor SOURCE_NULL_INTERPRETATION_STRATEGY = new PropertyDescriptor.Builder()
        .name("Source Null Interpret Strategy")
        .description("Method for identifying nulls in source HDFS data. "
                     + "For SQOOP_DEFAULT [{String column values: null in HDFS data -> null in relational system} {Non-string column values: null or empty_string in HDFS data -> null in relational system}]. "
                     + "For HIVE_DEFAULT [{String column values: \\N in HDFS data -> null in relational system} {Non-string column values: \\N or empty_string in HDFS data -> null in relational system}]. "
                     + "For CUSTOM_VALUES: Custom-provided identifiers to identify null values in string and non-string columns in HDFS data.")
        .required(true)
        .allowableValues(ExportNullInterpretationStrategy.values())
        .defaultValue(ExportNullInterpretationStrategy.HIVE_DEFAULT.toString())
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(false)
        .build();

    /**
     * Property to provide custom string for identifying null strings in HDFS data.
     */
    public static final PropertyDescriptor SOURCE_NULL_CUSTOM_STRING_IDENTIFIER = new PropertyDescriptor.Builder()
        .name("Source Null String Identifier")
        .description("Custom string for identifying null strings in HDFS data.")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property to provide custom string for identifying null non-strings in HDFS data.
     */
    public static final PropertyDescriptor SOURCE_NULL_CUSTOM_NON_STRING_IDENTIFIER = new PropertyDescriptor.Builder()
        .name("Source Null Non-String Identifier")
        .description("Custom string for identifying null non-strings in HDFS data.")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property to provide the table to populate in the target relational system. NOTE: This table must already exist.
     */
    public static final PropertyDescriptor TARGET_TABLE_NAME = new PropertyDescriptor.Builder()
        .name("Target Table")
        .description("The table to populate in the target relational system. NOTE: This table must already exist.")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    /**
     * Property to provide number of map tasks to export data in parallel.
     */
    public static final PropertyDescriptor CLUSTER_MAP_TASKS = new PropertyDescriptor.Builder()
        .name("Cluster Map Tasks")
        .description("Number of map tasks to export data in parallel. Valid values are from 1 to 25. "
                     + "Higher values put more load on the target relational system. "
                     + "Also, consider capacity of cluster when setting this property value.")
        .required(false)
        .expressionLanguageSupported(true)
        .defaultValue("4")
        .addValidator(StandardValidators.createLongValidator(1L, 25L, true))
        .build();

    /**
     * Success relationship
     */
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Sqoop export success")
        .build();

    /**
     * Failure relationship
     */
    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("Sqoop export failure")
        .build();

    /*
     * Property for Kerberos service principal
     */
    private PropertyDescriptor KERBEROS_PRINCIPAL;

    /*
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
        properties.add(SOURCE_HDFS_DIRECTORY);
        properties.add(SOURCE_HDFS_FILE_DELIMITER);
        properties.add(SOURCE_NULL_INTERPRETATION_STRATEGY);
        properties.add(SOURCE_NULL_CUSTOM_STRING_IDENTIFIER);
        properties.add(SOURCE_NULL_CUSTOM_NON_STRING_IDENTIFIER);
        properties.add(TARGET_TABLE_NAME);
        properties.add(CLUSTER_MAP_TASKS);

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
        final String sourceHdfsDirectory = context.getProperty(SOURCE_HDFS_DIRECTORY).evaluateAttributeExpressions(flowFile).getValue();
        final String sourceHdfsFileDelimiter = context.getProperty(SOURCE_HDFS_FILE_DELIMITER).evaluateAttributeExpressions(flowFile).getValue();
        final ExportNullInterpretationStrategy sourceNullInterpretationStrategy = ExportNullInterpretationStrategy.valueOf(context.getProperty(SOURCE_NULL_INTERPRETATION_STRATEGY).getValue());
        final String sourceNullCustomStringIdentifier = context.getProperty(SOURCE_NULL_CUSTOM_STRING_IDENTIFIER).evaluateAttributeExpressions(flowFile).getValue();
        final String sourceNullCustomNonStringIdentifier = context.getProperty(SOURCE_NULL_CUSTOM_NON_STRING_IDENTIFIER).evaluateAttributeExpressions(flowFile).getValue();
        final String targetTableName = context.getProperty(TARGET_TABLE_NAME).evaluateAttributeExpressions(flowFile).getValue();
        final Integer clusterMapTasks = context.getProperty(CLUSTER_MAP_TASKS).evaluateAttributeExpressions(flowFile).asInteger();

        final String COMMAND_SHELL = "/bin/bash";
        final String COMMAND_SHELL_FLAGS = "-c";

        final StopWatch stopWatch = new StopWatch(false);

        KerberosConfig kerberosConfig = new KerberosConfig()
            .setLogger(logger)
            .setKerberosPrincipal(kerberosPrincipal)
            .setKerberosKeytab(kerberosKeyTab);

        SqoopExportBuilder sqoopExportBuilder = new SqoopExportBuilder();
        String sqoopExportCommand = sqoopExportBuilder
            .setLogger(logger)
            .setTargetConnectionString(sqoopConnectionService.getConnectionString())
            .setTargetUserName(sqoopConnectionService.getUserName())
            .setPasswordMode(sqoopConnectionService.getPasswordMode())
            .setTargetPasswordHdfsFile(sqoopConnectionService.getPasswordHdfsFile())
            .setTargetPasswordPassphrase(sqoopConnectionService.getPasswordPassphrase())
            .setTargetEnteredPassword(sqoopConnectionService.getEnteredPassword())
            .setTargetConnectionManager(sqoopConnectionService.getConnectionManager())
            .setTargetDriver(sqoopConnectionService.getDriver())
            .setTargetTableName(targetTableName)
            .setSourceHdfsDirectory(sourceHdfsDirectory)
            .setSourceHdfsFileDelimiter(sourceHdfsFileDelimiter)
            .setSourceNullInterpretationStrategy(sourceNullInterpretationStrategy)
            .setSourceNullInterpretationStrategyCustomNullString(sourceNullCustomStringIdentifier)
            .setSourceNullInterpretationStrategyCustomNullNonString(sourceNullCustomNonStringIdentifier)
            .setClusterMapTasks(clusterMapTasks)
            .build();

        List<String> sqoopExportExecutionCommand = new ArrayList<>();
        sqoopExportExecutionCommand.add(COMMAND_SHELL);
        sqoopExportExecutionCommand.add(COMMAND_SHELL_FLAGS);
        sqoopExportExecutionCommand.add(sqoopExportCommand);

        SqoopExportProcessRunner sqoopExportProcessRunner = new SqoopExportProcessRunner(kerberosConfig,
                                                                                         sqoopExportExecutionCommand,
                                                                                         logger);

        logger.info("Starting execution of Sqoop export command");
        stopWatch.start();
        SqoopProcessResult sqoopExportProcessResult = sqoopExportProcessRunner.execute();
        long jobDurationSeconds = stopWatch.getElapsed(TimeUnit.SECONDS);
        stopWatch.stop();
        logger.info("Finished execution of Sqoop export command");

        int resultExportStatus = sqoopExportProcessResult.getExitValue();
        SqoopUtils sqoopUtils = new SqoopUtils();
        long recordsExportCount = sqoopUtils.getSqoopExportRecordCount(sqoopExportProcessResult, logger);

        String sqoopExportCommandWithCredentialsMasked = sqoopUtils.maskCredentials(sqoopExportCommand,
                                                                                    sqoopUtils.getCredentialsToMask());

        flowFile = session.putAttribute(flowFile, "sqoop.export.command.text", sqoopExportCommandWithCredentialsMasked);
        flowFile = session.putAttribute(flowFile, "sqoop.export.result.code", String.valueOf(resultExportStatus));
        flowFile = session.putAttribute(flowFile, "sqoop.export.run.seconds", String.valueOf(jobDurationSeconds));
        flowFile = session.putAttribute(flowFile, "sqoop.export.record.count", String.valueOf(recordsExportCount));
        flowFile = session.putAttribute(flowFile, "sqoop.export.output.table", targetTableName);
        logger.info("Wrote result attributes to flow file");

        if (resultExportStatus == 0) {
            logger.info("Sqoop Export OK [Code {}]", new Object[]{resultExportStatus});
            session.transfer(flowFile, REL_SUCCESS);
        } else {
            logger.info("Sqoop Export FAIL [Code {}]", new Object[]{resultExportStatus});
            session.transfer(flowFile, REL_FAILURE);
        }
    }

    /**
     * Called by the framework this method does additional validation on properties
     *
     * @param validationContext used to retrieves the properties to check
     * @return A collection of {@link ValidationResult} which will be checked by the framework
     */
    @Override
    protected Collection<ValidationResult> customValidate(ValidationContext validationContext) {
        final List<ValidationResult> results = new ArrayList<>();
        final ExportNullInterpretationStrategy sourceNullInterpretationStrategy =
            ExportNullInterpretationStrategy.valueOf(validationContext.getProperty(SOURCE_NULL_INTERPRETATION_STRATEGY).getValue());
        final String sourceNullCustomStringIdentifier = validationContext.getProperty(SOURCE_NULL_CUSTOM_STRING_IDENTIFIER).evaluateAttributeExpressions().getValue();
        final String sourceNullCustomNonStringIdentifier = validationContext.getProperty(SOURCE_NULL_CUSTOM_NON_STRING_IDENTIFIER).evaluateAttributeExpressions().getValue();

        if (sourceNullInterpretationStrategy == ExportNullInterpretationStrategy.CUSTOM_VALUES) {
            if ((sourceNullCustomStringIdentifier == null) || (sourceNullCustomNonStringIdentifier == null)) {
                results.add(new ValidationResult.Builder()
                                .subject(this.getClass().getSimpleName())
                                .valid(false)
                                .explanation("For Custom Source Null Interpret Strategy, custom strings for identifying null strings and null non-strings in HDFS data must be provided.")
                                .build());
            }
        }

        return results;
    }
}
