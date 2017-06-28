package com.thinkbiganalytics.spark.dataprofiler.testcases;

/*-
 * #%L
 * thinkbig-spark-job-profiler-app
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.spark.dataprofiler.ProfilerConfiguration;
import com.thinkbiganalytics.spark.dataprofiler.columns.TimestampColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;

import org.apache.spark.sql.types.DataTypes;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;

/**
 * Timestamp Column Statistics Test Case
 */
public class TimestampColumnCase2Test extends ProfilerTest {

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\t*** Starting run for TimestampColumnCase2Test ***");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for TimestampColumnCase2Test ***");
    }

    /**
     * Verify accommodating column values.
     */
    @Test
    public void accomodate() {
        final ProfilerConfiguration profilerConfiguration = new ProfilerConfiguration();

        // Test with a null value
        TimestampColumnStatistics stats = new TimestampColumnStatistics(DataTypes.createStructField("ts", DataTypes.TimestampType, true), profilerConfiguration);
        stats.accomodate(null, 1L);
        Assert.assertNull(stats.getMaxTimestamp());
        Assert.assertNull(stats.getMinTimestamp());

        // Test with uninitialized max & min
        stats.accomodate("2016-06-27 14:04:30", 1L);

        Timestamp ts1 = new Timestamp(new DateTime(2016, 6, 27, 14, 4, 30).getMillis());
        Assert.assertEquals(ts1, stats.getMaxTimestamp());
        Assert.assertEquals(ts1, stats.getMinTimestamp());

        // Test with a later timestamp
        stats.accomodate("2016-06-27 14:04:31", 1L);

        Timestamp ts2 = new Timestamp(new DateTime(2016, 6, 27, 14, 4, 31).getMillis());
        Assert.assertEquals(ts2, stats.getMaxTimestamp());
        Assert.assertEquals(ts1, stats.getMinTimestamp());

        // Test with an earlier timestamp
        stats.accomodate("2016-06-27 14:04:29", 1L);

        Timestamp ts3 = new Timestamp(new DateTime(2016, 6, 27, 14, 4, 29).getMillis());
        Assert.assertEquals(ts2, stats.getMaxTimestamp());
        Assert.assertEquals(ts3, stats.getMinTimestamp());
    }

    /**
     * Verify combining statistics.
     */
    @Test
    public void combine() {
        final ProfilerConfiguration profilerConfiguration = new ProfilerConfiguration();

        // Test when 'this' is empty
        TimestampColumnStatistics other = new TimestampColumnStatistics(DataTypes.createStructField("ts", DataTypes.TimestampType, true), profilerConfiguration);
        TimestampColumnStatistics stats = new TimestampColumnStatistics(DataTypes.createStructField("ts", DataTypes.TimestampType, true), profilerConfiguration);
        other.accomodate("2016-06-27 14:04:30", 1L);
        stats.combine(other);

        Timestamp ts1 = new Timestamp(new DateTime(2016, 6, 27, 14, 4, 30).getMillis());
        Assert.assertEquals(ts1, stats.getMaxTimestamp());
        Assert.assertEquals(ts1, stats.getMinTimestamp());

        // Test when other is empty
        other = new TimestampColumnStatistics(DataTypes.createStructField("ts", DataTypes.TimestampType, true), profilerConfiguration);
        stats.combine(other);

        Assert.assertEquals(ts1, stats.getMaxTimestamp());
        Assert.assertEquals(ts1, stats.getMinTimestamp());

        // Test when other has later timestamp
        other.accomodate("2016-06-27 14:04:31", 1L);
        stats.combine(other);

        Timestamp ts2 = new Timestamp(new DateTime(2016, 6, 27, 14, 4, 31).getMillis());
        Assert.assertEquals(ts2, stats.getMaxTimestamp());
        Assert.assertEquals(ts1, stats.getMinTimestamp());

        // Test when other has earlier timestamp
        other.accomodate("2016-06-27 14:04:29", 1L);
        stats.combine(other);

        Timestamp ts3 = new Timestamp(new DateTime(2016, 6, 27, 14, 4, 29).getMillis());
        Assert.assertEquals(ts2, stats.getMaxTimestamp());
        Assert.assertEquals(ts3, stats.getMinTimestamp());
    }

    /**
     * Verify statistics string.
     */
    @Test
    public void getVerboseStatistics() {
        final ProfilerConfiguration profilerConfiguration = new ProfilerConfiguration();

        // Test when empty
        TimestampColumnStatistics stats = new TimestampColumnStatistics(DataTypes.createStructField("ts", DataTypes.TimestampType, true), profilerConfiguration);

        String expected = "{\nColumnInfo [name=ts, datatype=timestamp, nullable=true, metadata={}]\n"
                          + "CommonStatistics [nullCount=0, totalCount=0, uniqueCount=0, percNullValues=0, percUniqueValues=0, percDuplicateValues=0]\n"
                          + "Top 3 values [\n]\n"
                          + "TimestampColumnStatistics [maxTimestamp=, minTimestamp=]\n}";
        Assert.assertEquals(expected, stats.getVerboseStatistics());

        // Test with multiple values
        stats.accomodate("", 1L);
        stats.accomodate("2016-06-27 14:04:29", 1L);
        stats.accomodate("2016-06-27 14:04:30", 1L);
        stats.accomodate("2016-06-27 14:04:31", 1L);
        stats.accomodate(null, 1L);

        expected = "{\nColumnInfo [name=ts, datatype=timestamp, nullable=true, metadata={}]\n"
                   + "CommonStatistics [nullCount=1, totalCount=5, uniqueCount=5, percNullValues=20, percUniqueValues=100, percDuplicateValues=0]\n"
                   + "Top 3 values [\n1^A^A1^B2^A2016-06-27 14:04:29^A1^B3^A2016-06-27 14:04:30^A1^B]\n"
                   + "TimestampColumnStatistics [maxTimestamp=2016-06-27 14:04:31.0, minTimestamp=2016-06-27 14:04:29.0]\n}";
        Assert.assertEquals(expected, stats.getVerboseStatistics());
    }

    /**
     * Verify writing statistics.
     */
    @Test
    public void writeStatistics() {
        final ProfilerConfiguration profilerConfiguration = new ProfilerConfiguration();

        // Test when empty
        TimestampColumnStatistics stats = new TimestampColumnStatistics(DataTypes.createStructField("ts", DataTypes.TimestampType, true), profilerConfiguration);
        List<OutputRow> rows = stats.getStatistics();

        Assert.assertEquals(12, rows.size());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=COLUMN_DATATYPE, metricValue=TimestampType]", rows.get(0).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=COLUMN_NULLABLE, metricValue=true]", rows.get(1).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=COLUMN_METADATA, metricValue={}]", rows.get(2).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=NULL_COUNT, metricValue=0]", rows.get(3).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=TOTAL_COUNT, metricValue=0]", rows.get(4).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=UNIQUE_COUNT, metricValue=0]", rows.get(5).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=PERC_NULL_VALUES, metricValue=0]", rows.get(6).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=PERC_UNIQUE_VALUES, metricValue=0]", rows.get(7).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=PERC_DUPLICATE_VALUES, metricValue=0]", rows.get(8).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=TOP_N_VALUES, metricValue=]", rows.get(9).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=MAX_TIMESTAMP, metricValue=]", rows.get(10).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=MIN_TIMESTAMP, metricValue=]", rows.get(11).toString());

        // Test with multiple values
        stats.accomodate("", 1L);
        stats.accomodate("2016-06-27 14:04:29", 1L);
        stats.accomodate("2016-06-27 14:04:30", 1L);
        stats.accomodate("2016-06-27 14:04:31", 1L);
        stats.accomodate(null, 1L);
        rows = stats.getStatistics();

        Assert.assertEquals(12, rows.size());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=COLUMN_DATATYPE, metricValue=TimestampType]", rows.get(0).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=COLUMN_NULLABLE, metricValue=true]", rows.get(1).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=COLUMN_METADATA, metricValue={}]", rows.get(2).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=NULL_COUNT, metricValue=1]", rows.get(3).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=TOTAL_COUNT, metricValue=5]", rows.get(4).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=UNIQUE_COUNT, metricValue=5]", rows.get(5).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=PERC_NULL_VALUES, metricValue=20]", rows.get(6).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=PERC_UNIQUE_VALUES, metricValue=100]", rows.get(7).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=PERC_DUPLICATE_VALUES, metricValue=0]", rows.get(8).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=TOP_N_VALUES, metricValue=1^A^A1^B2^A2016-06-27 14:04:29^A1^B3^A2016-06-27 14:04:30^A1^B]", rows.get(9).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=MAX_TIMESTAMP, metricValue=2016-06-27 14:04:31.0]", rows.get(10).toString());
        Assert.assertEquals("OutputRow [columnName=ts, metricType=MIN_TIMESTAMP, metricValue=2016-06-27 14:04:29.0]", rows.get(11).toString());
    }
}
