package com.thinkbiganalytics.spark.dataprofiler.columns;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.spark.sql.types.StructField;

import com.thinkbiganalytics.spark.dataprofiler.model.MetricType;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;

/**
 * Class to hold profile statistics for columns of timestamp data type <br>
 * [Hive data type: TIMESTAMP]
 * @author jagrut sharma
 *
 */
@SuppressWarnings("serial")
public class TimestampColumnStatistics extends ColumnStatistics {

	/* Timestamp specific metrics */
	private Timestamp maxTimestamp;
	private Timestamp minTimestamp;
	
	/* Other variables */
	private Timestamp columnTimestampValue;
	
	
	/**
	 * One-argument constructor
	 * @param columnField field schema
	 */
	public TimestampColumnStatistics(StructField columnField) {
		
		super(columnField);
		
		maxTimestamp = new Timestamp(Long.MIN_VALUE);
		minTimestamp = new Timestamp(Long.MAX_VALUE);
	}


	/**
	 * Calculate timestamp-specific statistics by accommodating the value and frequency/count
	 */
	@Override
	public void accomodate(Object columnValue, Long columnCount) {
		
		accomodateCommon(columnValue, columnCount);
		
		if (columnValue != null) {
			columnTimestampValue = Timestamp.valueOf(String.valueOf(columnValue));
		}
		
		if (maxTimestamp.before(columnTimestampValue)) {
			maxTimestamp = columnTimestampValue;
		}
		
		if (minTimestamp.after(columnTimestampValue)) {
			minTimestamp = columnTimestampValue;
		}
		
	}

	
	/**
	 * Combine with another column statistics 
	 */
	@Override
	public void combine(ColumnStatistics v_columnStatistics) {
		
		combineCommon(v_columnStatistics);
		
		TimestampColumnStatistics vTimestamp_columnStatistics = (TimestampColumnStatistics) v_columnStatistics;

		if (maxTimestamp.before(vTimestamp_columnStatistics.maxTimestamp)) {
			maxTimestamp = vTimestamp_columnStatistics.maxTimestamp;
		}
		
		if (minTimestamp.after(vTimestamp_columnStatistics.minTimestamp)) {
			minTimestamp = vTimestamp_columnStatistics.minTimestamp;
		}
	}


	/**
	 * Print statistics to console
	 */
	@Override
	public String getVerboseStatistics() {
		String retVal = "{\n" + getVerboseStatisticsCommon() 
		+ "\n" 
		+ "TimestampColumnStatistics ["
		+ "maxTimestamp=" + maxTimestamp
		+ ", minTimestamp=" + minTimestamp
		+ "]\n}";
		
		return retVal;
	}


	/**
	 * Write statistics for output result table
	 */
	@Override
	public void writeStatistics() {
		
		writeStatisticsCommon();
		
		rows = new ArrayList<OutputRow>();
		rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MAX_TIMESTAMP), String.valueOf(maxTimestamp)));
		rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MIN_TIMESTAMP), String.valueOf(minTimestamp)));
		outputWriter.addRows(rows);
		
	}


	/**
	 * Get latest timestamp
	 * @return latest timestamp
	 */
	public Timestamp getMaxTimestamp() {
		return maxTimestamp;
	}


	/**
	 * Get earliest timestamp
	 * @return earliest timestamp
	 */
	public Timestamp getMinTimestamp() {
		return minTimestamp;
	}
	
}
