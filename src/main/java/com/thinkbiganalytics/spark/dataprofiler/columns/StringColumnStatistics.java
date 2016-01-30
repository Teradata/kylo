package com.thinkbiganalytics.spark.dataprofiler.columns;

import java.util.ArrayList;

import org.apache.spark.sql.types.StructField;

import com.thinkbiganalytics.spark.dataprofiler.model.MetricType;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;

/**
 * Class to hold profile statistics for columns of string data type <br>
 * [Hive data types: STRING, VARCHAR]
 * @author jagrut sharma
 *
 */
@SuppressWarnings("serial")
public class StringColumnStatistics extends ColumnStatistics {

	/* String specific metrics */
	private int maxLength;
	private int minLength;
	private String longestString;
	private String shortestString;
	private long emptyCount;
	private double percEmptyValues;

	/* Other variables */
	private String columnStringValue;
	private int columnStringLength;


	/**
	 * One-argument constructor
	 * @param columnField field schema
	 */
	public StringColumnStatistics(StructField columnField) {
		super(columnField);

		maxLength = Integer.MIN_VALUE;
		minLength = Integer.MAX_VALUE;
		longestString = null;
		shortestString = null;
		emptyCount = 0;
		percEmptyValues = 0.0d;

		columnStringValue = null;
		columnStringLength = 0;
	}


	/**
	 * Calculate string-specific statistics by accommodating the value and frequency/count
	 */
	@Override
	public void accomodate(Object columnValue, Long columnCount) {

		accomodateCommon(columnValue, columnCount);

		if (columnValue!= null) {

			columnStringValue = String.valueOf(columnValue);
			columnStringLength = columnStringValue.length();

			if (maxLength < columnStringLength) {
				maxLength = columnStringLength;
				longestString = columnStringValue;
			}

			if (minLength > columnStringLength) {
				minLength = columnStringLength;
				shortestString = columnStringValue;
			}

			if (columnStringValue.isEmpty()) {
				emptyCount += columnCount;
			}

			//TODO: Think about only doing these derived calculations only in combine phase
			doPercentageCalculations();
		}		
	}


	/**
	 * Combine with another column statistics 
	 */
	@Override
	public void combine(ColumnStatistics v_columnStatistics) {

		combineCommon(v_columnStatistics);

		StringColumnStatistics vString_columnStatistics = (StringColumnStatistics) v_columnStatistics;

		maxLength = Math.max(maxLength, vString_columnStatistics.maxLength);
		minLength = Math.min(minLength, vString_columnStatistics.minLength);

		if (longestString.length() < vString_columnStatistics.longestString.length()) {
			longestString = vString_columnStatistics.longestString;
		}

		if (shortestString.length() > vString_columnStatistics.shortestString.length()) {
			shortestString = vString_columnStatistics.shortestString;
		}

		emptyCount += vString_columnStatistics.emptyCount;

		doPercentageCalculations();
	}


	/*
	 * Calculate percentage metrics
	 */
	private void doPercentageCalculations() {
		percEmptyValues = ((double) emptyCount / totalCount) * 100;
	}

	
	/**
	 * Write statistics for output result table
	 */
	@Override
	public void writeStatistics() {

		writeStatisticsCommon();

		rows = new ArrayList<OutputRow>();
		rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MAX_LENGTH), String.valueOf(maxLength)));
		rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MIN_LENGTH), String.valueOf(minLength)));
		rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.LONGEST_STRING), String.valueOf(longestString)));
		rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.SHORTEST_STRING), String.valueOf(shortestString)));
		rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.EMPTY_COUNT), String.valueOf(emptyCount)));
		rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.PERC_EMPTY_VALUES), df.format(percEmptyValues)));
		outputWriter.addRows(rows);
	}


	/**
	 * Print statistics to console
	 */
	@Override
	public String getVerboseStatistics() {
		String retVal = "{\n" + getVerboseStatisticsCommon() 
		+ "\n" 
		+ "StringColumnStatistics [" 
		+ "maxLength=" + maxLength
		+ ", minLength=" + minLength
		+ ", longestString=" + longestString
		+ ", shortestString=" + shortestString
		+ ", emptyCount=" + emptyCount
		+ ", percEmptyValues=" + df.format(percEmptyValues)
		+ "]\n}";

		return retVal;
	}


	/**
	 * Get length of longest string
	 * @return max length
	 */
	public int getMaxLength() {
		return maxLength;
	}


	/**
	 * Get length of shortest string
	 * @return min length
	 */
	public int getMinLength() {
		return minLength;
	}


	/**
	 * Get value of longest string
	 * @return longest string
	 */
	public String getLongestString() {
		return longestString;
	}

	
	/**
	 * Get value of shortest string (empty string is still considered a string)
	 * @return shortest string
	 */
	public String getShortestString() {
		return shortestString;
	}


	/**
	 * Get count of empty strings
	 * @return empty string count
	 */
	public long getEmptyCount() {
		return emptyCount;
	}


	/**
	 * Get percentage of empty strings
	 * @return perc empty strings
	 */
	public double getPercEmptyValues() {
		return percEmptyValues;
	}

}
