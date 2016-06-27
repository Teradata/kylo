package com.thinkbiganalytics.spark.dataprofiler.columns;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.types.StructField;

import com.thinkbiganalytics.spark.dataprofiler.model.MetricType;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Computes statistics for timestamp columns.
 */
@SuppressWarnings("serial")
public class TimestampColumnStatistics extends ColumnStatistics {
	/** Records the maximum value of the column */
	@Nullable
	private Timestamp maxTimestamp;

	/** Records the minimum value of the column */
	@Nullable
	private Timestamp minTimestamp;

	/**
	 * Constructs a {@code TimestampColumnStatistics} for profiling the the specified field.
	 *
	 * @param columnField the field to be profiled
	 */
	public TimestampColumnStatistics(@Nonnull final StructField columnField) {
		super(columnField);
	}

	/**
     * Adds the specified value to the statistics for this column.
     *
     * @param columnValue the column value to be profiled
     * @param columnCount the number of rows containing the value
	 */
	@Override
	public void accomodate(@Nullable final Object columnValue, @Nonnull Long columnCount) {
        // Update common statistics
		accomodateCommon(columnValue, columnCount);

        // Update timestamp-specific statistics
        String stringValue = (columnValue != null) ? columnValue.toString() : null;

		if (!StringUtils.isEmpty(stringValue)) {
			Timestamp timestamp = Timestamp.valueOf(stringValue);
            if (maxTimestamp == null || maxTimestamp.before(timestamp)) {
                maxTimestamp = timestamp;
            }
            if (minTimestamp == null || minTimestamp.after(timestamp)) {
                minTimestamp = timestamp;
            }
		}
	}

	/**
     * Merges the specified statistics into this object.
     *
     * @param v_columnStatistics the statistics to be merged
	 */
	@Override
	public void combine(@Nonnull final ColumnStatistics v_columnStatistics) {
        // Combine common statistics
		combineCommon(v_columnStatistics);

        // Combine timestamp-specific statistics
		TimestampColumnStatistics vTimestamp_columnStatistics = (TimestampColumnStatistics) v_columnStatistics;

        if (maxTimestamp == null || (vTimestamp_columnStatistics.maxTimestamp != null && maxTimestamp.before(vTimestamp_columnStatistics.maxTimestamp))) {
            maxTimestamp = vTimestamp_columnStatistics.maxTimestamp;
        }
		if (minTimestamp == null || (vTimestamp_columnStatistics.minTimestamp != null && minTimestamp.after(vTimestamp_columnStatistics.minTimestamp))) {
			minTimestamp = vTimestamp_columnStatistics.minTimestamp;
		}
	}

	/**
	 * Returns the statistics as a string.
     *
     * @return the statistics
	 */
    @Nonnull
	@Override
	public String getVerboseStatistics() {
		return "{\n" + getVerboseStatisticsCommon() + "\n"
               + "TimestampColumnStatistics [maxTimestamp=" + (maxTimestamp != null ? maxTimestamp : "") + ", minTimestamp=" + (minTimestamp != null ? minTimestamp : "") + "]\n}";
	}

	/**
     * Writes the statistics to an output table.
	 */
	@Override
	public void writeStatistics() {
        // Write common statistics
		writeStatisticsCommon();

        // Write timestamp-specific statistics
		rows = new ArrayList<>();
		rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MAX_TIMESTAMP), (maxTimestamp != null) ? maxTimestamp.toString() : ""));
		rows.add(new OutputRow(columnField.name(), String.valueOf(MetricType.MIN_TIMESTAMP), (minTimestamp != null) ? minTimestamp.toString() : ""));
		outputWriter.addRows(rows);
	}

	/**
	 * Get latest timestamp
	 * @return latest timestamp
	 */
    @Nullable
	public Timestamp getMaxTimestamp() {
		return maxTimestamp;
	}

	/**
	 * Get earliest timestamp
	 * @return earliest timestamp
	 */
    @Nullable
	public Timestamp getMinTimestamp() {
		return minTimestamp;
	}
}
