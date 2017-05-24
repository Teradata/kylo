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
import com.thinkbiganalytics.spark.dataprofiler.columns.StringColumnStatistics;
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
 * String Column Statistics Test Case
 */
public class StringColumnCase2Test extends ProfilerTest {

    private static StandardColumnStatistics columnStats;
    private static long nullCount;
    private static long totalCount;
    private static long uniqueCount;
    private static double percNullValues;
    private static double percUniqueValues;
    private static double percDuplicateValues;
    private static TopNDataList topNValues;
    private static int maxLength;
    private static int minLength;
    private static String longestString;
    private static String shortestString;
    private static long emptyCount;
    private static double percEmptyValues;
    private static String minStringCase;
    private static String maxStringCase;
    private static String minStringICase;
    private static String maxStringICase;

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for StringColumnCase2Test ***");
    }

    @Before
    public void setUp() {
        super.setUp();

        columnStats = columnStatsMap.get(2);        //lastname
        nullCount = 0L;
        totalCount = 10L;
        uniqueCount = 6L;
        percNullValues = 0.0d;
        percUniqueValues = 60.0d;
        percDuplicateValues = 40.0d;
        topNValues = columnStats.getTopNValues();
        maxLength = 12;
        minLength = 2;
        longestString = "Edmundson Jr";
        shortestString = "Hu";
        emptyCount = 4;
        percEmptyValues = 40.0d;
        minStringCase = "Edmundson Jr";
        maxStringCase = "Wright";
        minStringICase = "Edmundson Jr";
        maxStringICase = "Wright";
    }

    @Test
    public void testStringNullCount() {
        Assert.assertEquals(nullCount, columnStats.getNullCount());
    }

    @Test
    public void testStringTotalCount() {
        Assert.assertEquals(totalCount, columnStats.getTotalCount());
    }

    @Test
    public void testStringUniqueCount() {
        Assert.assertEquals(uniqueCount, columnStats.getUniqueCount());
    }

    @Test
    public void testStringPercNullValues() {
        assertEquals(percNullValues, columnStats.getPercNullValues(), epsilon);
    }

    @Test
    public void testStringPercUniqueValues() {
        assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), epsilon);
    }

    @Test
    public void testStringPercDuplicateValues() {
        assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), epsilon);
    }

    @Test
    public void testStringTopNValues() {
        TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
        Iterator<TopNDataItem> iterator = items.descendingIterator();

        //Verify that there are 3 items
        Assert.assertEquals(3, items.size());

        //Verify the top 3 item counts
        int index = 1;
        while (iterator.hasNext()) {
            TopNDataItem item = iterator.next();
            if (index == 1) {
                assertEquals(EMPTY_STRING, item.getValue());
                Assert.assertEquals(Long.valueOf(4L), item.getCount());
            } else if (index == 2) {
                Assert.assertEquals("Taylor", item.getValue());
                Assert.assertEquals(Long.valueOf(2L), item.getCount());
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
    public void testStringMaxLength() {
        Assert.assertEquals(maxLength, ((StringColumnStatistics) columnStats).getMaxLength());
    }

    @Test
    public void testStringMinLength() {
        Assert.assertEquals(minLength, ((StringColumnStatistics) columnStats).getMinLength());
    }

    @Test
    public void testStringLongestString() {
        Assert.assertEquals(longestString, ((StringColumnStatistics) columnStats).getLongestString());
    }

    @Test
    public void testStringShortestString() {
        Assert.assertEquals(shortestString, ((StringColumnStatistics) columnStats).getShortestString());
    }

    @Test
    public void testStringEmptyCount() {
        Assert.assertEquals(emptyCount, ((StringColumnStatistics) columnStats).getEmptyCount());
    }

    @Test
    public void testStringPercEmptyValues() {
        assertEquals(percEmptyValues, ((StringColumnStatistics) columnStats).getPercEmptyValues(), epsilon);
    }

    @Test
    public void testStringMinStringCaseSensitive() {
        Assert.assertEquals(minStringCase, ((StringColumnStatistics) columnStats).getMinStringCase());
    }

    @Test
    public void testStringMaxStringCaseSensitive() {
        Assert.assertEquals(maxStringCase, ((StringColumnStatistics) columnStats).getMaxStringCase());
    }

    @Test
    public void testStringMinStringCaseInsensitive() {
        Assert.assertEquals(minStringICase, ((StringColumnStatistics) columnStats).getMinStringICase());
    }

    @Test
    public void testStringMaxStringCaseInsensitive() {
        Assert.assertEquals(maxStringICase, ((StringColumnStatistics) columnStats).getMaxStringICase());
    }

}
