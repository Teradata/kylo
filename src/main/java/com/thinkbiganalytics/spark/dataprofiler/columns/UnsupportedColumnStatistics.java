package com.thinkbiganalytics.spark.dataprofiler.columns;


import org.apache.spark.sql.types.StructField;

/**
 * Class to hold profile statistics for columns of unsupported data type<br> 
 * [Hive data types: CHAR, BINARY, ARRAY, MAP, STRUCT, UNIONTYPE]
 * @author jagrut sharma
 *
 */
@SuppressWarnings("serial")
public class UnsupportedColumnStatistics extends ColumnStatistics {

	/**
	 * One-argument constructor
	 * @param columnField field schema
	 */
	public UnsupportedColumnStatistics(StructField columnField) {
		super(columnField);
	}
	
	
	/**
	 * Calculate unsupported type-specific statistics by accommodating the value and frequency/count<br>
	 * No additional statistics computed
	 */
	@Override
	public void accomodate(Object columnValue, Long columnCount) {
		
	}

	/**
	 * Combine with another column statistics <br>
	 * No additional statistics combined
	 */
	@Override
	public void combine(ColumnStatistics v_columnStatistics) {
		
	}

	/**
	 * Print statistics to console
	 */
	@Override
	public String getVerboseStatistics() {
		
		String retVal = "{\n" + getVerboseStatisticsCommon() 
		+ "\n" 
		+ "UnsupportedColumnType [no additional statistics"
		+ "]\n}";
		
		return retVal;
		
	}

	/**
	 * Write statistics for output result table<br>
	 * No additional statistics written
	 */
	@Override
	public void writeStatistics() {
		
		writeStatisticsCommon();
		
	}

}
