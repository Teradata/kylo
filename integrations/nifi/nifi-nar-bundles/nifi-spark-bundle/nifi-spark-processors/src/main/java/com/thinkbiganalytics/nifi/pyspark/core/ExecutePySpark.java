package com.thinkbiganalytics.nifi.pyspark.core;

/*-
 * #%L
 * thinkbig-nifi-spark-processors
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
import com.thinkbiganalytics.nifi.pyspark.utils.PySparkUtils;
import com.thinkbiganalytics.nifi.security.ApplySecurityPolicy;
import com.thinkbiganalytics.nifi.security.KerberosProperties;
import com.thinkbiganalytics.nifi.security.SecurityUtil;
import com.thinkbiganalytics.nifi.security.SpringSecurityContextLoader;
import com.thinkbiganalytics.nifi.util.InputStreamReaderRunnable;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.logging.LogLevel;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.spark.launcher.SparkLauncher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A NiFi processor to execute a PySpark job
 */
@EventDriven
@Tags({"spark", "thinkbig", "pyspark"})
@CapabilityDescription("Execute a PySpark job.")
public class ExecutePySpark extends AbstractNiFiProcessor {

    /* Processor properties */
    public static final PropertyDescriptor HADOOP_CONFIGURATION_RESOURCES = new PropertyDescriptor.Builder()
        .name("Hadoop Configuration Resources")
        .description("A file or comma separated list of files which contains the Hadoop file system configuration. Without this, Hadoop "
                     + "will search the classpath for a 'core-site.xml' and 'hdfs-site.xml' file or will revert to a default configuration. "
                     + "NOTE: This value is also required for a Kerberized cluster.")
        .required(false)
        .addValidator(multipleFilesExistValidator())
        .build();
    public static final PropertyDescriptor PYSPARK_APP_FILE = new PropertyDescriptor.Builder()
        .name("PySpark App File")
        .description("Full path for PySpark application file (having Python code to be executed)")
        .required(true)
        .addValidator(new StandardValidators.FileExistsValidator(true))
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor PYSPARK_APP_ARGS = new PropertyDescriptor.Builder()
        .name("PySpark App Args")
        .description("Comma separated arguments to be passed to the PySpark application. "
                     + "NOTE: Ensure that no spaces are present between the comma separated arguments.")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor PYSPARK_APP_NAME = new PropertyDescriptor.Builder()
        .name("PySpark App Name")
        .description("A name for the PySpark application")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .defaultValue("PySpark-App")
        .build();
    public static final PropertyDescriptor PYSPARK_ADDITIONAL_FILES = new PropertyDescriptor.Builder()
        .name("Additional Python files/zips/eggs")
        .description("(Comma separated) Full path for additional Python files/zips/eggs to be submitted with the application. "
                     + "NOTE: Ensure that no spaces are present between the comma separated file locations.")
        .required(false)
        .addValidator(multipleFilesExistValidator())
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor SPARK_MASTER = new PropertyDescriptor.Builder()
        .name("Spark Master")
        .description("The Spark master. NOTE: Please ensure that you have not set this in your application.")
        .required(true)
        .defaultValue("local")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor SPARK_YARN_DEPLOY_MODE = new PropertyDescriptor.Builder()
        .name("Spark YARN Deploy Mode")
        .description("The deploy mode for YARN master (client, cluster). Only applicable for yarn mode. "
                     + "NOTE: Please ensure that you have not set this in your application.")
        .required(false)
        .defaultValue("client")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor YARN_QUEUE = new PropertyDescriptor.Builder()
        .name("YARN Queue")
        .description("The name of the YARN queue to which the job is submitted. Only applicable for yarn mode.")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor SPARK_HOME = new PropertyDescriptor.Builder()
        .name("Spark Home")
        .description("Spark installation location")
        .required(true)
        .defaultValue("/usr/hdp/current/spark-client/")
        .addValidator(new StandardValidators.DirectoryExistsValidator(true, false))
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor DRIVER_MEMORY = new PropertyDescriptor.Builder()
        .name("Driver Memory")
        .description("Amount of memory (RAM) to allocate to the driver (e.g. 512m, 2g).  Consider cluster capacity when setting value.")
        .required(true)
        .defaultValue("512m")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor EXECUTOR_MEMORY = new PropertyDescriptor.Builder()
        .name("Executor Memory")
        .description("Amount of memory (RAM) to allocate to an executor (e.g. 512m, 2g).  Consider cluster capacity when setting value.")
        .required(true)
        .defaultValue("512m")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor EXECUTOR_INSTANCES = new PropertyDescriptor.Builder()
        .name("Executor Instances")
        .description("The number of executors to use for job execution. Consider cluster capacity when setting value.")
        .required(true)
        .defaultValue("1")
        .addValidator(StandardValidators.createLongValidator(1L, 1000L, true))
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor EXECUTOR_CORES = new PropertyDescriptor.Builder()
        .name("Executor Cores")
        .description("The number of CPU cores to be used on each executor. Consider cluster capacity when setting value.")
        .required(true)
        .defaultValue("1")
        .addValidator(StandardValidators.createLongValidator(1L, 100L, true))
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor NETWORK_TIMEOUT = new PropertyDescriptor.Builder()
        .name("Network Timeout")
        .description("Default timeout for all network interactions. "
                     + "This config will be used in place of spark.core.connection.ack.wait.timeout, "
                     + "spark.akka.timeout, spark.storage.blockManagerSlaveTimeoutMs, "
                     + "spark.shuffle.io.connectionTimeout, spark.rpc.askTimeout "
                     + "or spark.rpc.lookupTimeout if they are not configured.")
        .required(true)
        .defaultValue("120s")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor ADDITIONAL_SPARK_CONFIG_OPTIONS = new PropertyDescriptor.Builder()
        .name("Additional Spark Configuration")
        .description("Additional configuration options to pass to the Spark job. "
                     + "These would be key=value pairs separated by comma. "
                     + "Note that the configuration option would start with 'spark.' "
                     + "e.g. spark.ui.port=4040 "
                     + "NOTE: Ensure that no spaces are present between the comma-separated key=value pairs.")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    /* Processor relationships */
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("PySpark job execution success")
        .build();
    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("PySpark job execution failure")
        .build();
    /* Spark configuration */
    private static final String CONFIG_PROP_SPARK_YARN_KEYTAB = "spark.yarn.keytab";
    private static final String CONFIG_PROP_SPARK_YARN_PRINCIPAL = "spark.yarn.principal";
    private static final String CONFIG_PROP_SPARK_NETWORK_TIMEOUT = "spark.network.timeout";
    private static final String CONFIG_PROP_SPARK_YARN_QUEUE = "spark.yarn.queue";
    private static final String CONFIG_PROP_SPARK_EXECUTOR_INSTANCES = "spark.executor.instances";
    /* Properties for Kerberos service keytab and principal */
    private PropertyDescriptor KERBEROS_KEYTAB;
    private PropertyDescriptor KERBEROS_PRINCIPAL;

    private List<PropertyDescriptor> properties;
    private Set<Relationship> relationships;

    /* Validates that one or more files exist, as specified in a single property (comma-separated values) */
    public static Validator multipleFilesExistValidator() {
        return new Validator() {
            @Override
            public ValidationResult validate(String subject, String input, ValidationContext context) {
                try {
                    final String[] files = input.split(",");

                    for (String filename : files) {
                        try {
                            final File file = new File(filename.trim());
                            if (!file.exists()) {
                                final String message = "file " + filename + " does not exist.";
                                return new ValidationResult.Builder()
                                    .subject(this.getClass().getSimpleName())
                                    .input(input)
                                    .valid(false)
                                    .explanation(message)
                                    .build();
                            } else if (!file.isFile()) {
                                final String message = filename + " is not a file.";
                                return new ValidationResult.Builder()
                                    .subject(this.getClass().getSimpleName())
                                    .input(input)
                                    .valid(false)
                                    .explanation(message)
                                    .build();
                            } else if (!file.canRead()) {
                                final String message = "could not read " + filename;
                                return new ValidationResult.Builder()
                                    .subject(this.getClass().getSimpleName())
                                    .input(input)
                                    .valid(false)
                                    .explanation(message)
                                    .build();
                            }
                        } catch (SecurityException e) {
                            final String message = "unable to access " + filename + " due to " + e.getMessage();
                            return new ValidationResult.Builder()
                                .subject(this.getClass().getSimpleName())
                                .input(input)
                                .valid(false)
                                .explanation(message)
                                .build();
                        }
                    }
                } catch (Exception e) {
                    return new ValidationResult.Builder()
                        .subject(this.getClass().getSimpleName())
                        .input(input)
                        .valid(false)
                        .explanation("error evaluating value. Please sure that value is provided as file1,file2,file3 and so on. "
                                     + "Also, the files should exist and be readable.")
                        .build();
                }

                return new ValidationResult.Builder()
                    .subject(this.getClass().getSimpleName())
                    .input(input)
                    .valid(true)
                    .build();
            }
        };
    }

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
        properties.add(HADOOP_CONFIGURATION_RESOURCES);
        properties.add(PYSPARK_APP_FILE);
        properties.add(PYSPARK_APP_ARGS);
        properties.add(PYSPARK_APP_NAME);
        properties.add(PYSPARK_ADDITIONAL_FILES);
        properties.add(SPARK_MASTER);
        properties.add(SPARK_YARN_DEPLOY_MODE);
        properties.add(YARN_QUEUE);
        properties.add(SPARK_HOME);
        properties.add(DRIVER_MEMORY);
        properties.add(EXECUTOR_MEMORY);
        properties.add(EXECUTOR_INSTANCES);
        properties.add(EXECUTOR_CORES);
        properties.add(NETWORK_TIMEOUT);
        properties.add(ADDITIONAL_SPARK_CONFIG_OPTIONS);
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
        try {
            final String kerberosPrincipal = context.getProperty(KERBEROS_PRINCIPAL).getValue();
            final String kerberosKeyTab = context.getProperty(KERBEROS_KEYTAB).getValue();
            final String hadoopConfigurationResources = context.getProperty(HADOOP_CONFIGURATION_RESOURCES).getValue();
            final String pySparkAppFile = context.getProperty(PYSPARK_APP_FILE).evaluateAttributeExpressions(flowFile).getValue();
            final String pySparkAppArgs = context.getProperty(PYSPARK_APP_ARGS).evaluateAttributeExpressions(flowFile).getValue();
            final String pySparkAppName = context.getProperty(PYSPARK_APP_NAME).evaluateAttributeExpressions(flowFile).getValue();
            final String pySparkAdditionalFiles = context.getProperty(PYSPARK_ADDITIONAL_FILES).evaluateAttributeExpressions(flowFile).getValue();
            final String sparkMaster = context.getProperty(SPARK_MASTER).evaluateAttributeExpressions(flowFile).getValue().trim().toLowerCase();
            final String sparkYarnDeployMode = context.getProperty(SPARK_YARN_DEPLOY_MODE).evaluateAttributeExpressions(flowFile).getValue();
            final String yarnQueue = context.getProperty(YARN_QUEUE).evaluateAttributeExpressions(flowFile).getValue();
            final String sparkHome = context.getProperty(SPARK_HOME).evaluateAttributeExpressions(flowFile).getValue();
            final String driverMemory = context.getProperty(DRIVER_MEMORY).evaluateAttributeExpressions(flowFile).getValue();
            final String executorMemory = context.getProperty(EXECUTOR_MEMORY).evaluateAttributeExpressions(flowFile).getValue();
            final String executorInstances = context.getProperty(EXECUTOR_INSTANCES).evaluateAttributeExpressions(flowFile).getValue();
            final String executorCores = context.getProperty(EXECUTOR_CORES).evaluateAttributeExpressions(flowFile).getValue();
            final String networkTimeout = context.getProperty(NETWORK_TIMEOUT).evaluateAttributeExpressions(flowFile).getValue();
            final String additionalSparkConfigOptions = context.getProperty(ADDITIONAL_SPARK_CONFIG_OPTIONS).evaluateAttributeExpressions(flowFile).getValue();

            PySparkUtils pySparkUtils = new PySparkUtils();

            /* Get app arguments */
            String[] pySparkAppArgsArray = null;
            if (!StringUtils.isEmpty(pySparkAppArgs)) {
                pySparkAppArgsArray = pySparkUtils.getCsvValuesAsArray(pySparkAppArgs);
                logger.info("Provided application arguments: {}", new Object[]{pySparkUtils.getCsvStringFromArray(pySparkAppArgsArray)});
            }

            /* Get additional python files */
            String[] pySparkAdditionalFilesArray = null;
            if (!StringUtils.isEmpty(pySparkAdditionalFiles)) {
                pySparkAdditionalFilesArray = pySparkUtils.getCsvValuesAsArray(pySparkAdditionalFiles);
                logger.info("Provided python files: {}", new Object[]{pySparkUtils.getCsvStringFromArray(pySparkAdditionalFilesArray)});
            }

            /* Get additional config key-value pairs */
            String[] additionalSparkConfigOptionsArray = null;
            if (!StringUtils.isEmpty(additionalSparkConfigOptions)) {
                additionalSparkConfigOptionsArray = pySparkUtils.getCsvValuesAsArray(additionalSparkConfigOptions);
                logger.info("Provided spark config options: {}", new Object[]{pySparkUtils.getCsvStringFromArray(additionalSparkConfigOptionsArray)});
            }

            /* Determine if Kerberos is enabled */
            boolean kerberosEnabled = false;
            if (!StringUtils.isEmpty(kerberosPrincipal) && !StringUtils.isEmpty(kerberosKeyTab) && !StringUtils.isEmpty(hadoopConfigurationResources)) {
                kerberosEnabled = true;
                logger.info("Kerberos is enabled");
            }

            /* For Kerberized cluster, attempt user authentication */
            if (kerberosEnabled) {
                logger.info("Attempting user authentication for Kerberos");
                ApplySecurityPolicy applySecurityObject = new ApplySecurityPolicy();
                Configuration configuration;
                try {
                    logger.info("Getting Hadoop configuration from " + hadoopConfigurationResources);
                    configuration = ApplySecurityPolicy.getConfigurationFromResources(hadoopConfigurationResources);

                    if (SecurityUtil.isSecurityEnabled(configuration)) {
                        logger.info("Security is enabled");

                        if (kerberosPrincipal.equals("") && kerberosKeyTab.equals("")) {
                            logger.error("Kerberos Principal and Keytab provided with empty values for a Kerberized cluster.");
                            session.transfer(flowFile, REL_FAILURE);
                            return;
                        }

                        try {
                            logger.info("User authentication initiated");

                            boolean authenticationStatus = applySecurityObject.validateUserWithKerberos(logger, hadoopConfigurationResources, kerberosPrincipal, kerberosKeyTab);
                            if (authenticationStatus) {
                                logger.info("User authenticated successfully.");
                            } else {
                                logger.error("User authentication failed.");
                                session.transfer(flowFile, REL_FAILURE);
                                return;
                            }

                        } catch (Exception unknownException) {
                            logger.error("Unknown exception occurred while validating user :" + unknownException.getMessage());
                            session.transfer(flowFile, REL_FAILURE);
                            return;
                        }
                    }
                } catch (IOException e1) {
                    logger.error("Unknown exception occurred while authenticating user :" + e1.getMessage());
                    session.transfer(flowFile, REL_FAILURE);
                    return;
                }
            }

            /* Build and launch PySpark Job */
            logger.info("Configuring PySpark job for execution");
            SparkLauncher pySparkLauncher = new SparkLauncher()
                .setAppResource(pySparkAppFile);
            logger.info("PySpark app file set to: {}", new Object[]{pySparkAppFile});

            if (pySparkAppArgsArray != null && pySparkAppArgsArray.length > 0) {
                pySparkLauncher = pySparkLauncher
                    .addAppArgs(pySparkAppArgsArray);
                logger.info("App arguments set to: {}", new Object[]{pySparkUtils.getCsvStringFromArray(pySparkAppArgsArray)});
            }

            pySparkLauncher = pySparkLauncher
                .setAppName(pySparkAppName)
                .setMaster(sparkMaster);

            logger.info("App name set to: {}", new Object[]{pySparkAppName});
            logger.info("Spark master set to: {}", new Object[]{sparkMaster});

            if (pySparkAdditionalFilesArray != null && pySparkAdditionalFilesArray.length > 0) {
                for (String pySparkAdditionalFile : pySparkAdditionalFilesArray) {
                    pySparkLauncher = pySparkLauncher
                        .addPyFile(pySparkAdditionalFile);
                    logger.info("Additional python file set to: {}", new Object[]{pySparkAdditionalFile});
                }
            }

            if (sparkMaster.equals("yarn")) {
                pySparkLauncher = pySparkLauncher
                    .setDeployMode(sparkYarnDeployMode);
                logger.info("YARN deploy mode set to: {}", new Object[]{sparkYarnDeployMode});
            }

            pySparkLauncher = pySparkLauncher
                .setSparkHome(sparkHome)
                .setConf(SparkLauncher.DRIVER_MEMORY, driverMemory)
                .setConf(SparkLauncher.EXECUTOR_MEMORY, executorMemory)
                .setConf(CONFIG_PROP_SPARK_EXECUTOR_INSTANCES, executorInstances)
                .setConf(SparkLauncher.EXECUTOR_CORES, executorCores)
                .setConf(CONFIG_PROP_SPARK_NETWORK_TIMEOUT, networkTimeout);

            logger.info("Spark home set to: {} ", new Object[]{sparkHome});
            logger.info("Driver memory set to: {} ", new Object[]{driverMemory});
            logger.info("Executor memory set to: {} ", new Object[]{executorMemory});
            logger.info("Executor instances set to: {} ", new Object[]{executorInstances});
            logger.info("Executor cores set to: {} ", new Object[]{executorCores});
            logger.info("Network timeout set to: {} ", new Object[]{networkTimeout});

            if (kerberosEnabled) {
                pySparkLauncher = pySparkLauncher
                    .setConf(CONFIG_PROP_SPARK_YARN_PRINCIPAL, kerberosPrincipal);
                pySparkLauncher = pySparkLauncher
                    .setConf(CONFIG_PROP_SPARK_YARN_KEYTAB, kerberosKeyTab);
                logger.info("Kerberos principal set to: {} ", new Object[]{kerberosPrincipal});
                logger.info("Kerberos keytab set to: {} ", new Object[]{kerberosKeyTab});
            }

            if (!StringUtils.isEmpty(yarnQueue)) {
                pySparkLauncher = pySparkLauncher
                    .setConf(CONFIG_PROP_SPARK_YARN_QUEUE, yarnQueue);
                logger.info("YARN queue set to: {} ", new Object[]{yarnQueue});
            }

            if (additionalSparkConfigOptionsArray != null && additionalSparkConfigOptionsArray.length > 0) {
                for (String additionalSparkConfigOption : additionalSparkConfigOptionsArray) {
                    String[] confKeyValue = additionalSparkConfigOption.split("=");
                    if (confKeyValue.length == 2) {
                        pySparkLauncher = pySparkLauncher
                            .setConf(confKeyValue[0], confKeyValue[1]);
                        logger.info("Spark additional config option set to: {}={}", new Object[]{confKeyValue[0], confKeyValue[1]});
                    }
                }
            }

            logger.info("Starting execution of PySpark job");
            Process pySparkProcess = pySparkLauncher.launch();

            InputStreamReaderRunnable inputStreamReaderRunnable = new InputStreamReaderRunnable(LogLevel.INFO, logger, pySparkProcess.getInputStream());
            Thread inputThread = new Thread(inputStreamReaderRunnable, "stream input");
            inputThread.start();

            InputStreamReaderRunnable errorStreamReaderRunnable = new InputStreamReaderRunnable(LogLevel.INFO, logger, pySparkProcess.getErrorStream());
            Thread errorThread = new Thread(errorStreamReaderRunnable, "stream error");
            errorThread.start();

            logger.info("Waiting for PySpark job to complete");

            int exitCode = pySparkProcess.waitFor();
            if (exitCode != 0) {
                logger.info("Finished execution of PySpark job [FAILURE] [Status code: {}]", new Object[]{exitCode});
                session.transfer(flowFile, REL_FAILURE);
            } else {
                logger.info("Finished execution of PySpark job [SUCCESS] [Status code: {}]", new Object[]{exitCode});
                session.transfer(flowFile, REL_SUCCESS);
            }
        } catch (final Exception e) {
            logger.error("Unable to execute PySpark job [FAILURE]", new Object[]{flowFile, e});
            session.transfer(flowFile, REL_FAILURE);
        }
    }

    @Override
    protected Collection<ValidationResult> customValidate(ValidationContext validationContext) {
        final List<ValidationResult> results = new ArrayList<>();
        final String sparkMaster = validationContext.getProperty(SPARK_MASTER).evaluateAttributeExpressions().getValue().trim().toLowerCase();
        final String sparkYarnDeployMode = validationContext.getProperty(SPARK_YARN_DEPLOY_MODE).evaluateAttributeExpressions().getValue();
        final String pySparkAppArgs = validationContext.getProperty(PYSPARK_APP_ARGS).evaluateAttributeExpressions().getValue();
        final String additionalSparkConfigOptions = validationContext.getProperty(ADDITIONAL_SPARK_CONFIG_OPTIONS).evaluateAttributeExpressions().getValue();

        PySparkUtils pySparkUtils = new PySparkUtils();

        if ((!sparkMaster.contains("local")) && (!sparkMaster.equals("yarn")) && (!sparkMaster.contains("mesos")) && (!sparkMaster.contains("spark"))) {
            results.add(new ValidationResult.Builder()
                            .subject(this.getClass().getSimpleName())
                            .valid(false)
                            .explanation("invalid spark master provided. Valid values will have local, local[n], local[*], yarn, mesos, spark")
                            .build());

        }

        if (sparkMaster.equals("yarn")) {
            if (!(sparkYarnDeployMode.equals("client") || sparkYarnDeployMode.equals("cluster"))) {
                results.add(new ValidationResult.Builder()
                                .subject(this.getClass().getSimpleName())
                                .valid(false)
                                .explanation("yarn master requires a deploy mode to be specified as either 'client' or 'cluster'")
                                .build());
            }
        }

        if (!StringUtils.isEmpty(pySparkAppArgs)) {
            if (!pySparkUtils.validateCsvArgs(pySparkAppArgs)) {
                results.add(new ValidationResult.Builder()
                                .subject(this.getClass().getSimpleName())
                                .valid(false)
                                .explanation("app args in invalid format. They should be provided as arg1,arg2,arg3 and so on.")
                                .build());

            }
        }

        if (!StringUtils.isEmpty(additionalSparkConfigOptions)) {
            if (!pySparkUtils.validateKeyValueArgs(additionalSparkConfigOptions)) {
                results.add(new ValidationResult.Builder()
                                .subject(this.getClass().getSimpleName())
                                .valid(false)
                                .explanation("additional spark config options in invalid format. They should be provided as config1=value1,config2=value2 and so on.")
                                .build());

            }
        }

        return results;
    }
}
