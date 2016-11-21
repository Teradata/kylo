package com.thinkbiganalytics.spark.dataprofiler.columns;

import com.thinkbiganalytics.spark.dataprofiler.model.MetricType;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;
import org.apache.spark.sql.types.StructField;

import java.util.ArrayList;

/**
 * Class to hold profile statistics for columns of boolean data type <br>
 * [Hive data type: BOOLEAN]
 * @author jagrut sharma
 *
 */
@SuppressWarnings("serial")
public class BooleanColumnStatistics extends ColumnStatistics {

	/* Boolean specific metrics */
	private long trueCount;
	private long falseCount;
	
	/* Other variables */
	private boolean columnBooleanValue;
	
	
	/**
	 * One-argument constructor
	 * @param columnField field schema
	 */
	public BooleanColumnStatistics(StructField columnField) {
		
		super(columnField);
		
		trueCount = 0L;
		falseCount = 0L;
		
		columnBooleanValue = Boolean.TRUE;
	}

	
	/**
	 * Calculate boolean-specific statistics by accommodating the value and frequency/count
	 */
	@Override
	public void accomodate(Object columnValue, Long columnCount) {
		
		accomodateCommon(columnValue, columnCount);
		
		if (columnValue != null) {
			
			columnBooleanValue = Boolean.valueOf(String.valueOf(columnValue));
			
			if (columnBooleanValue == Boolean.TRUE) {
				trueCount += columnCount;
			}
			else {
				falseCount += columnCount;
			}
		}
		
	}

	
	/**
	 * Combine with another column statistics 
	 */
	@Override
	public void combine(ColumnStatistics v_columnStatistics) {
		
		combineCommon(v_columnStatistics);
		
		BooleanColumnStatistics vBoolean_columnStatistics = (BooleanColumnStatistics) v_columnStatistics;
		
		trueCount += vBoolean_columnStatistics.trueCount;
		falseCount += vBoolean_columnStatistics.falseCount;
		
	}

	
	/**
	 * Print statistics to console
	 */
	@Override
	public String getVerboseStatistics() {

		return "{\n" + getVerboseStatisticsCommon()
		+ "\n"
		+ "BooleanColumnStatistics ["
		+ "trueCount=" + trueCount
		+ ", falseCount=" + falseCount
		+ "]\n}";
	}

	
	/**
	 * Write statistics for output result table
	 */
	@Override
	public void writeStatistics() {
		writeStatisticsCommon();
		
		rows = new ArrayList<>();
		rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.TRUE_COUNT), String.valueOf(trueCount)));
		rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.FALSE_COUNT), String.valueOf(falseCount)));
		outputWriter.addRows(rows);
	}

	
	/**
	 * Get TRUE count
	 * @return TRUE count
	 */
	public long getTrueCount() {
		return trueCount;
	}

	
	/**
	 * Get FALSE count
	 * @return FALSE count
	 */
	public long getFalseCount() {
		return falseCount;
	}	
	
}
