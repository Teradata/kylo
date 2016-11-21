package com.thinkbiganalytics.spark.dataprofiler.model;

import org.apache.spark.sql.types.StructField;

import java.util.HashMap;
import java.util.Map;

/**
 * A helper class to hold the schema
 * @author jagrut sharma
 *
 */

public abstract class SchemaInfo {

	/**
	 * Mapping from column index to column schema
	 */
	public static final Map<Integer, StructField> schemaMap = new HashMap<>();

	/* no instantiation */
	private SchemaInfo() {}
}
