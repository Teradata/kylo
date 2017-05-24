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

import com.thinkbiganalytics.spark.dataprofiler.columns.BooleanColumnStatistics;
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
 * Boolean Column Statistics Test Case
 */
public class BooleanColumnCase1Test extends ProfilerTest {

    private static StandardColumnStatistics columnStats;
    private static long nullCount;
    private static long totalCount;
    private static long uniqueCount;
    private static double percNullValues;
    private static double percUniqueValues;
    private static double percDuplicateValues;
    private static TopNDataList topNValues;
    private static long trueCount;
    private static long falseCount;

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for BooleanColumnCase1Test ***");
    }

    @Before
    public void setUp() {
        super.setUp();

        columnStats = columnStatsMap.get(7);        //lifemember
        nullCount = 2L;
        totalCount = 10L;
        uniqueCount = 3L;
        percNullValues = 20.0d;
        percUniqueValues = 30.0d;
        percDuplicateValues = 70.0d;
        topNValues = columnStats.getTopNValues();
        trueCount = 5L;
        falseCount = 3L;
    }

    @Test
    public void testBooleanNullCount() {
        Assert.assertEquals(nullCount, columnStats.getNullCount());
    }

    @Test
    public void testBooleanTotalCount() {
        Assert.assertEquals(totalCount, columnStats.getTotalCount());
    }

    @Test
    public void testBooleanUniqueCount() {
        Assert.assertEquals(uniqueCount, columnStats.getUniqueCount());
    }

    @Test
    public void testBooleanPercNullValues() {
        assertEquals(percNullValues, columnStats.getPercNullValues(), epsilon);
    }

    @Test
    public void testBooleanPercUniqueValues() {
        assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), epsilon);
    }

    @Test
    public void testBooleanPercDuplicateValues() {
        assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), epsilon);
    }

    @Test
    public void testBooleanTopNValues() {
        TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
        Iterator<TopNDataItem> iterator = items.descendingIterator();

        //Verify that there are 3 items
        Assert.assertEquals(3, items.size());

        //Verify the top 3 item counts
        int index = 1;
        while (iterator.hasNext()) {
            TopNDataItem item = iterator.next();
            if (index == 1) {
                Assert.assertEquals(Boolean.TRUE, item.getValue());
                Assert.assertEquals(Long.valueOf(5L), item.getCount());
            } else if (index == 2) {
                Assert.assertEquals(Boolean.FALSE, item.getValue());
                Assert.assertEquals(Long.valueOf(3L), item.getCount());
            } else if (index == 3) {
                Assert.assertEquals(null, item.getValue());
                Assert.assertEquals(Long.valueOf(2L), item.getCount());
            }

            index++;
        }
    }

    @Test
    public void testBooleanTrueCount() {
        Assert.assertEquals(trueCount, ((BooleanColumnStatistics) columnStats).getTrueCount());
    }

    @Test
    public void testBooleanFalseCount() {
        Assert.assertEquals(falseCount, ((BooleanColumnStatistics) columnStats).getFalseCount());
    }
}
