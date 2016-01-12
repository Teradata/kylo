/*
 * Copyright (c) 2015. Teradata Inc.
 */

package com.thinkbiganalytics.nifi;

import com.thinkbiganalytics.util.InputStreamReaderRunnable;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.LogLevel;
import org.apache.nifi.logging.ProcessorLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.spark.launcher.SparkLauncher;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_ALLOWED)
@Tags({"spark", "thinkbig"})
@CapabilityDescription("Execute a Spark job. "
)
public class ExecuteSparkJob extends AbstractProcessor {

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
/*
    public static final PropertyDescriptor DBCP_SERVICE = new PropertyDescriptor.Builder()
            .name("Database Connection Pooling Service")
            .description("The Controller Service that is used to obtain connection to database")
            .required(true)
            .identifiesControllerService(DBCPService.class)
            .build();
*/

    public static final PropertyDescriptor APPLICATION_JAR = new PropertyDescriptor.Builder()
            .name("ApplicationJAR")
            .description("Path to the JAR file containing the Spark job application")
            .required(true)
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


    private final List<PropertyDescriptor> propDescriptors;

    public ExecuteSparkJob() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(APPLICATION_JAR);
        pds.add(MAIN_CLASS);
        pds.add(MAIN_ARGS);
        pds.add(SPARK_MASTER);
        pds.add(SPARK_HOME);
        pds.add(QUERY_TIMEOUT);
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

    private void setQueryTimeout(Statement st, int queryTimeout) {
        final ProcessorLog logger = getLogger();
        try {
            st.setQueryTimeout(queryTimeout); // timeout in seconds
        } catch (SQLException e) {
            logger.debug("Timeout is unsupported. No timeout will be provided.");
        }
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        final ProcessorLog logger = getLogger();
        FlowFile incoming = session.get();
        FlowFile outgoing = (incoming == null ? session.create() : incoming);

        try {
              /* Configuration parameters for spark launcher */
            String appJar = context.getProperty(APPLICATION_JAR).evaluateAttributeExpressions(incoming).getValue(); //"target/spark-launcher-demo-0.0.1-SNAPSHOT-jar-with-dependencies.jar";
            String mainClass = context.getProperty(MAIN_CLASS).evaluateAttributeExpressions(incoming).getValue();
            String sparkMaster = context.getProperty(SPARK_MASTER).evaluateAttributeExpressions(incoming).getValue();
            String appArgs = context.getProperty(MAIN_ARGS).evaluateAttributeExpressions(incoming).getValue();
            String[] args = null;
            if (!StringUtils.isEmpty(appArgs)) {
                args = appArgs.split(",");
            }
            String driverMemory = "512m";
            String numExecutors = "1";
            String execMemory = "256m";
            String sparkHome = context.getProperty(SPARK_HOME).getValue();
            String appName = "Simple Spark App Launcher Demo";

             /* Launch the spark job as a child process */
            SparkLauncher launcher = new SparkLauncher()
                    .setAppResource(appJar)
                    .setMainClass(mainClass)
                    .setMaster(sparkMaster)
                    .setConf(SparkLauncher.DRIVER_MEMORY, driverMemory)
                    .setConf(SparkLauncher.EXECUTOR_CORES, numExecutors)
                    .setConf(SparkLauncher.EXECUTOR_MEMORY, execMemory)
                    .setSparkHome(sparkHome)
                    .setAppName(appName);
            if (args != null) {
                launcher.addAppArgs(args);
            }
            Process spark = launcher.launch();

            /*
             * Need to read/clear the process input and error streams
             * Otherwise, the Process buffer can get full.
             * Job may show behavior of waiting indefinitely.
             */

            /* Read/clear the process input stream */
            InputStreamReaderRunnable inputStreamReaderRunnable = new InputStreamReaderRunnable(LogLevel.INFO, logger, spark.getInputStream());
            Thread inputThread = new Thread(inputStreamReaderRunnable, "stream input");
            inputThread.start();

             /* Read/clear the process error stream */
            InputStreamReaderRunnable errorStreamReaderRunnable = new InputStreamReaderRunnable(LogLevel.ERROR, logger, spark.getErrorStream());
            Thread errorThread = new Thread(errorStreamReaderRunnable, "stream error");
            errorThread.start();

            System.out.println("*** Waiting for job to complete ***");

             /* Wait for job completion */
            int exitCode = spark.waitFor();
            System.out.println("*** Completed with status " + exitCode);
            session.transfer(outgoing, REL_SUCCESS);
        } catch (final IOException | InterruptedException e) {
            e.printStackTrace();
            logger.error("Unable to execute Spark job", new Object[]{incoming, e});
            session.transfer(incoming, REL_FAILURE);
        }

    }


}