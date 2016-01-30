package com.thinkbiganalytics.spark.dataprofiler.model;

/**
 * List of metric types<br>
 * A subset of these metric types will be calculated for each data type<br>
 * For specifics, please refer to datatype metric mapping matrix
 * @author jagrut sharma
 *
 */
public enum MetricType {
	
	/**
	 * Datatype of column (as evaluated by Spark)
	 */
	COLUMN_DATATYPE,
	
	
	/**
	 * Does column allow null values? 
	 */
	COLUMN_NULLABLE,
	
	
	/**
	 * Metadata associated with column
	 */
	COLUMN_METADATA,
	
	
	/**
	 * Number of null values
	 */
	NULL_COUNT,
	
	
	/**
	 * Total values (includes nulls and empty values)
	 */
	TOTAL_COUNT,
	
	
	/**
	 * Total unique values (null and empty are considered a unique value each)
	 */
	UNIQUE_COUNT, 
	
	
	/**
	 * Percentage of null values
	 */
	PERC_NULL_VALUES, 
	
	
	/**
	 * Percentage of unique values
	 */
	PERC_UNIQUE_VALUES, 
	
	
	/**
	 * Percentage of duplicate values
	 */
	PERC_DUPLICATE_VALUES, 
	
	
	/**
	 * Top n values (in order of their frequency)
	 */
	TOP_N_VALUES,
	
	
	/**
	 * Maximum length of string
	 */
	MAX_LENGTH,
	
	
	/**
	 * Minimum length of string
	 */
	MIN_LENGTH,
	
	
	/**
	 * Longest string value
	 */
	LONGEST_STRING,
	
	
	/**
	 * Shortest string value
	 */
	SHORTEST_STRING,
	
	
	/**
	 * Total empty strings (empty string is not a null value)
	 */
	EMPTY_COUNT,
	
	
	/**
	 * Percentage of empty strings
	 */
	PERC_EMPTY_VALUES,
	
	
	/**
	 * Maximum of numeric values
	 */
	MAX,
	
	
	/**
	 * Minimum of numeric values
	 */
	MIN,
	
	
	/**
	 * Sum of numeric values
	 */
	SUM,
	
	
	/**
	 * Count of TRUE boolean values
	 */
	TRUE_COUNT,
	
	
	/**
	 * Count of FALSE boolean values
	 */
	FALSE_COUNT,
	
	
	/**
	 * Mean (average) of numeric values
	 */
	MEAN,
	
	
	/**
	 * Standard Deviation (Population) of numeric values
	 */
	STDDEV,
	
	
	/**
	 * Variance (Population) of numeric values
	 */
	VARIANCE,
	
	
	/**
	 * Latest date value
	 */
	MAX_DATE,
	
	
	/**
	 * Earliest date value
	 */
	MIN_DATE,
	
	
	/**
	 * Latest timestamp value
	 */
	MAX_TIMESTAMP,
	
	
	/**
	 * Earliest timestamp value
	 */
	MIN_TIMESTAMP
}
