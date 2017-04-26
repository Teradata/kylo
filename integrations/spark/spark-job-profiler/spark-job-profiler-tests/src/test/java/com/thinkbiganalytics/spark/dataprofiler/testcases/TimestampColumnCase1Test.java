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

import com.thinkbiganalytics.spark.dataprofiler.columns.StandardColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.TimestampColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;


/**
 * Timestamp Column Statistics Test Case
 */
public class TimestampColumnCase1Test extends ProfilerTest {

    private static StandardColumnStatistics columnStats;
    private static long nullCount;
    private static long totalCount;
    private static long uniqueCount;
    private static double percNullValues;
    private static double percUniqueValues;
    private static double percDuplicateValues;
    private static TopNDataList topNValues;
    private static Timestamp maxTimestamp;
    private static Timestamp minTimestamp;

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for TimestampColumnCase1Test ***");
    }

    @Before
    public void setUp() {
        super.setUp();

        columnStats = columnStatsMap.get(8);        //lastlogin
        nullCount = 0L;
        totalCount = 10L;
        uniqueCount = 5L;
        percNullValues = 0.0d;
        percUniqueValues = 50.0d;
        percDuplicateValues = 50.0d;
        topNValues = columnStats.getTopNValues();
        maxTimestamp = Timestamp.valueOf("2016-01-14 14:20:20");
        minTimestamp = Timestamp.valueOf("2007-03-16 08:24:31");
    }

    @Test
    public void testTimestampNullCount() {
        Assert.assertEquals(nullCount, columnStats.getNullCount());
    }

    @Test
    public void testTimestampTotalCount() {
        Assert.assertEquals(totalCount, columnStats.getTotalCount());
    }

    @Test
    public void testTimestampUniqueCount() {
        Assert.assertEquals(uniqueCount, columnStats.getUniqueCount());
    }

    @Test
    public void testTimestampPercNullValues() {
        assertEquals(percNullValues, columnStats.getPercNullValues(), epsilon);
    }

    @Test
    public void testTimestampPercUniqueValues() {
        assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), epsilon);
    }

    @Test
    public void testTimestampPercDuplicateValues() {
        assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), epsilon);
    }

    @Test
    public void testTimestampTopNValues() {
        TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
        Iterator<TopNDataItem> iterator = items.descendingIterator();

        //Verify that there are 3 items
        Assert.assertEquals(3, items.size());

        //Verify the top 3 item counts
        int index = 1;
        while (iterator.hasNext()) {
            TopNDataItem item = iterator.next();
            if (index == 1) {
                Assert.assertEquals(Timestamp.valueOf("2008-05-06 23:10:10"), item.getValue());
                Assert.assertEquals(Long.valueOf(4L), item.getCount());
            } else if (index == 2) {
                Assert.assertEquals(Timestamp.valueOf("2011-01-08 11:25:45"), item.getValue());
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
    public void testTimestampMaxTimestamp() {
        Assert.assertEquals(maxTimestamp, ((TimestampColumnStatistics) columnStats).getMaxTimestamp());
    }

    @Test
    public void testTimestampMinTimestamp() {
        Assert.assertEquals(minTimestamp, ((TimestampColumnStatistics) columnStats).getMinTimestamp());
    }

}
