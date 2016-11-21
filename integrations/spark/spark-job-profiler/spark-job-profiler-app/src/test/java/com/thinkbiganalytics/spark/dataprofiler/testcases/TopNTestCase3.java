package com.thinkbiganalytics.spark.dataprofiler.testcases;

import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TopN Test Case 3
 * @author jagrut sharma
 */
/* This case provides insight into the operation of the topN data structure */
public class TopNTestCase3 {

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\t*** Starting run for TopNTestCase3 ***");
    }

    @Test
    public void testTopNValuesToWriteString01() {
        TopNDataList topNList = new TopNDataList(3);
        topNList.add(40.2f, 1L);
        assertEquals(topNList.getTopNDataItemsForColumn().size(), 1);
        String expectedRetVal = "1^A40.2^A1^B";
        assertEquals(expectedRetVal, topNList.printTopNItems());
    }

    @Test
    public void testTopNValuesToWriteString02() {
        TopNDataList topNList = new TopNDataList(3);
        topNList.add(40.2f, 1L);
        topNList.add(160.7f, 2L);
        assertEquals(topNList.getTopNDataItemsForColumn().size(), 2);
        String expectedRetVal = "1^A160.7^A2^B2^A40.2^A1^B";
        assertEquals(expectedRetVal, topNList.printTopNItems());
    }

    @Test
    public void testTopNValuesToWriteString03() {
        TopNDataList topNList = new TopNDataList(3);
        topNList.add(40.2f, 1L);
        topNList.add(160.7f, 2L);
        topNList.add(170.5f, 2L);
        assertEquals(topNList.getTopNDataItemsForColumn().size(), 3);
        String expectedRetVal = "1^A160.7^A2^B2^A170.5^A2^B3^A40.2^A1^B";
        assertEquals(expectedRetVal, topNList.printTopNItems());
    }


    @Test
    public void testTopNValuesToWriteString04() {
        TopNDataList topNList = new TopNDataList(3);
        topNList.add(40.2f, 1L);
        topNList.add(160.7f, 2L);
        topNList.add(170.5f, 2L);
        topNList.add(null, 3L);
        assertEquals(topNList.getTopNDataItemsForColumn().size(), 3);
        String expectedRetVal = "1^Anull^A3^B2^A160.7^A2^B3^A170.5^A2^B";
        assertEquals(expectedRetVal, topNList.printTopNItems());
    }

    @Test
    public void testTopNValuesToWriteString05() {
        TopNDataList topNList = new TopNDataList(3);
        topNList.add(40.2f, 1L);
        topNList.add(160.7f, 2L);
        topNList.add(170.5f, 2L);
        topNList.add(null, 3L);
        topNList.add(155.3f, 1L);
        assertEquals(topNList.getTopNDataItemsForColumn().size(), 3);
        String expectedRetVal = "1^Anull^A3^B2^A160.7^A2^B3^A170.5^A2^B";
        assertEquals(expectedRetVal, topNList.printTopNItems());
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
        assertEquals(topNList.getTopNDataItemsForColumn().size(), 3);
        String expectedRetVal = "1^A10.6^A4^B2^Anull^A3^B3^A160.7^A2^B";
        assertEquals(expectedRetVal, topNList.printTopNItems());
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
        assertEquals(topNList.getTopNDataItemsForColumn().size(), 3);
        String expectedRetVal = "1^A10.6^A4^B2^A15.0^A3^B3^Anull^A3^B";
        assertEquals(expectedRetVal, topNList.printTopNItems());
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for TopNTestCase3 ***");
    }
}
