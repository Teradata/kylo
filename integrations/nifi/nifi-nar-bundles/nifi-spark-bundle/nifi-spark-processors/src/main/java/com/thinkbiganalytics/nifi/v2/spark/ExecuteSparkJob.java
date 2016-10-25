/*
 * Copyright (c) 2015. Teradata Inc.
 */

package com.thinkbiganalytics.nifi.v2.spark;

import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;
import com.thinkbiganalytics.nifi.security.ApplySecurityPolicy;
import com.thinkbiganalytics.nifi.security.KerberosProperties;
import com.thinkbiganalytics.nifi.security.SecurityUtil;
import com.thinkbiganalytics.nifi.security.SpringSecurityContextLoader;
import com.thinkbiganalytics.nifi.util.InputStreamReaderRunnable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.logging.LogLevel;
import org.apache.nifi.processor.AbstractProcessor;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"spark", "thinkbig"})
@CapabilityDescription("Execute a Spark job. "
)
public class ExecuteSparkJob extends AbstractNiFiProcessor {

    public static final String SPARK_NETWORK_TIMEOUT_CONFIG_NAME = "spark.network.timeout";
    public static final String SPARK_YARN_KEYTAB = "spark.yarn.keytab";
    public static final String SPARK_YARN_PRINCIPAL = "spark.yarn.principal";
    public static final String SPARK_YARN_QUEUE = "spark.yarn.queue";

    // Relationships
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Successful result.")
        .build();
    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("Spark execution failed. Incoming FlowFile will be penalized and routed to this relationship")
        .build();
    private final Set<Relationship> relationships;

    public static final PropertyDescriptor APPLICATION_JAR = new PropertyDescriptor.Builder()
        .name("ApplicationJAR")
        .description("Path to the JAR file containing the Spark job application")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor EXTRA_JARS = new PropertyDescriptor.Builder()
        .name("Extra JARs")
        .description("A file or a list of files separated by comma which should be added to the class path")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor YARN_QUEUE = new PropertyDescriptor.Builder()
        .name("Yarn Queue")
        .description("Optional Yarn Queue")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor MAIN_CLASS = new PropertyDescriptor.Builder()
        .name("MainClass")
        .description("Qualified classname of the Spark job application class")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor MAIN_ARGS = new PropertyDescriptor.Builder()
        .name("MainArgs")
        .description("Comma separated arguments to be passed into the main as args")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor SPARK_HOME = new PropertyDescriptor.Builder()
        .name("SparkHome")
        .description("Qualified classname of the Spark job application class")
        .required(true)
        .defaultValue("/usr/hdp/current/spark-client/")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();

    public static final PropertyDescriptor SPARK_MASTER = new PropertyDescriptor.Builder()
        .name("SparkMaster")
        .description("The Spark master")
        .required(true)
        .defaultValue("local")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();


    public static final PropertyDescriptor QUERY_TIMEOUT = new PropertyDescriptor.Builder()
        .name("Max Wait Time")
        .description("The maximum amount of time allowed for a running SQL select query "
                     + " , zero means there is no limit. Max time less than 1 second will be equal to zero.")
        .defaultValue("0 seconds")
        .required(true)
        .addValidator(StandardValidators.TIME_PERIOD_VALIDATOR)
        .sensitive(false)
        .build();

    public static final PropertyDescriptor DRIVER_MEMORY = new PropertyDescriptor.Builder()
        .name("Driver Memory")
        .description("How much RAM to allocate to the driver")
        .required(true)
        .defaultValue("512m")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor EXECUTOR_MEMORY = new PropertyDescriptor.Builder()
        .name("Executor Memory")
        .description("How much RAM to allocate to the executor")
        .required(true)
        .defaultValue("512m")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor NUMBER_EXECUTORS = new PropertyDescriptor.Builder()
        .name("Number of Executors")
        .description("The number of exectors to be used")
        .required(true)
        .defaultValue("1")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor EXECUTOR_CORES = new PropertyDescriptor.Builder()
        .name("Executor Cores")
        .description("The number of executor cores to be used")
        .required(true)
        .defaultValue("1")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor SPARK_APPLICATION_NAME = new PropertyDescriptor.Builder()
        .name("Spark Application Name")
        .description("The name of the spark application")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor NETWORK_TIMEOUT = new PropertyDescriptor.Builder()
        .name("Network Timeout")
        .description(
            "Default timeout for all network interactions. This config will be used in place of spark.core.connection.ack.wait.timeout, spark.akka.timeout, spark.storage.blockManagerSlaveTimeoutMs, spark.shuffle.io.connectionTimeout, spark.rpc.askTimeout or spark.rpc.lookupTimeout if they are not configured.")
        .required(true)
        .defaultValue("120s")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor HADOOP_CONFIGURATION_RESOURCES = new PropertyDescriptor.Builder()
        .name("Hadoop Configuration Resources")
        .description("A file or comma separated list of files which contains the Hadoop file system configuration. Without this, Hadoop "
                     + "will search the classpath for a 'core-site.xml' and 'hdfs-site.xml' file or will revert to a default configuration.")
        .required(false).addValidator(createMultipleFilesExistValidator())
        .build();

    /** Kerberos service keytab */
    private PropertyDescriptor kerberosKeyTab;

    /** Kerberos service principal */
    private PropertyDescriptor kerberosPrincipal;

    /** List of properties */
    private List<PropertyDescriptor> propDescriptors;

    public ExecuteSparkJob() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);
    }

    @Override
    protected void init(@Nonnull final ProcessorInitializationContext context) {
        super.init(context);

        // Create Kerberos properties
        final SpringSecurityContextLoader securityContextLoader = SpringSecurityContextLoader.create(context);
        final KerberosProperties kerberosProperties = securityContextLoader.getKerberosProperties();
        kerberosKeyTab = kerberosProperties.createKerberosKeytabProperty();
        kerberosPrincipal = kerberosProperties.createKerberosPrincipalProperty();

        // Create list of properties
        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(APPLICATION_JAR);
        pds.add(EXTRA_JARS);
        pds.add(MAIN_CLASS);
        pds.add(MAIN_ARGS);
        pds.add(SPARK_MASTER);
        pds.add(SPARK_HOME);
        pds.add(QUERY_TIMEOUT);
        pds.add(DRIVER_MEMORY);
        pds.add(EXECUTOR_MEMORY);
        pds.add(NUMBER_EXECUTORS);
        pds.add(SPARK_APPLICATION_NAME);
        pds.add(EXECUTOR_CORES);
        pds.add(NETWORK_TIMEOUT);
        pds.add(HADOOP_CONFIGURATION_RESOURCES);
        pds.add(kerberosPrincipal);
        pds.add(kerberosKeyTab);
        pds.add(YARN_QUEUE);
        propDescriptors = Collections.unmodifiableList(pds);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return propDescriptors;
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        final ComponentLog logger = getLog();
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }
        try {
              /* Configuration parameters for spark launcher */
            String appJar = context.getProperty(APPLICATION_JAR).evaluateAttributeExpressions(flowFile).getValue().trim();
            String extraJars = context.getProperty(EXTRA_JARS).evaluateAttributeExpressions(flowFile).getValue();
            String yarnQueue = context.getProperty(YARN_QUEUE).evaluateAttributeExpressions(flowFile).getValue();
            String mainClass = context.getProperty(MAIN_CLASS).evaluateAttributeExpressions(flowFile).getValue().trim();
            String sparkMaster = context.getProperty(SPARK_MASTER).evaluateAttributeExpressions(flowFile).getValue().trim();
            String appArgs = context.getProperty(MAIN_ARGS).evaluateAttributeExpressions(flowFile).getValue().trim();
            String driverMemory = context.getProperty(DRIVER_MEMORY).evaluateAttributeExpressions(flowFile).getValue();
            String executorMemory = context.getProperty(EXECUTOR_MEMORY).evaluateAttributeExpressions(flowFile).getValue();
            String numberOfExecutors = context.getProperty(NUMBER_EXECUTORS).evaluateAttributeExpressions(flowFile).getValue();
            String sparkApplicationName = context.getProperty(SPARK_APPLICATION_NAME).evaluateAttributeExpressions(flowFile).getValue();
            String executorCores = context.getProperty(EXECUTOR_CORES).evaluateAttributeExpressions(flowFile).getValue();
            String networkTimeout = context.getProperty(NETWORK_TIMEOUT).evaluateAttributeExpressions(flowFile).getValue();
            String principal = context.getProperty(kerberosPrincipal).getValue();
            String keyTab = context.getProperty(kerberosKeyTab).getValue();
            String hadoopConfigurationResources = context.getProperty(HADOOP_CONFIGURATION_RESOURCES).getValue();
            String[] args = null;
            if (!StringUtils.isEmpty(appArgs)) {
                args = appArgs.split(",");
            }

            String[] extraJarPaths = null;
            if (!StringUtils.isEmpty(extraJars)) {
                extraJarPaths = extraJars.split(",");
            } else {
                getLog().info("No extra jars to be added to class path");
            }

            // If all 3 fields are filled out then assume kerberos is enabled and we want to authenticate the user
            boolean authenticateUser = false;
            if (!(StringUtils.isEmpty(principal) && StringUtils.isEmpty(keyTab) && StringUtils.isEmpty(hadoopConfigurationResources))) {
                authenticateUser = true;
            }

            if(authenticateUser) {
                ApplySecurityPolicy applySecurityObject = new ApplySecurityPolicy();
                Configuration configuration;
                try {
                    getLog().info("Getting Hadoop configuration from " + hadoopConfigurationResources);
                    configuration = ApplySecurityPolicy.getConfigurationFromResources(hadoopConfigurationResources);

                    if (SecurityUtil.isSecurityEnabled(configuration)) {
                        getLog().info("Security is enabled");

                        if (principal.equals("") && keyTab.equals("")) {
                            getLog().error("Kerberos Principal and Kerberos KeyTab information missing in Kerboeros enabled cluster.");
                            session.transfer(flowFile, REL_FAILURE);
                            return;
                        }

                        try {
                            getLog().info("User authentication initiated");

                            boolean authenticationStatus = applySecurityObject.validateUserWithKerberos(logger, hadoopConfigurationResources, principal, keyTab);
                            if (authenticationStatus) {
                                getLog().info("User authenticated successfully.");
                            } else {
                                getLog().info("User authentication failed.");
                                session.transfer(flowFile, REL_FAILURE);
                                return;
                            }

                        } catch (Exception unknownException) {
                            getLog().error("Unknown exception occured while validating user :" + unknownException.getMessage());
                            session.transfer(flowFile, REL_FAILURE);
                            return;
                        }

                    }
                } catch (IOException e1) {
                    getLog().error("Unknown exception occurred while authenticating user :" + e1.getMessage());
                    session.transfer(flowFile, REL_FAILURE);
                    return;
                }
            }

            String sparkHome = context.getProperty(SPARK_HOME).getValue();

             /* Launch the spark job as a child process */
            SparkLauncher launcher = new SparkLauncher()
                .setAppResource(appJar)
                .setMainClass(mainClass)
                .setMaster(sparkMaster)
                .setConf(SparkLauncher.DRIVER_MEMORY, driverMemory)
                .setConf(SparkLauncher.EXECUTOR_CORES, numberOfExecutors)
                .setConf(SparkLauncher.EXECUTOR_MEMORY, executorMemory)
                .setConf(SparkLauncher.EXECUTOR_CORES, executorCores)
                .setConf(SPARK_NETWORK_TIMEOUT_CONFIG_NAME, networkTimeout)
                .setSparkHome(sparkHome)
                .setAppName(sparkApplicationName);

            if(authenticateUser) {
                launcher.setConf(SPARK_YARN_KEYTAB, keyTab);
                launcher.setConf(SPARK_YARN_PRINCIPAL, principal);
            }
            if (args != null) {
                launcher.addAppArgs(args);
            }
            if (ArrayUtils.isNotEmpty(extraJarPaths)) {
                for (String path : extraJarPaths) {
                    getLog().info("Adding to class path '" + path + "'");
                    launcher.addJar(path);
                }
            }
            if (StringUtils.isNotEmpty(yarnQueue)) {
                launcher.setConf(SPARK_YARN_QUEUE, yarnQueue);
            }


            Process spark = launcher.launch();

            /* Read/clear the process input stream */
            InputStreamReaderRunnable inputStreamReaderRunnable = new InputStreamReaderRunnable(LogLevel.INFO, logger, spark.getInputStream());
            Thread inputThread = new Thread(inputStreamReaderRunnable, "stream input");
            inputThread.start();

             /* Read/clear the process error stream */
            InputStreamReaderRunnable errorStreamReaderRunnable = new InputStreamReaderRunnable(LogLevel.INFO, logger, spark.getErrorStream());
            Thread errorThread = new Thread(errorStreamReaderRunnable, "stream error");
            errorThread.start();

            logger.info("Waiting for Spark job to complete");

             /* Wait for job completion */
            int exitCode = spark.waitFor();
            if (exitCode != 0) {
                logger.info("*** Completed with failed status " + exitCode);
                session.transfer(flowFile, REL_FAILURE);
            } else {
                logger.info("*** Completed with status " + exitCode);
                session.transfer(flowFile, REL_SUCCESS);
            }
        } catch (final Exception e) {
            logger.error("Unable to execute Spark job", new Object[]{flowFile, e});
            session.transfer(flowFile, REL_FAILURE);
        }

    }

    /*
     * Validates that one or more files exist, as specified in a single property.
     */

    public static final Validator createMultipleFilesExistValidator() {
        return new Validator() {
            @Override
            public ValidationResult validate(String subject, String input, ValidationContext context) {
                final String[] files = input.split(",");
                for (String filename : files) {
                    try {
                        final File file = new File(filename.trim());
                        if (!file.exists()) {
                            final String message = "file " + filename + " does not exist";
                            return new ValidationResult.Builder().subject(subject).input(input).valid(false).explanation(message).build();
                        } else if (!file.isFile()) {
                            final String message = filename + " is not a file";
                            return new ValidationResult.Builder().subject(subject).input(input).valid(false).explanation(message).build();
                        } else if (!file.canRead()) {
                            final String message = "could not read " + filename;
                            return new ValidationResult.Builder().subject(subject).input(input).valid(false).explanation(message).build();
                        }
                    } catch (SecurityException e) {
                        final String message = "Unable to access " + filename + " due to " + e.getMessage();
                        return new ValidationResult.Builder().subject(subject).input(input).valid(false).explanation(message).build();
                    }
                }
                return new ValidationResult.Builder().subject(subject).input(input).valid(true).build();
            }

        };
    }
}
