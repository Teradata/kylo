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
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TopN Test Case 3
 */
/* This case provides insight into the operation of the topN data structure */
public class TopNCase3Test extends ProfilerTest {

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\t*** Starting run for TopNCase3Test ***");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for TopNCase3Test ***");
    }

    @Test
    public void testTopNValuesToWriteString01() {
        TopNDataList topNList = new TopNDataList(3);
        topNList.add(40.2f, 1L);
        Assert.assertEquals(topNList.getTopNDataItemsForColumn().size(), 1);
        String expectedRetVal = "1^A40.2^A1^B";
        Assert.assertEquals(expectedRetVal, topNList.printTopNItems());
    }

    @Test
    public void testTopNValuesToWriteString02() {
        TopNDataList topNList = new TopNDataList(3);
        topNList.add(40.2f, 1L);
        topNList.add(160.7f, 2L);
        Assert.assertEquals(topNList.getTopNDataItemsForColumn().size(), 2);
        String expectedRetVal = "1^A160.7^A2^B2^A40.2^A1^B";
        Assert.assertEquals(expectedRetVal, topNList.printTopNItems());
    }

    @Test
    public void testTopNValuesToWriteString03() {
        TopNDataList topNList = new TopNDataList(3);
        topNList.add(40.2f, 1L);
        topNList.add(160.7f, 2L);
        topNList.add(170.5f, 2L);
        Assert.assertEquals(topNList.getTopNDataItemsForColumn().size(), 3);
        String expectedRetVal = "1^A160.7^A2^B2^A170.5^A2^B3^A40.2^A1^B";
        Assert.assertEquals(expectedRetVal, topNList.printTopNItems());
    }

    @Test
    public void testTopNValuesToWriteString04() {
        TopNDataList topNList = new TopNDataList(3);
        topNList.add(40.2f, 1L);
        topNList.add(160.7f, 2L);
        topNList.add(170.5f, 2L);
        topNList.add(null, 3L);
        Assert.assertEquals(topNList.getTopNDataItemsForColumn().size(), 3);
        String expectedRetVal = "1^Anull^A3^B2^A160.7^A2^B3^A170.5^A2^B";
        Assert.assertEquals(expectedRetVal, topNList.printTopNItems());
    }

    @Test
    public void testTopNValuesToWriteString05() {
        TopNDataList topNList = new TopNDataList(3);
        topNList.add(40.2f, 1L);
        topNList.add(160.7f, 2L);
        topNList.add(170.5f, 2L);
        topNList.add(null, 3L);
        topNList.add(155.3f, 1L);
        Assert.assertEquals(topNList.getTopNDataItemsForColumn().size(), 3);
        String expectedRetVal = "1^Anull^A3^B2^A160.7^A2^B3^A170.5^A2^B";
        Assert.assertEquals(expectedRetVal, topNList.printTopNItems());
    }

    @Test
    public void testTopNValuesToWriteString06() {
        TopNDataList topNList = new TopNDataList(3);
        topNList.add(40.2f, 1L);
        topNList.add(160.7f, 2L);
        topNList.add(170.5f, 2L);
        topNList.add(null, 3L);
        topNList.add(155.3f, 1L);
        topNList.add(10.6f, 4L);
        Assert.assertEquals(topNList.getTopNDataItemsForColumn().size(), 3);
        String expectedRetVal = "1^A10.6^A4^B2^Anull^A3^B3^A160.7^A2^B";
        Assert.assertEquals(expectedRetVal, topNList.printTopNItems());
    }

    @Test
    public void testTopNValuesToWriteString07() {
        TopNDataList topNList = new TopNDataList(3);
        topNList.add(40.2f, 1L);
        topNList.add(160.7f, 2L);
        topNList.add(170.5f, 2L);
        topNList.add(null, 3L);
        topNList.add(155.3f, 1L);
        topNList.add(10.6f, 4L);
        topNList.add(15.0f, 3L);
        Assert.assertEquals(topNList.getTopNDataItemsForColumn().size(), 3);
        String expectedRetVal = "1^A10.6^A4^B2^A15.0^A3^B3^Anull^A3^B";
        Assert.assertEquals(expectedRetVal, topNList.printTopNItems());
    }
}
