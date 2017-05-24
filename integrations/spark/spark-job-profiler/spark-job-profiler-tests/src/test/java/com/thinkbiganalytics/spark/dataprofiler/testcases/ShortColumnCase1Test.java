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

import com.thinkbiganalytics.spark.dataprofiler.columns.ShortColumnStatistics;
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
 * Short Column Statistics Test Case
 */

public class ShortColumnCase1Test extends ProfilerTest {

    private static StandardColumnStatistics columnStats;
    private static long nullCount;
    private static long totalCount;
    private static long uniqueCount;
    private static double percNullValues;
    private static double percUniqueValues;
    private static double percDuplicateValues;
    private static TopNDataList topNValues;
    private static short max;
    private static short min;
    private static long sum;
    private static double mean;
    private static double stddev;
    private static double variance;

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for ShortColumnCase1Test ***");
    }

    @Before
    public void setUp() {
        super.setUp();

        columnStats = columnStatsMap.get(11);        //credits
        nullCount = 2L;
        totalCount = 10L;
        uniqueCount = 6L;
        percNullValues = 20.0d;
        percUniqueValues = 60.0d;
        percDuplicateValues = 40.0d;
        topNValues = columnStats.getTopNValues();
        max = (short) 5000;
        min = (short) 0;
        sum = 8600L;
        mean = 1075d;
        stddev = 1579.359047d;
        variance = 2494375d;

    }

    @Test
    public void testShortNullCount() {
        Assert.assertEquals(nullCount, columnStats.getNullCount());
    }

    @Test
    public void testShortTotalCount() {
        Assert.assertEquals(totalCount, columnStats.getTotalCount());
    }

    @Test
    public void testShortUniqueCount() {
        Assert.assertEquals(uniqueCount, columnStats.getUniqueCount());
    }

    @Test
    public void testShortPercNullValues() {
        assertEquals(percNullValues, columnStats.getPercNullValues(), epsilon);
    }

    @Test
    public void testShortPercUniqueValues() {
        assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), epsilon);
    }

    @Test
    public void testShortPercDuplicateValues() {
        assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), epsilon);
    }

    @Test
    public void testShortTopNValues() {
        TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
        Iterator<TopNDataItem> iterator = items.descendingIterator();

        //Verify that there are 3 items
        Assert.assertEquals(3, items.size());

        //Verify the top 3 item counts
        int index = 1;
        while (iterator.hasNext()) {
            TopNDataItem item = iterator.next();
            if (index == 1) {
                Assert.assertEquals((short) 100, item.getValue());
                Assert.assertEquals(Long.valueOf(3L), item.getCount());
            } else if (index == 2) {
                // tie for count 2
                Assert.assertThat(String.valueOf(item.getValue()),
                                  Matchers.anyOf(Matchers.is(String.valueOf((short) 1400)), Matchers.is("null")));
                Assert.assertEquals(Long.valueOf(2L), item.getCount());
            } else if (index == 3) {
                // tie for count 2
                Assert.assertThat(String.valueOf(item.getValue()),
                                  Matchers.anyOf(Matchers.is(String.valueOf((short) 1400)), Matchers.is("null")));
                Assert.assertEquals(Long.valueOf(2L), item.getCount());
            }

            index++;
        }
    }

    @Test
    public void testShortMax() {
        Assert.assertEquals(max, ((ShortColumnStatistics) columnStats).getMax());
    }

    @Test
    public void testShortMin() {
        Assert.assertEquals(min, ((ShortColumnStatistics) columnStats).getMin());
    }

    @Test
    public void testShortSum() {
        Assert.assertEquals(sum, ((ShortColumnStatistics) columnStats).getSum());
    }

    @Test
    public void testShortMean() {
        assertEquals(mean, ((ShortColumnStatistics) columnStats).getMean(), epsilon);
    }

    @Test
    public void testShortStddev() {
        assertEquals(stddev, ((ShortColumnStatistics) columnStats).getStddev(), epsilon);
    }

    @Test
    public void testShortVariance() {
        assertEquals(variance, ((ShortColumnStatistics) columnStats).getVariance(), epsilon);
    }
}   
