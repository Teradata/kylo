package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.TreeSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;


/**
 * TopN Test Case 1
 * @author jagrut sharma
 *
 */
public class TopNTestCase1 {

    private static TreeSet<TopNDataItem> items;
	private static Iterator<TopNDataItem> iterator;
	private static String topNDataItemsString;
	
	@BeforeClass
	public static void setUpClass() {
		
		System.out.println("\t*** Starting run for TopNTestCase1 ***");

        TopNDataList topNList = new TopNDataList(5);
		
		topNList.add("A", 18L);
		topNList.add("B", 20L);
		topNList.add("C", 15L);
		topNList.add("D", 25L);
		topNList.add("E", 19L);

		items = topNList.getTopNDataItemsForColumn();
		iterator = items.descendingIterator();
		topNDataItemsString= topNList.printTopNItems();
			
	}
	
	
	@Test
	public void testTopNSummaryCount() {
		
			assertEquals(5, items.size());
	}
	
	
	@Test
	public void testTopNValues() {

		int index = 1;
		while (iterator.hasNext()) {
			TopNDataItem item = iterator.next();

			if (index == 1) {
				assertEquals("D", item.getValue());
				assertEquals(Long.valueOf(25), item.getCount());
			}
			else if (index == 2) {
				assertEquals("B", item.getValue());
				assertEquals(Long.valueOf(20), item.getCount());
			}
			else if (index == 3) {
				assertEquals("E", item.getValue());
				assertEquals(Long.valueOf(19), item.getCount());
			}
			else if (index == 4) {
				assertEquals("A", item.getValue());
				assertEquals(Long.valueOf(18), item.getCount());
			}
			else if (index == 5) {
				assertEquals("C", item.getValue());
				assertEquals(Long.valueOf(15), item.getCount());
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
		
		assertEquals(expectedRetVal, topNDataItemsString);
	}
	
	
	@AfterClass
    public static void tearDownClass() {
    	System.out.println("\t*** Completed run for TopNTestCase1 ***");
    }
}
