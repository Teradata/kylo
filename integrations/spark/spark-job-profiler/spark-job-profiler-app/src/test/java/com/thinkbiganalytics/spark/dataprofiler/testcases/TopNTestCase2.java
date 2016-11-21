package com.thinkbiganalytics.spark.dataprofiler.testcases;

import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

/**
 * TopN Test Case 2
 * @author jagrut sharma
 */
public class TopNTestCase2 {
    private static TreeSet<TopNDataItem> items;
    private static String topNDataItemsString;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("\t*** Starting run for TopNTestCase2 ***");

        TopNDataList topNList = new TopNDataList(4);

        for (int i = 1; i < 10000000; i++) {
            topNList.add("Item" + i, (long) (i % 5000000));
        }

        items = topNList.getTopNDataItemsForColumn();
        topNDataItemsString= topNList.printTopNItems();
    }

    @Test
    public void testTopNSummaryCount() {
        assertEquals(4, items.size());
    }

    @Test
    public void testTopNValuesToWriteString() {
        String expectedRetVal = "1^AItem4999999^A4999999^B"
                + "2^AItem9999999^A4999999^B"
                + "3^AItem4999998^A4999998^B"
                + "4^AItem9999998^A4999998^B";

        assertEquals(expectedRetVal, topNDataItemsString);
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for TopNTestCase2 ***");
    }

}
