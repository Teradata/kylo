/**
 * Class to represent a row in profile statistics output<br>
 * Format of output:<br>
 *
 * ColumnName, MetricType, MetricValue
 */
export interface ProfileOutputRow {

    /**
     * Column name
     */
    columnName: string;

    /**
     * Metric type
     */
    metricType: string;

    /**
     * Metric value
     */
    metricValue: number;
}
