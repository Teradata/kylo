package com.thinkbiganalytics.spark.dataprofiler.output;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.hive.HiveContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Class to write profile statistics result to Hive table
 * @author jagrut sharma
 *
 */
@SuppressWarnings("serial")
public class OutputWriter implements Serializable{

	private JavaSparkContext sc = null;
	private HiveContext hiveContext = null;
	private String outputTable = null;
	private String partitionKey = null;
	private JavaRDD<OutputRow> outputRowsRDD = null;

	private static List<OutputRow> outputRows = new ArrayList<OutputRow>();
	private static OutputWriter outputWriter = null;

	
	/* no direct instantiation */
	private OutputWriter() {}


	/**
	 * Get the instance of OutputWriter
	 * @return OutputWriter
	 */
	public static OutputWriter getInstance() {

		if (outputWriter == null) {
			outputWriter = new OutputWriter();
		}

		return outputWriter;
	}


	/**
	 * Get list of rows to write
	 * @return list of OutputRow
	 */
	public List<OutputRow> getOutputRows() {
		return outputRows;
	}


	/**
	 * Get count of output rows
	 * @return row count
	 */
	public int getOutputRowCount() {
		return outputRows.size();
	}


	/**
	 * Set table name for writing result
	 * @param p_outputTable table name
	 */
	public void setOutputTable(String p_outputTable) {
		outputTable = p_outputTable;
	}


	/**
	 * Get table name where result will be written to
	 * @return table name
	 */
	public String getOutputTable() {
		return outputTable;
	}


	/* 
	 * Helper method:
	 * Check if output table has been set 
	 */
	private boolean checkOutputTableSetting() {

		if (outputTable == null) {
			return false;
		}
		else {
			return true;
		}
	}


	/**
	 * Set Spark context
	 * @param p_sc JavaSparkContext
	 */
	public void setSparkContext(JavaSparkContext p_sc) {
		sc = p_sc;
	}


	/**
	 * Set Hive context
	 * @param p_hiveContext HiveContext
	 */
	public void setHiveContext(HiveContext p_hiveContext) {
		hiveContext = p_hiveContext;
	}


	/**
	 * Add row to write in output
	 * @param row row for output
	 */
	public void addRow(OutputRow row) {
		outputRows.add(row);
	}


	/**
	 * Add multiple rows to write in output
	 * @param rows list of rows for output
	 */
	public void addRows(List<OutputRow> rows) {
		outputRows.addAll(rows);
	}

	
	/**
	 * Write result to Hive table
	 * @param p_sc JavaSparkContext
	 * @param p_hiveContext HiveContext
	 * @param p_outputTable output table name
	 * @return boolean indicating result of write
	 */
	public boolean writeResultToTable(JavaSparkContext p_sc, HiveContext p_hiveContext, String p_outputTable, String p_partitionKey) {
		sc = p_sc;
		hiveContext = p_hiveContext;
		outputTable = p_outputTable;
		partitionKey = p_partitionKey;
		return writeResultToTable();
	}



	/*
	 * Write result to Hive table after verifying checks
	 */
	private boolean writeResultToTable() {

		boolean retVal = false;

		if (!checkOutputTableSetting()) {
			System.out.println("Error writing result: Output table not set.");
		}
		else if (sc == null){ 
			System.out.println("Error writing result: Spark context is not available.");
		}
		else if (hiveContext == null) {
			System.out.println("Error writing result: Hive context is not available.");
		}
		else {			
			outputRowsRDD = sc.parallelize(outputRows);
			DataFrame outputRowsDF = hiveContext.createDataFrame(outputRowsRDD, OutputRow.class);			
			//outputRowsDF.write().mode(SaveMode.Overwrite).saveAsTable(outputTable);

			// Since SPark doesn't support partitions write to temp table then write to partitioned table
			String tempTable = outputTable + "_" + System.currentTimeMillis();
			outputRowsDF.registerTempTable(tempTable);

			hiveContext.executeSql("INSERT INTO " + outputTable + " PARTITION (processing_dttm='" + partitionKey + "') SELECT * FROM "+tempTable);

			System.out.println("[PROFILER-INFO] Metrics written to Hive table: " + outputTable + " [" + outputRows.size() +" rows]");
			retVal = true;
		}

		return retVal;
	}
}