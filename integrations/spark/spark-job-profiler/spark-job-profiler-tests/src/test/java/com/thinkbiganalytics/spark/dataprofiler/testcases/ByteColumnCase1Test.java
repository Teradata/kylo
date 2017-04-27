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

import com.thinkbiganalytics.spark.dataprofiler.columns.ByteColumnStatistics;
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
 * Byte Column Statistics Test Case
 */
public class ByteColumnCase1Test extends ProfilerTest {

    private static StandardColumnStatistics columnStats;
    private static long nullCount;
    private static long totalCount;
    private static long uniqueCount;
    private static double percNullValues;
    private static double percUniqueValues;
    private static double percDuplicateValues;
    private static TopNDataList topNValues;
    private static byte max;
    private static byte min;
    private static long sum;
    private static double mean;
    private static double stddev;
    private static double variance;

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for ByteColumnCase1Test ***");
    }

    @Before
    public void setUp() {
        super.setUp();

        columnStats = columnStatsMap.get(12);        //ccode
        nullCount = 3L;
        totalCount = 10L;
        uniqueCount = 4L;
        percNullValues = 30.0d;
        percUniqueValues = 40.0d;
        percDuplicateValues = 60.0d;
        topNValues = columnStats.getTopNValues();
        max = (byte) 99;
        min = (byte) 2;
        sum = 537L;
        mean = 76.71428571d;
        stddev = 36.67118092d;
        variance = 1344.77551d;
    }

    @Test
    public void testByteNullCount() {
        Assert.assertEquals(nullCount, columnStats.getNullCount());
    }

    @Test
    public void testByteTotalCount() {
        Assert.assertEquals(totalCount, columnStats.getTotalCount());
    }

    @Test
    public void testByteUniqueCount() {
        Assert.assertEquals(uniqueCount, columnStats.getUniqueCount());
    }

    @Test
    public void testBytePercNullValues() {
        assertEquals(percNullValues, columnStats.getPercNullValues(), epsilon);
    }

    @Test
    public void testBytePercUniqueValues() {
        assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), epsilon);
    }

    @Test
    public void testBytePercDuplicateValues() {
        assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), epsilon);
    }

    @Test
    public void testByteTopNValues() {
        TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
        Iterator<TopNDataItem> iterator = items.descendingIterator();

        //Verify that there are 3 items
        Assert.assertEquals(3, items.size());

        //Verify the top 3 item counts
        int index = 1;
        while (iterator.hasNext()) {
            TopNDataItem item = iterator.next();
            if (index == 1) {
                Assert.assertEquals((byte) 99, item.getValue());
                Assert.assertEquals(Long.valueOf(5L), item.getCount());
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
    public void testByteMax() {
        Assert.assertEquals(max, ((ByteColumnStatistics) columnStats).getMax());
    }

    @Test
    public void testByteMin() {
        Assert.assertEquals(min, ((ByteColumnStatistics) columnStats).getMin());
    }

    @Test
    public void testByteSum() {
        Assert.assertEquals(sum, ((ByteColumnStatistics) columnStats).getSum());
    }

    @Test
    public void testByteMean() {
        assertEquals(mean, ((ByteColumnStatistics) columnStats).getMean(), epsilon);
    }

    @Test
    public void testByteStddev() {
        assertEquals(stddev, ((ByteColumnStatistics) columnStats).getStddev(), epsilon);
    }

    @Test
    public void testByteVariance() {
        assertEquals(variance, ((ByteColumnStatistics) columnStats).getVariance(), epsilon);
    }
}    
