package com.thinkbiganalytics.spark;

import com.thinkbiganalytics.spark.util.InputStreamReaderRunnable;

import java.util.Arrays;

/**
 * This is a demo of launching a spark job via Java
 * Does not need spark-submit
 *
 * @author jagrut
 */

public class SparkLauncher {

    public static void main(String[] args) throws Exception {

        System.out.println("*** Spark Launcher Demo - Start ***");

		/* Spark master must be provided via command line. 
		 * examples of valid values: [local, yarn-client, yarn-cluster]
		 */
        String[] args2;
        if ((args.length < 1) || (args[0] == null)) {
            System.out.println("[SparkLauncher] Spark master must be provided via command line argument.");
            return;
        } else {
            System.out.println("[SparkLauncher] Spark master will be set to: " + args[0]);
            args2=Arrays.copyOfRange(args, 1, args.length);
            for (String arg : args2) {
                System.out.println("   "+arg);
            }
        }
		
		/* Configuration parameters for spark launcher */
        String APPLICATION_JAR = "target/spark-launcher-demo-0.0.1-SNAPSHOT-jar-with-dependencies.jar";
        //String MAIN_CLASS = "com.thinkbiganalytics.spark.Validator";
        String MAIN_CLASS = "com.thinkbiganalytics.spark.MergeDedupe";
        String SPARK_MASTER = args[0];
        String DRIVER_MEMORY = "512m";
        String NUM_EXECUTORS = "1";
        String EXECUTOR_MEMORY = "256m";
        String SPARK_HOME = "/usr/hdp/current/spark-client/";
        String APP_NAME = "Simple Spark App Launcher Demo";
		
		
		/* Launch the spark job as a child process */
        Process spark = new org.apache.spark.launcher.SparkLauncher()
                .setAppResource(APPLICATION_JAR)
                .setMainClass(MAIN_CLASS)
                .setMaster(SPARK_MASTER)
                .setConf(org.apache.spark.launcher.SparkLauncher.DRIVER_MEMORY, DRIVER_MEMORY)
                .setConf(org.apache.spark.launcher.SparkLauncher.EXECUTOR_CORES, NUM_EXECUTORS)
                .setConf(org.apache.spark.launcher.SparkLauncher.EXECUTOR_MEMORY, EXECUTOR_MEMORY)
                .addAppArgs(args2)
                        //.addAppArgs(new String[]{"emp_sr", "employee", "20151202100000"})
                .setSparkHome(SPARK_HOME)
                .setAppName(APP_NAME)
                .launch();
		
		/*
		 * Need to read/clear the process input and error streams
		 * Otherwise, the Process buffer can get full.
		 * Job may show behavior of waiting indefinitely. 
		 */
		
		/* Read/clear the process input stream */
        InputStreamReaderRunnable inputStreamReaderRunnable = new InputStreamReaderRunnable(spark.getInputStream());
        Thread inputThread = new Thread(inputStreamReaderRunnable, "stream input");
        inputThread.start();

		/* Read/clear the process error stream */
        InputStreamReaderRunnable errorStreamReaderRunnable = new InputStreamReaderRunnable(spark.getErrorStream());
        Thread errorThread = new Thread(errorStreamReaderRunnable, "stream error");
        errorThread.start();

        System.out.println("*** Waiting for job to complete ***");

		/* Wait for job completion */
        int exitCode = spark.waitFor();

		/* Print completion and exit code */
        System.out.println("*** Job finished with exit code:" + exitCode + " ***");
        System.out.println("*** Spark Launcher Demo - End ***");
    }
}
