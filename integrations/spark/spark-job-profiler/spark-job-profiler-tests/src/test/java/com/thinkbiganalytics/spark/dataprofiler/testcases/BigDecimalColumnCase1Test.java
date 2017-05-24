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

import com.thinkbiganalytics.spark.dataprofiler.columns.BigDecimalColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.StandardColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

/**
 * Big Decimal Column Statistics Test Case
 */
public class BigDecimalColumnCase1Test extends ProfilerTest {

    private static StandardColumnStatistics columnStats;
    private static long nullCount;
    private static long totalCount;
    private static long uniqueCount;
    private static double percNullValues;
    private static double percUniqueValues;
    private static double percDuplicateValues;
    private static TopNDataList topNValues;
    private static BigDecimal max;
    private static BigDecimal min;
    private static BigDecimal sum;
    private static BigDecimal topFirstValue;
    private static BigDecimal topSecondValue;

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for BigDecimalColumnCase1Test ***");
    }

    @Before
    public void setUp() {
        super.setUp();

        System.out.println("\t*** Starting run for BigDecimalColumnCase1Test ***");
        columnStats = columnStatsMap.get(13);        //id
        nullCount = 2L;
        totalCount = 10L;
        uniqueCount = 4L;
        percNullValues = 20.0d;
        percUniqueValues = 40.0d;
        percDuplicateValues = 60.0d;
        topNValues = columnStats.getTopNValues();
        max = new BigDecimal(String.valueOf(8.223)).setScale(5, BigDecimal.ROUND_HALF_UP);
        min = new BigDecimal(String.valueOf(1.567)).setScale(5, BigDecimal.ROUND_HALF_UP);
        sum = new BigDecimal(String.valueOf(30.296)).setScale(5, BigDecimal.ROUND_HALF_UP);
        topFirstValue = new BigDecimal(String.valueOf(4.343)).setScale(5, BigDecimal.ROUND_HALF_UP);
        topSecondValue = new BigDecimal(String.valueOf(1.567)).setScale(5, BigDecimal.ROUND_HALF_UP);

    }

    @Test
    public void testBigDecimalNullCount() {
        Assert.assertEquals(nullCount, columnStats.getNullCount());
    }

    @Test
    public void testBigDecimalTotalCount() {
        Assert.assertEquals(totalCount, columnStats.getTotalCount());
    }

    @Test
    public void testBigDecimalUniqueCount() {
        Assert.assertEquals(uniqueCount, columnStats.getUniqueCount());
    }

    @Test
    public void testBigDecimalPercNullValues() {
        assertEquals(percNullValues, columnStats.getPercNullValues(), epsilon);
    }

    @Test
    public void testBigDecimalPercUniqueValues() {
        assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), epsilon);
    }

    @Test
    public void testBigDecimalPercDuplicateValues() {
        assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), epsilon);
    }

    @Test
    public void testBigDecimalTopNValues() {
        TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
        Iterator<TopNDataItem> iterator = items.descendingIterator();

        //Verify that there are 3 items
        Assert.assertEquals(3, items.size());

        //Verify the top 3 item counts
        int index = 1;
        while (iterator.hasNext()) {
            TopNDataItem item = iterator.next();
            if (index == 1) {
                Assert.assertEquals(topFirstValue, new BigDecimal(String.valueOf(item.getValue())).setScale(5, BigDecimal.ROUND_HALF_UP));
                Assert.assertEquals(Long.valueOf(4L), item.getCount());
            } else if (index == 2) {
                Assert.assertEquals(topSecondValue, new BigDecimal(String.valueOf(item.getValue())).setScale(5, BigDecimal.ROUND_HALF_UP));
                Assert.assertEquals(Long.valueOf(3L), item.getCount());
            } else if (index == 3) {
                Assert.assertEquals(null, item.getValue());
                Assert.assertEquals(Long.valueOf(2L), item.getCount());
            }
            index++;
        }
    }

    @Test
    public void testBigDecimalMax() {
        Assert.assertEquals(max, ((BigDecimalColumnStatistics) columnStats).getMax().setScale(5, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    public void testBigDecimalMin() {
        Assert.assertEquals(min, ((BigDecimalColumnStatistics) columnStats).getMin().setScale(5, BigDecimal.ROUND_HALF_UP));
    }

    @Test
    public void testBigDecimalSum() {
        Assert.assertEquals(sum, ((BigDecimalColumnStatistics) columnStats).getSum().setScale(5, BigDecimal.ROUND_HALF_UP));
    }
}
