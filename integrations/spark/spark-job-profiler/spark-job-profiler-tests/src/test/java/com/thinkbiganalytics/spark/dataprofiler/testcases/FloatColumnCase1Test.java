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

import com.thinkbiganalytics.spark.dataprofiler.columns.FloatColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.StandardColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

/**
 * Float Column Statistics Test Case
 */
public class FloatColumnCase1Test extends ProfilerTest {

    private static StandardColumnStatistics columnStats;
    private static long nullCount;
    private static long totalCount;
    private static long uniqueCount;
    private static double percNullValues;
    static private double percUniqueValues;
    private static double percDuplicateValues;
    private static TopNDataList topNValues;
    private static float max;
    private static float min;
    private static double sum;
    private static double mean;
    private static double stddev;
    private static double variance;

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for FloatColumnCase1Test ***");
    }

    @Before
    public void setUp() {
        super.setUp();

        columnStats = columnStatsMap.get(10);        //weight
        nullCount = 3L;
        totalCount = 10L;
        uniqueCount = 6L;
        percNullValues = 30.0d;
        percUniqueValues = 60.0d;
        percDuplicateValues = 40.0d;
        topNValues = columnStats.getTopNValues();
        max = 180.6f;
        min = 40.2f;
        sum = 918.5f;
        mean = 131.2142857f;
        stddev = 44.56079086f;
        variance = 1985.664082f;

    }

    @Test
    public void testFloatNullCount() {
        Assert.assertEquals(nullCount, columnStats.getNullCount());
    }

    @Test
    public void testFloatTotalCount() {
        Assert.assertEquals(totalCount, columnStats.getTotalCount());
    }

    @Test
    public void testFloatUniqueCount() {
        Assert.assertEquals(uniqueCount, columnStats.getUniqueCount());
    }

    @Test
    public void testFloatPercNullValues() {
        assertEquals(percNullValues, columnStats.getPercNullValues(), epsilon);
    }

    @Test
    public void testFloatPercUniqueValues() {
        assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), epsilon);
    }

    @Test
    public void testFloatPercDuplicateValues() {
        assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), epsilon);
    }

    @Test
    public void testFloatTopNValues() {
        TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
        Iterator<TopNDataItem> iterator = items.descendingIterator();

        //Verify that there are 3 items
        Assert.assertEquals(3, items.size());

        //Verify the top 3 item counts
        int index = 1;
        while (iterator.hasNext()) {
            TopNDataItem item = iterator.next();
            if (index == 1) {
                Assert.assertEquals(null, item.getValue());
                Assert.assertEquals(Long.valueOf(3L), item.getCount());
            } else if (index == 2) {
                // A tie for count 2
                Assert.assertThat(Float.valueOf(item.getValue().toString()),
                                  Matchers.anyOf(Matchers.is(110.5f), Matchers.is(160.7f)));
                Assert.assertEquals(Long.valueOf(2L), item.getCount());
            } else if (index == 3) {
                // A tie for count 2
                Assert.assertThat(Float.valueOf(item.getValue().toString()),
                                  Matchers.anyOf(Matchers.is(110.5f), Matchers.is(160.7f)));
                Assert.assertEquals(Long.valueOf(2L), item.getCount());
            }

            index++;
        }
    }

    @Test
    public void testFloatMax() {
        assertEquals(max, ((FloatColumnStatistics) columnStats).getMax(), epsilon);
    }

    @Test
    public void testFloatMin() {
        assertEquals(min, ((FloatColumnStatistics) columnStats).getMin(), epsilon);
    }

    @Test
    public void testFloatSum() {
        assertEquals(sum, ((FloatColumnStatistics) columnStats).getSum(), epsilon);
    }

    @Test
    public void testFloatMean() {
        assertEquals(mean, ((FloatColumnStatistics) columnStats).getMean(), epsilon);
    }

    @Test
    public void testFloatStddev() {
        assertEquals(stddev, ((FloatColumnStatistics) columnStats).getStddev(), epsilon);
    }

    @Test
    public void testFloatVariance() {
        assertEquals(variance, ((FloatColumnStatistics) columnStats).getVariance(), epsilon);
    }
}   
