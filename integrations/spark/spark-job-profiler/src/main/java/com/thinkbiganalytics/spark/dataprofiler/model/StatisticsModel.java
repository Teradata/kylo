package com.thinkbiganalytics.spark.dataprofiler.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructField;

import com.thinkbiganalytics.spark.dataprofiler.columns.BigDecimalColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.BooleanColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ByteColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.DateColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.DoubleColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.FloatColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.IntegerColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.LongColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ShortColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.StringColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.TimestampColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.UnsupportedColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputWriter;


/**
 * Class to store the profile statistics
 * @author jagrut sharma
 *
 */
@SuppressWarnings("serial")
public class StatisticsModel implements Serializable {

	Map<Integer, ColumnStatistics> columnStatisticsMap = new HashMap<Integer, ColumnStatistics>();
	final String decimalTypeRegex = "decimal\\S+";
	OutputWriter outputWriter = null;

	
	/**
	 * Include a column value in calculation of profile statistics for the column
	 * @param columnIndex numeric index of column (0-based)
	 * @param columnValue value in column
	 * @param columnCount number of times value is found in column
	 * @param columnField schema information of the column
	 */
	public void add (Integer columnIndex, Object columnValue, Long columnCount, StructField columnField) {

		ColumnStatistics newColumnStatistics = null;
		DataType columnDataType = columnField.dataType();

		switch(columnDataType.simpleString()) {

		/* === Group 1 ===*/

		/*
		 * Hive datatype: 		TINYINT
		 * SparkSQL datatype: 	tinyint
		 * Java datatype:		Byte
		 */
		case "tinyint":
			newColumnStatistics = new ByteColumnStatistics(columnField);
			break;


			/*
			 * Hive datatype: 		SMALLINT
			 * SparkSQL datatype: 	smallint
			 * Java datatype:		Short
			 */
		case "smallint":
			newColumnStatistics = new ShortColumnStatistics(columnField);
			break;


			/*
			 * Hive datatype: 		INT
			 * SparkSQL datatype: 	int
			 * Java datatype:		Int
			 */	
		case "int":
			newColumnStatistics = new IntegerColumnStatistics(columnField);
			break;


			/*
			 * Hive datatype: 		BIGINT
			 * SparkSQL datatype: 	bigint
			 * Java datatype:		Long
			 */
		case "bigint":
			newColumnStatistics = new LongColumnStatistics(columnField);
			break;	



			/* === Group 2 === */

			/*
			 * Hive datatype: 		FLOAT
			 * SparkSQL datatype: 	float
			 * Java datatype:		Float
			 */		
		case "float":
			newColumnStatistics = new FloatColumnStatistics(columnField);
			break;


			/*
			 * Hive datatype: 		DOUBLE
			 * SparkSQL datatype: 	double
			 * Java datatype:		Double
			 */			
		case "double":
			newColumnStatistics = new DoubleColumnStatistics(columnField);
			break;



			/* === Group 3 === */

			/*
			 * Hive datatypes: 		STRING, VARCHAR
			 * SparkSQL datatype: 	string
			 * Java datatype:		String
			 */			
		case "string":
			newColumnStatistics = new StringColumnStatistics(columnField);
			break;



			/* === Group 4 === */

			/*
			 * Hive datatype: 		BOOLEAN
			 * SparkSQL datatype: 	boolean
			 * Java datatype:		Boolean
			 */		
		case "boolean":
			newColumnStatistics = new BooleanColumnStatistics(columnField);
			break;



			/* === Group 5 === */

			/*
			 * Hive datatype: 		DATE
			 * SparkSQL datatype: 	date
			 * Java datatype:		java.sql.Date
			 */
		case "date":
			newColumnStatistics = new DateColumnStatistics(columnField);
			break;


			/*
			 * Hive datatype: 		TIMESTAMP
			 * SparkSQL datatype: 	timestamp
			 * Java datatype:		java.sql.Timestamp
			 */			
		case "timestamp":
			newColumnStatistics = new TimestampColumnStatistics(columnField);
			break;



			/* === Group 6 === */

		default:
			/*
			 * Hive datatype: 		DECIMAL
			 * SparkSQL datatype: 	decimal
			 * Java datatype:		java.math.BigDecimal
			 *
			 * Handle the decimal type here since it comes with scale and precision e.g. decimal(7,5)
			 */
			if (columnDataType.simpleString().matches(decimalTypeRegex)) {
				newColumnStatistics = new BigDecimalColumnStatistics(columnField);
			}

			/*
			 * Hive datatypes: 		CHAR, BINARY, ARRAY, MAP, STRUCT, UNIONTYPE
			 */
			else {
				System.out.println("[PROFILER-INFO] Unsupported data type: " + columnDataType.simpleString());
				newColumnStatistics = new UnsupportedColumnStatistics(columnField);
			}
		}


		if(!columnStatisticsMap.containsKey(columnIndex)) {
			columnStatisticsMap.put(columnIndex, newColumnStatistics);
		}

		ColumnStatistics currentColumnStatistics = columnStatisticsMap.get(columnIndex);
		currentColumnStatistics.accomodate(columnValue, columnCount);
	}



	/**
	 * Combine another statistics model
	 * @param statisticsModel model to combine with
	 */
	public void combine (StatisticsModel statisticsModel) {

		for (Integer k_columnIndex: statisticsModel.columnStatisticsMap.keySet()) {

			ColumnStatistics columnStatistics = columnStatisticsMap.get(k_columnIndex);
			ColumnStatistics v_columnStatistics = statisticsModel.columnStatisticsMap.get(k_columnIndex);

			if (columnStatistics != null) {
				
				columnStatistics.combine(v_columnStatistics);
				
			}
			else {
				columnStatisticsMap.put(k_columnIndex, v_columnStatistics);
			}
		}
	}


	/**
	 * Write the profile statistics to Hive
	 * @param sc JavaSparkContext
	 * @param hiveContext HiveContext
	 */
	public void writeModel(JavaSparkContext sc, HiveContext hiveContext) {

		for (Integer columnIndex: columnStatisticsMap.keySet()) {
			columnStatisticsMap.get(columnIndex).writeStatistics();
		}

		outputWriter = OutputWriter.getInstance();
		outputWriter.writeResultToTable(sc, hiveContext);
	}

	

	/**
	 * Print the profile statistics on console
	 * @return profile model string
	 */
	public String printModel() {

		StringBuilder sb = new StringBuilder();		
		sb.append("====== Statistics Model ======");
		sb.append("\n");

		for (Integer columnIndex: columnStatisticsMap.keySet()) {
			sb.append("=== Column #" + columnIndex + "\n");
			sb.append(columnStatisticsMap.get(columnIndex).getVerboseStatistics() + "\n");
		}

		sb.append("==============================");
		return sb.toString();
	}


	
	/**
	 * Print the profile statistics on console
	 */
	@Override
	public String toString() {
		return printModel();
	}



	/**
	 * Get the column statistics map (column number mapped to column statistics)
	 * @return column statistics map
	 */
	public Map<Integer, ColumnStatistics> getColumnStatisticsMap() {
		return columnStatisticsMap;
	}
	
}
