package com.thinkbiganalytics.spark.dataprofiler.core;

/**
 * Helper class to hold parameters for profiler
 * @author jagrut sharma
 *
 */

/* Class not intended to be instantiated */
public class ProfilerConfiguration {
	
	/** 
	 * Application name for Spark UI 
	 */
	public static String APP_NAME = "Profiler";
	
	
	/** 
	 * Flavor of queries to run - Hive supported HQL 
	 */
	public static String SQL_DIALECT = "hiveql";
	
	
	/** 
	 * Efficient Kryo serialization
	 */
	public static String SERIALIZER = "kryo";
	
	
	/** 
	 * N for top-N values to store in result table<br>
	 * A required command line parameter 
	 */
	public static Integer NUMBER_OF_TOP_N_VALUES = 3;
	
	
	/**
	 * Delimiter to use when storing top-N values in result table<br>
	 * This delimiter is output between fields of a single top-N entry
	 */
	public static String TOP_N_VALUES_INTERNAL_DELIMITER = "^A";
	
	
	/**
	 * Delimiter to use when storing top-N values in result table<br>
	 * This delimiter is output between top-N entries
	 */
	public static String TOP_N_VALUES_RECORD_DELIMITER = "^B";
	
	
	/** Number of decimals to print out in console<br> 
	 * (not considered when writing to table) 
	 */
	public static Integer DECIMAL_DIGITS_TO_DISPLAY_CONSOLE_OUTPUT = 4;
	
	
	/**
	 * Name of table to write result to<br>
	 * A required command line parameter 
	 */
	public static String OUTPUT_TABLE_NAME = "profilestats";

	/**
	 * Partition key to read and write to
	 */
	public static String PARTITION_KEY = "partitionKey";
	
	
	/* no instantiation */
	private ProfilerConfiguration() {
		
	}

}
