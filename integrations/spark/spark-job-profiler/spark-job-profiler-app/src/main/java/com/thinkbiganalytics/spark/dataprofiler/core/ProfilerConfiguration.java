package com.thinkbiganalytics.spark.dataprofiler.core;

/*-
 * #%L
 * thinkbig-spark-job-profiler-app
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
	 * Name of database to write result to
	 */
	public static String OUTPUT_DB_NAME = "default";
	
	
	/**
	 * Name of table to write result to<br>
	 * A required command line parameter 
	 */
	public static String OUTPUT_TABLE_NAME = "profilestats";

	
	/**
	 * Partition key to read and write to
	 */
	public static String INPUT_AND_OUTPUT_TABLE_PARTITION_KEY = "partitionKey";
	
	
	/**
	 * Partition column name for input table
	 */
	public static String INPUT_TABLE_PARTITION_COLUMN_NAME = "processing_dttm";
	
	
	/**
	 * Partition column name for output table
	 */
	public static String OUTPUT_TABLE_PARTITION_COLUMN_NAME = "processing_dttm";

	
	/**
	 * Helper method for unit testing
	 */
	public static void initialize() {
		APP_NAME = "Profiler";
		SQL_DIALECT = "hiveql";
		SERIALIZER = "kryo";
		NUMBER_OF_TOP_N_VALUES = 3;
		TOP_N_VALUES_INTERNAL_DELIMITER = "^A";
		TOP_N_VALUES_RECORD_DELIMITER = "^B";
		DECIMAL_DIGITS_TO_DISPLAY_CONSOLE_OUTPUT = 4;
		OUTPUT_DB_NAME = "default";
		OUTPUT_TABLE_NAME = "profilestats";
		INPUT_AND_OUTPUT_TABLE_PARTITION_KEY = "partitionKey";
		INPUT_TABLE_PARTITION_COLUMN_NAME = "processing_dttm";
		OUTPUT_TABLE_PARTITION_COLUMN_NAME = "processing_dttm";
	}
	
	/* no instantiation */
	private ProfilerConfiguration() {
		
	}

}
