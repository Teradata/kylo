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

import java.util.TreeSet;

/**
 * TopN Test Case 2
 */
public class TopNCase2Test extends ProfilerTest {

    private static TreeSet<TopNDataItem> items;
    private static String topNDataItemsString;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\t*** Starting run for TopNCase2Test ***");

        TopNDataList topNList = new TopNDataList(4);

        for (int i = 1; i < 10000000; i++) {
            topNList.add("Item" + i, (long) (i % 5000000));
        }

        items = topNList.getTopNDataItemsForColumn();
        topNDataItemsString = topNList.printTopNItems();
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for TopNCase2Test ***");
    }

    @Test
    public void testTopNSummaryCount() {
        Assert.assertEquals(4, items.size());
    }

    @Test
    public void testTopNValuesToWriteString() {
        String expectedRetVal = "1^AItem4999999^A4999999^B"
                                + "2^AItem9999999^A4999999^B"
                                + "3^AItem4999998^A4999998^B"
                                + "4^AItem9999998^A4999998^B";

        Assert.assertEquals(expectedRetVal, topNDataItemsString);
    }

}
