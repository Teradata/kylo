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

import com.thinkbiganalytics.spark.dataprofiler.columns.DoubleColumnStatistics;
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
 * Double Column Statistics Test Case
 */
public class DoubleColumnCase1Test extends ProfilerTest {

    private static StandardColumnStatistics columnStats;
    private static long nullCount;
    private static long totalCount;
    private static long uniqueCount;
    private static double percNullValues;
    private static double percUniqueValues;
    private static double percDuplicateValues;
    private static TopNDataList topNValues;
    private static double max;
    private static double min;
    private static double sum;
    private static double mean;
    private static double stddev;
    private static double variance;

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for DoubleColumnCase1Test ***");
    }

    @Before
    public void setUp() {
        super.setUp();

        columnStats = columnStatsMap.get(5);        //height
        nullCount = 3L;
        totalCount = 10L;
        uniqueCount = 5L;
        percNullValues = 30.0d;
        percUniqueValues = 50.0d;
        percDuplicateValues = 50.0d;
        topNValues = columnStats.getTopNValues();
        max = 6.22d;
        min = 4.37d;
        sum = 38.87d;
        mean = 5.552857143d;
        stddev = 0.615298169d;
        variance = 0.378591837d;
    }

    @Test
    public void testDoubleNullCount() {
        Assert.assertEquals(nullCount, columnStats.getNullCount());
    }

    @Test
    public void testDoubleTotalCount() {
        Assert.assertEquals(totalCount, columnStats.getTotalCount());
    }

    @Test
    public void testDoubleUniqueCount() {
        Assert.assertEquals(uniqueCount, columnStats.getUniqueCount());
    }

    @Test
    public void testDoublePercNullValues() {
        assertEquals(percNullValues, columnStats.getPercNullValues(), epsilon);
    }

    @Test
    public void testDoublePercUniqueValues() {
        assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), epsilon);
    }

    @Test
    public void testDoublePercDuplicateValues() {
        assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), epsilon);
    }

    @Test
    public void testDoubleTopNValues() {
        TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
        Iterator<TopNDataItem> iterator = items.descendingIterator();

        //Verify that there are 3 items
        Assert.assertEquals(3, items.size());

        //Verify the top 3 item counts
        int index = 1;
        while (iterator.hasNext()) {
            TopNDataItem item = iterator.next();
            if (index == 1) {
                Assert.assertEquals(5.85d, item.getValue());
                Assert.assertEquals(Long.valueOf(4L), item.getCount());
            } else if (index == 2) {
                Assert.assertEquals(null, item.getValue());
                Assert.assertEquals(Long.valueOf(3L), item.getCount());
            } else if (index == 3) {
                                /*
                    Not checking value since it can be arbitrary.
                    All remaining values have count 1
                */
                Assert.assertEquals(Long.valueOf(1L), item.getCount());
            }

            index++;
        }
    }

    @Test
    public void testDoubleMax() {
        assertEquals(max, ((DoubleColumnStatistics) columnStats).getMax(), epsilon);
    }

    @Test
    public void testDoubleMin() {
        assertEquals(min, ((DoubleColumnStatistics) columnStats).getMin(), epsilon);
    }

    @Test
    public void testDoubleSum() {
        assertEquals(sum, ((DoubleColumnStatistics) columnStats).getSum(), epsilon);
    }

    @Test
    public void testDoubleMean() {
        assertEquals(mean, ((DoubleColumnStatistics) columnStats).getMean(), epsilon);
    }

    @Test
    public void testDoubleStddev() {
        assertEquals(stddev, ((DoubleColumnStatistics) columnStats).getStddev(), epsilon);
    }

    @Test
    public void testDoubleVariance() {
        assertEquals(variance, ((DoubleColumnStatistics) columnStats).getVariance(), epsilon);
    }
}   
