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

import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * TopN Test Case 1
 */
public class TopNCase1Test extends ProfilerTest {

    private static TreeSet<TopNDataItem> items;
    private static Iterator<TopNDataItem> iterator;
    private static String topNDataItemsString;

    @BeforeClass
    public static void setUpClass() {

        System.out.println("\t*** Starting run for TopNCase1Test ***");

        TopNDataList topNList = new TopNDataList(5);

        topNList.add("A", 18L);
        topNList.add("B", 20L);
        topNList.add("C", 15L);
        topNList.add("D", 25L);
        topNList.add("E", 19L);

        items = topNList.getTopNDataItemsForColumn();
        iterator = items.descendingIterator();
        topNDataItemsString = topNList.printTopNItems();

    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for TopNCase1Test ***");
    }

    @Test
    public void testTopNSummaryCount() {

        Assert.assertEquals(5, items.size());
    }

    @Test
    public void testTopNValues() {

        int index = 1;
        while (iterator.hasNext()) {
            TopNDataItem item = iterator.next();

            if (index == 1) {
                Assert.assertEquals("D", item.getValue());
                Assert.assertEquals(Long.valueOf(25), item.getCount());
            } else if (index == 2) {
                Assert.assertEquals("B", item.getValue());
                Assert.assertEquals(Long.valueOf(20), item.getCount());
            } else if (index == 3) {
                Assert.assertEquals("E", item.getValue());
                Assert.assertEquals(Long.valueOf(19), item.getCount());
            } else if (index == 4) {
                Assert.assertEquals("A", item.getValue());
                Assert.assertEquals(Long.valueOf(18), item.getCount());
            } else if (index == 5) {
                Assert.assertEquals("C", item.getValue());
                Assert.assertEquals(Long.valueOf(15), item.getCount());
            }

            index++;
        }
    }

    @Test
    public void testTopNValuesToWriteString() {
        String expectedRetVal = "1^AD^A25^B" +
                                "2^AB^A20^B" +
                                "3^AE^A19^B" +
                                "4^AA^A18^B" +
                                "5^AC^A15^B";

        Assert.assertEquals(expectedRetVal, topNDataItemsString);
    }
}
