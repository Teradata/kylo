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

import com.thinkbiganalytics.spark.dataprofiler.columns.IntegerColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.StandardColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;


/**
 * Integer Column Statistics Test Case
 */
public class IntegerColumnCase1Test extends ProfilerTest {

    private static StandardColumnStatistics columnStats;
    private static long nullCount;
    private static long totalCount;
    private static long uniqueCount;
    private static double percNullValues;
    private static double percUniqueValues;
    private static double percDuplicateValues;
    private static TopNDataList topNValues;
    private static int max;
    private static int min;
    private static long sum;
    private static double mean;
    private static double stddev;
    private static double variance;

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for IntegerColumnCase1Test ***");
    }

    @Before
    public void setUp() {
        super.setUp();

        columnStats = columnStatsMap.get(0);        //id
        nullCount = 0L;
        totalCount = 10L;
        uniqueCount = 10L;
        percNullValues = 0.0d;
        percUniqueValues = 100.0d;
        percDuplicateValues = 0.0d;
        topNValues = columnStats.getTopNValues();
        max = 10;
        min = 1;
        sum = 55L;
        mean = 5.5d;
        stddev = 2.872281323d;
        variance = 8.25d;

    }

    @Test
    public void testIntegerNullCount() {
        Assert.assertEquals(nullCount, columnStats.getNullCount());
    }

    @Test
    public void testIntegerTotalCount() {
        Assert.assertEquals(totalCount, columnStats.getTotalCount());
    }

    @Test
    public void testIntegerUniqueCount() {
        Assert.assertEquals(uniqueCount, columnStats.getUniqueCount());
    }

    @Test
    public void testIntegerPercNullValues() {
        assertEquals(percNullValues, columnStats.getPercNullValues(), epsilon);
    }

    @Test
    public void testIntegerPercUniqueValues() {
        assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), epsilon);
    }

    @Test
    public void testIntegerPercDuplicateValues() {
        assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), epsilon);
    }

    @Test
    public void testIntegerTopNValues() {
        TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
        Iterator<TopNDataItem> iterator = items.descendingIterator();

        /* Verify that there are 3 items */
        Assert.assertEquals(3, items.size());

        /*
            Verify the top 3 item counts
            Not checking values since they can be arbitrary
            All values have count 1 for this column
         */

        int index = 1;
        while (iterator.hasNext()) {
            TopNDataItem item = iterator.next();
            if (index == 1) {
                Assert.assertEquals(Long.valueOf(1L), item.getCount());
            } else if (index == 2) {
                Assert.assertEquals(Long.valueOf(1L), item.getCount());
            } else if (index == 3) {
                Assert.assertEquals(Long.valueOf(1L), item.getCount());
            }

            index++;
        }
    }

    @Test
    public void testIntegerMax() {
        Assert.assertEquals(max, ((IntegerColumnStatistics) columnStats).getMax());
    }

    @Test
    public void testIntegerMin() {
        Assert.assertEquals(min, ((IntegerColumnStatistics) columnStats).getMin());
    }

    @Test
    public void testIntegerSum() {
        Assert.assertEquals(sum, ((IntegerColumnStatistics) columnStats).getSum());
    }

    @Test
    public void testIntegerMean() {
        assertEquals(mean, ((IntegerColumnStatistics) columnStats).getMean(), epsilon);
    }

    @Test
    public void testIntegerStddev() {
        assertEquals(stddev, ((IntegerColumnStatistics) columnStats).getStddev(), epsilon);
    }

    @Test
    public void testIntegerVariance() {
        assertEquals(variance, ((IntegerColumnStatistics) columnStats).getVariance(), epsilon);
    }
}    
