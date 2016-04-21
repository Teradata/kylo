package com.thinkbiganalytics.spark.dataprofiler.output;

import java.io.Serializable;

/**
 * Class to represent a row in profile statistics output<br>
 * Format of output:<br>
 * 
 * ColumnName, MetricType, MetricValue
 * 
 * @author jagrut sharma
 *
 */
@SuppressWarnings("serial")
public class OutputRow implements Serializable {

	private String columnName;
	private String metricType;
	private String metricValue;


	/**
	 * No-argument constructor
	 */
	public OutputRow() {
		columnName = null;
		metricType = null;
		metricValue = null;
	}


	/**
	 * Three-argument constructor to create a new row
	 * @param columnName name of column
	 * @param metricType metric type
	 * @param metricValue metric value
	 */
	public OutputRow(String columnName, String metricType, String metricValue) {
		this.columnName = columnName;
		this.metricType = metricType;
		this.metricValue = metricValue;
	}


	/**
	 * Get the column name
	 * @return column name
	 */
	public String getColumnName() {
		return columnName;
	}

	
	/**
	 * Set the column name
	 * @param columnName column name
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}


	/**
	 * Get the metric type
	 * @return metric type
	 */
	public String getMetricType() {
		return metricType;
	}


	/**
	 * Set the metric type
	 * @param metricType metric type
	 */
	public void setMetricType(String metricType) {
		this.metricType = metricType;
	}


	/**
	 * Get the metric value
	 * @return metric value
	 */
	public String getMetricValue() {
		return metricValue;
	}

	
	/**
	 * Set the metric value
	 * @param metricValue metric value
	 */
	public void setMetricValue(String metricValue) {
		this.metricValue = metricValue;
	}

	
	/**
	 * Set values for the row
	 * @param columnName name of column
	 * @param metricType metric type
	 * @param metricValue metric value
	 */
	public void setValues(String columnName, String metricType, String metricValue) {
		this.columnName = columnName;
		this.metricType = metricType;
		this.metricValue = metricValue;
	}


	/**
	 * Print verbose description of row to console
	 */
	@Override
	public String toString() {
		return "OutputRow [columnName=" + columnName + ", metricType=" + metricType + ", metricValue=" + metricValue
				+ "]";
	}

}
