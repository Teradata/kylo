package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;


/**
 * TopN Test Case
 * @author jagrut sharma
 *
 */
public class TopNTestCase {
	
	static TopNDataList topNList;
	static Object[] topNDataItems;
	static String topNDataItemsString;
	
	@BeforeClass
	public static void setUpClass() {
		
		System.out.println("\t*** Starting run for TopNTestCase ***");
		
		topNList = new TopNDataList();
		
		topNList.add("A", Long.valueOf(18));
		topNList.add("B", Long.valueOf(20));
		topNList.add("C", Long.valueOf(15));
		topNList.add("D", Long.valueOf(25));
		topNList.add("E", Long.valueOf(19));
		
		topNDataItems = topNList.getTopNDataItemsForColumnInReverse().toArray();
		Arrays.sort(topNDataItems);
		topNDataItemsString=topNList.printTopNItems(5);
			
	}
	
	
	@Test
	public void testTopNSummaryCount() {
		
			assertEquals(5, topNDataItems.length);
	}
	
	
	@Test
	public void testTopNValues() {
		
		assertEquals("D", ((TopNDataItem) topNDataItems[4]).getValue());
		assertEquals(Long.valueOf(25), ((TopNDataItem) topNDataItems[4]).getCount());
		
		assertEquals("B", ((TopNDataItem) topNDataItems[3]).getValue());
		assertEquals(Long.valueOf(20), ((TopNDataItem) topNDataItems[3]).getCount());
		
		assertEquals("E", ((TopNDataItem) topNDataItems[2]).getValue());
		assertEquals(Long.valueOf(19), ((TopNDataItem) topNDataItems[2]).getCount());
		
		assertEquals("A", ((TopNDataItem) topNDataItems[1]).getValue());
		assertEquals(Long.valueOf(18), ((TopNDataItem) topNDataItems[1]).getCount());
		
		assertEquals("C", ((TopNDataItem) topNDataItems[0]).getValue());
		assertEquals(Long.valueOf(15), ((TopNDataItem) topNDataItems[0]).getCount());
		
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
    	System.out.println("\t*** Completed run for TopNTestCase ***");
    }
}
