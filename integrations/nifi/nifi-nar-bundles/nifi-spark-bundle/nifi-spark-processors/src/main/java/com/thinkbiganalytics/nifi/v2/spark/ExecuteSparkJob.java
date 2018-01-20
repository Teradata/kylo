package com.thinkbiganalytics.nifi.v2.spark;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.metadata.rest.model.data.Datasource;
import com.thinkbiganalytics.metadata.rest.model.data.JdbcDatasource;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;
import com.thinkbiganalytics.nifi.security.ApplySecurityPolicy;
import com.thinkbiganalytics.nifi.security.KerberosProperties;
import com.thinkbiganalytics.nifi.security.SecurityUtil;
import com.thinkbiganalytics.nifi.security.SpringSecurityContextLoader;
import com.thinkbiganalytics.nifi.util.InputStreamReaderRunnable;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"spark", "thinkbig"})
@CapabilityDescription("Execute a Spark job.")
public class ExecuteSparkJob extends AbstractNiFiProcessor {

    public static final String SPARK_NETWORK_TIMEOUT_CONFIG_NAME = "spark.network.timeout";
    public static final String SPARK_YARN_KEYTAB = "spark.yarn.keytab";
    public static final String SPARK_YARN_PRINCIPAL = "spark.yarn.principal";
    public static final String SPARK_YARN_QUEUE = "spark.yarn.queue";
    public static final String SPARK_CONFIG_NAME = "--conf";
    public static final String SPARK_EXTRA_FILES_CONFIG_NAME = "--files";
    public static final String SPARK_NUM_EXECUTORS = "spark.executor.instances";

    // Relationships
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
        .name("success")
        .description("Successful result.")
        .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
        .name("failure")
        .description("Spark execution failed. Incoming FlowFile will be penalized and routed to this relationship")
        .build();
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
        .description("Path to the Spark Client directory")
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
    public static final PropertyDescriptor SPARK_YARN_DEPLOY_MODE = new PropertyDescriptor.Builder()
        .name("Spark YARN Deploy Mode")
        .description("The deploy mode for YARN master (client, cluster). Only applicable for yarn mode. "
                     + "NOTE: Please ensure that you have not set this in your application.")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
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
        .required(false)
        .addValidator(createMultipleFilesExistValidator())
        .build();
    public static final PropertyDescriptor SPARK_CONFS = new PropertyDescriptor.Builder()
        .name("Spark Configurations")
        .description("Pipe separated arguments to be passed into the Spark as configurations i.e <CONF1>=<VALUE1>|<CONF2>=<VALUE2>..")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor EXTRA_SPARK_FILES = new PropertyDescriptor.Builder()
        .name("Extra Files")
        .description("Comma separated file paths to be passed to the Spark Executors")
        .required(false)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor PROCESS_TIMEOUT = new PropertyDescriptor.Builder()
        .name("Spark Process Timeout")
        .description("Time to wait for successful completion of Spark process. Routes to failure if Spark process runs for longer than expected here")
        .required(true)
        .defaultValue("1 hr")
        .addValidator(StandardValidators.TIME_PERIOD_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor DATASOURCES = new PropertyDescriptor.Builder()
        .name("Data Sources")
        .description("A comma-separated list of data source ids to include in the environment for Spark.")
        .required(false)
        .addValidator(createUuidListValidator())
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor METADATA_SERVICE = new PropertyDescriptor.Builder()
        .name("Metadata Service")
        .description("Kylo metadata service")
        .required(false)
        .identifiesControllerService(MetadataProviderService.class)
        .build();

    /**
     * Matches a comma-separated list of UUIDs
     */
    private static final Pattern UUID_REGEX = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"
                                                              + "(,[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12})*$");

    private final Set<Relationship> relationships;

    /**
     * Kerberos service keytab
     */
    private PropertyDescriptor kerberosKeyTab;

    /**
     * Kerberos service principal
     */
    private PropertyDescriptor kerberosPrincipal;

    /**
     * List of properties
     */
    private List<PropertyDescriptor> propDescriptors;

    public ExecuteSparkJob() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);
    }

    /*
     * Validates that one or more files exist, as specified in a single property.
     */
    public static final Validator createMultipleFilesExistValidator() {
        return (subject, input, context) -> {
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
        };
    }

    /**
     * Creates a new {@link Validator} that checks for a comma-separated list of UUIDs.
     *
     * @return the validator
     */
    @Nonnull
    private static Validator createUuidListValidator() {
        return (subject, input, context) -> {
            final String value = context.getProperty(DATASOURCES).evaluateAttributeExpressions().getValue();
            if (value == null || value.isEmpty() || UUID_REGEX.matcher(value).matches()) {
                return new ValidationResult.Builder().subject(subject).input(input).valid(true).explanation("List of UUIDs").build();
            } else {
                return new ValidationResult.Builder().subject(subject).input(input).valid(false).explanation("not a list of UUIDs").build();
            }
        };
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
        pds.add(SPARK_YARN_DEPLOY_MODE);
        pds.add(SPARK_HOME);
        pds.add(PROCESS_TIMEOUT);
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
        pds.add(SPARK_CONFS);
        pds.add(EXTRA_SPARK_FILES);
        pds.add(DATASOURCES);
        pds.add(METADATA_SERVICE);
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
        String PROVENANCE_JOB_STATUS_KEY = "Job Status";
        String PROVENANCE_SPARK_EXIT_CODE_KEY = "Spark Exit Code";

        try {

            PROVENANCE_JOB_STATUS_KEY = context.getName() + " Job Status";
            PROVENANCE_SPARK_EXIT_CODE_KEY = context.getName() + " Spark Exit Code";

              /* Configuration parameters for spark launcher */
            String appJar = context.getProperty(APPLICATION_JAR).evaluateAttributeExpressions(flowFile).getValue().trim();
            String extraJars = context.getProperty(EXTRA_JARS).evaluateAttributeExpressions(flowFile).getValue();
            String yarnQueue = context.getProperty(YARN_QUEUE).evaluateAttributeExpressions(flowFile).getValue();
            String mainClass = context.getProperty(MAIN_CLASS).evaluateAttributeExpressions(flowFile).getValue().trim();
            String sparkMaster = context.getProperty(SPARK_MASTER).evaluateAttributeExpressions(flowFile).getValue().trim();
            String sparkYarnDeployMode = context.getProperty(SPARK_YARN_DEPLOY_MODE).evaluateAttributeExpressions(flowFile).getValue();
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
            String sparkConfs = context.getProperty(SPARK_CONFS).evaluateAttributeExpressions(flowFile).getValue();
            String extraFiles = context.getProperty(EXTRA_SPARK_FILES).evaluateAttributeExpressions(flowFile).getValue();
            Integer sparkProcessTimeout = context.getProperty(PROCESS_TIMEOUT).evaluateAttributeExpressions(flowFile).asTimePeriod(TimeUnit.SECONDS).intValue();
            String datasourceIds = context.getProperty(DATASOURCES).evaluateAttributeExpressions(flowFile).getValue();
            MetadataProviderService metadataService = context.getProperty(METADATA_SERVICE).asControllerService(MetadataProviderService.class);

            final List<String> extraJarPaths = getExtraJarPaths(extraJars);

            // If all 3 fields are filled out then assume kerberos is enabled, and user should be authenticated
            boolean isAuthenticated = !StringUtils.isEmpty(principal) && !StringUtils.isEmpty(keyTab) && !StringUtils.isEmpty(hadoopConfigurationResources);
            try {
                if (isAuthenticated && isSecurityEnabled(hadoopConfigurationResources)) {
                    logger.info("Security is enabled");

                    if (principal.equals("") && keyTab.equals("")) {
                        logger.error("Kerberos Principal and Kerberos KeyTab information missing in Kerboeros enabled cluster. {} ", new Object[]{flowFile});
                        session.transfer(flowFile, REL_FAILURE);
                        return;
                    }

                    logger.info("User authentication initiated");

                    boolean authenticationStatus = new ApplySecurityPolicy().validateUserWithKerberos(logger, hadoopConfigurationResources, principal, keyTab);
                    if (authenticationStatus) {
                        logger.info("User authenticated successfully.");
                    } else {
                        logger.error("User authentication failed.  {} ", new Object[]{flowFile});
                        session.transfer(flowFile, REL_FAILURE);
                        return;
                    }
                }
            } catch (IOException e1) {
                logger.error("Unknown exception occurred while authenticating user : {} and flow file: {}", new Object[]{e1.getMessage(), flowFile});
                session.transfer(flowFile, REL_FAILURE);
                return;

            } catch (Exception unknownException) {
                logger.error("Unknown exception occurred while validating user : {}.  {} ", new Object[]{unknownException.getMessage(), flowFile});
                session.transfer(flowFile, REL_FAILURE);
                return;
            }

            String sparkHome = context.getProperty(SPARK_HOME).evaluateAttributeExpressions(flowFile).getValue();

            // Build environment
            final Map<String, String> env = getDatasources(session, flowFile, PROVENANCE_JOB_STATUS_KEY, datasourceIds, metadataService, extraJarPaths);
            if (env == null) {
                return;
            }

             /* Launch the spark job as a child process */
            SparkLauncher launcher = new SparkLauncher(env)
                .setAppResource(appJar)
                .setMainClass(mainClass)
                .setMaster(sparkMaster)
                .setConf(SparkLauncher.DRIVER_MEMORY, driverMemory)
                .setConf(SPARK_NUM_EXECUTORS, numberOfExecutors)
                .setConf(SparkLauncher.EXECUTOR_MEMORY, executorMemory)
                .setConf(SparkLauncher.EXECUTOR_CORES, executorCores)
                .setConf(SPARK_NETWORK_TIMEOUT_CONFIG_NAME, networkTimeout)
                .setSparkHome(sparkHome)
                .setAppName(sparkApplicationName);

            OptionalSparkConfigurator optionalSparkConf = new OptionalSparkConfigurator(launcher)
                .setDeployMode(sparkMaster, sparkYarnDeployMode)
                .setAuthentication(isAuthenticated, keyTab, principal)
                .addAppArgs(appArgs)
                .addSparkArg(sparkConfs)
                .addExtraJars(extraJarPaths)
                .setYarnQueue(yarnQueue)
                .setExtraFiles(extraFiles);

            Process spark = optionalSparkConf.getLaucnher().launch();

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
            boolean completed = spark.waitFor(sparkProcessTimeout, TimeUnit.SECONDS);
            if (!completed) {
                spark.destroyForcibly();
                getLog().error("Spark process timed out after {} seconds using flow file: {}  ", new Object[]{sparkProcessTimeout, flowFile});
                session.transfer(flowFile, REL_FAILURE);
                return;
            }

            int exitCode = spark.exitValue();

            flowFile = session.putAttribute(flowFile, PROVENANCE_SPARK_EXIT_CODE_KEY, Integer.toString(exitCode));
            if (exitCode != 0) {
                logger.error("ExecuteSparkJob for {} and flowfile: {} completed with failed status {} ", new Object[]{context.getName(), flowFile, exitCode});
                flowFile = session.putAttribute(flowFile, PROVENANCE_JOB_STATUS_KEY, "Failed");
                session.transfer(flowFile, REL_FAILURE);
            } else {
                logger.info("ExecuteSparkJob for {} and flowfile: {} completed with success status {} ", new Object[]{context.getName(), flowFile, exitCode});
                flowFile = session.putAttribute(flowFile, PROVENANCE_JOB_STATUS_KEY, "Success");
                session.transfer(flowFile, REL_SUCCESS);
            }
        } catch (final Exception e) {
            logger.error("Unable to execute Spark job {},{}", new Object[]{flowFile, e.getMessage()}, e);
            flowFile = session.putAttribute(flowFile, PROVENANCE_JOB_STATUS_KEY, "Failed With Exception");
            flowFile = session.putAttribute(flowFile, "Spark Exception:", e.getMessage());
            session.transfer(flowFile, REL_FAILURE);
        }
    }

    private Map<String, String> getDatasources(ProcessSession session, FlowFile flowFile, String PROVENANCE_JOB_STATUS_KEY, String datasourceIds, MetadataProviderService metadataService,
                                               List<String> extraJarPaths) throws JsonProcessingException {
        final Map<String, String> env = new HashMap<>();

        if (StringUtils.isNotBlank(datasourceIds)) {
            final StringBuilder datasources = new StringBuilder(10240);
            final ObjectMapper objectMapper = new ObjectMapper();
            final MetadataProvider provider = metadataService.getProvider();

            for (final String id : datasourceIds.split(",")) {
                datasources.append((datasources.length() == 0) ? '[' : ',');

                final Optional<Datasource> datasource = provider.getDatasource(id);
                if (datasource.isPresent()) {
                    if (datasource.get() instanceof JdbcDatasource && StringUtils.isNotBlank(((JdbcDatasource) datasource.get()).getDatabaseDriverLocation())) {
                        final String[] databaseDriverLocations = ((JdbcDatasource) datasource.get()).getDatabaseDriverLocation().split(",");
                        extraJarPaths.addAll(Arrays.asList(databaseDriverLocations));
                    }
                    datasources.append(objectMapper.writeValueAsString(datasource.get()));

                } else {
                    getLog().error("Required datasource {} is missing for Spark job: {}", new Object[]{id, flowFile});
                    flowFile = session.putAttribute(flowFile, PROVENANCE_JOB_STATUS_KEY, "Invalid data source: " + id);
                    session.transfer(flowFile, REL_FAILURE);
                    return null;
                }
            }

            datasources.append(']');
            env.put("DATASOURCES", datasources.toString());
        }
        return env;
    }

    private boolean isSecurityEnabled(String hadoopConfigurationResources) throws IOException {
        getLog().info("Getting Hadoop configuration from " + hadoopConfigurationResources);
        Configuration configuration = ApplySecurityPolicy.getConfigurationFromResources(hadoopConfigurationResources);
        return SecurityUtil.isSecurityEnabled(configuration);
    }

    private List<String> getExtraJarPaths(String extraJars) {
        final List<String> extraJarPaths = new ArrayList<>();
        if (!StringUtils.isEmpty(extraJars)) {
            extraJarPaths.addAll(Arrays.asList(extraJars.split(",")));
        } else {
            getLog().info("No extra jars to be added to class path");
        }
        return extraJarPaths;
    }


    @Override
    protected Collection<ValidationResult> customValidate(@Nonnull final ValidationContext validationContext) {
        final Set<ValidationResult> results = new HashSet<>();
        final String sparkMaster = validationContext.getProperty(SPARK_MASTER).evaluateAttributeExpressions().getValue().trim().toLowerCase();
        final String sparkYarnDeployMode = validationContext.getProperty(SPARK_YARN_DEPLOY_MODE).evaluateAttributeExpressions().getValue();

        if (validationContext.getProperty(DATASOURCES).isSet() && !validationContext.getProperty(METADATA_SERVICE).isSet()) {
            results.add(new ValidationResult.Builder()
                            .subject(METADATA_SERVICE.getName())
                            .input(validationContext.getProperty(METADATA_SERVICE).getValue())
                            .valid(false)
                            .explanation("Metadata Service is required when Data Sources is not empty")
                            .build());
        }

        if (StringUtils.isNotEmpty(sparkYarnDeployMode)) {
            if ((!sparkMaster.contains("local")) && (!sparkMaster.equals("yarn")) && (!sparkMaster.contains("mesos")) && (!sparkMaster.contains("spark"))) {
                results.add(new ValidationResult.Builder()
                                .subject(this.getClass().getSimpleName())
                                .valid(false)
                                .explanation("invalid spark master provided. Valid values will have local, local[n], local[*], yarn, mesos, spark")
                                .build());

            }

            if (sparkMaster.equals("yarn") && (!(sparkYarnDeployMode.equals("client") || sparkYarnDeployMode.equals("cluster")))) {
                results.add(new ValidationResult.Builder()
                                .subject(this.getClass().getSimpleName())
                                .valid(false)
                                .explanation("yarn master requires a deploy mode to be specified as either 'client' or 'cluster'")
                                .build());
            }
        }

        return results;
    }

    private class OptionalSparkConfigurator {

        private SparkLauncher launcher;

        public OptionalSparkConfigurator(SparkLauncher launcher) {
            this.launcher = launcher;
        }

        public SparkLauncher getLaucnher() {
            return this.launcher;
        }

        private OptionalSparkConfigurator setDeployMode(String sparkMaster, String sparkYarnDeployMode) {
            if (sparkMaster.equals("yarn") && StringUtils.isNotEmpty(sparkYarnDeployMode)) {
                launcher.setDeployMode(sparkYarnDeployMode);
                getLog().info("YARN deploy mode set to: {}", new Object[]{sparkYarnDeployMode});
            }
            return this;
        }

        private OptionalSparkConfigurator setAuthentication(boolean authenticateUser, String keyTab, String principal) {
            if (authenticateUser) {
                launcher.setConf(SPARK_YARN_KEYTAB, keyTab);
                launcher.setConf(SPARK_YARN_PRINCIPAL, principal);
            }
            return this;
        }

        private OptionalSparkConfigurator addAppArgs(String appArgs) {
            if (!StringUtils.isEmpty(appArgs)) {
                launcher.addAppArgs(appArgs.split(","));
            }
            return this;
        }

        private OptionalSparkConfigurator addSparkArg(String sparkConfs) {
            if (!StringUtils.isEmpty(sparkConfs)) {
                for (String conf : sparkConfs.split("\\|")) {
                    getLog().info("Adding sparkconf '" + conf + "'");
                    launcher.addSparkArg(SPARK_CONFIG_NAME, conf);
                }
            }
            return this;
        }

        private OptionalSparkConfigurator addExtraJars(List<String> extraJarPaths) {
            if (!extraJarPaths.isEmpty()) {
                for (String path : extraJarPaths) {
                    getLog().info("Adding to class path '" + path + "'");
                    launcher.addJar(path);
                }
            }
            return this;
        }

        private OptionalSparkConfigurator setYarnQueue(String yarnQueue) {
            if (StringUtils.isNotEmpty(yarnQueue)) {
                launcher.setConf(SPARK_YARN_QUEUE, yarnQueue);
            }
            return this;
        }

        private OptionalSparkConfigurator setExtraFiles(String extraFiles) {
            if (StringUtils.isNotEmpty(extraFiles)) {
                launcher.addSparkArg(SPARK_EXTRA_FILES_CONFIG_NAME, extraFiles);
            }
            return this;
        }


    }
}
