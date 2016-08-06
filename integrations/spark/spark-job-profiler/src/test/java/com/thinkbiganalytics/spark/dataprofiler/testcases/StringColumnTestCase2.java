package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.TreeSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.StringColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.testdriver.DataProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;


/**
 * String Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class StringColumnTestCase2 {

	private static ColumnStatistics columnStats;
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
	
	@BeforeClass 
    public static void setUpClass() {      
        System.out.println("\t*** Starting run for StringColumnTestCase2 ***");
        columnStats = DataProfilerTest.columnStatsMap.get(2);	//lastname
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
		assertEquals(nullCount, columnStats.getNullCount());
	}
	
	
	@Test
	public void testStringTotalCount() {
		assertEquals(totalCount, columnStats.getTotalCount());
	}
	
	
	@Test
	public void testStringUniqueCount() {
		assertEquals(uniqueCount, columnStats.getUniqueCount());
	}
	
	
	@Test
	public void testStringPercNullValues() {
		assertEquals(percNullValues, columnStats.getPercNullValues(), DataProfilerTest.epsilon);
	}
	
	
	@Test
	public void testStringPercUniqueValues() {
		assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), DataProfilerTest.epsilon);
	}
	
	
	@Test
	public void testStringPercDuplicateValues() {
		assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), DataProfilerTest.epsilon);
	}
	
	
	@Test
	public void testStringTopNValues() {
		TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
		Iterator<TopNDataItem> iterator = items.descendingIterator();

		//Verify that there are 3 items
		assertEquals(3, items.size());

		//Verify the top 3 item counts
		int index = 1;
		while (iterator.hasNext()) {
			TopNDataItem item = iterator.next();
			if (index == 1) {
				assertEquals(DataProfilerTest.EMPTY_STRING, item.getValue());
				assertEquals(Long.valueOf(4L), item.getCount());
			}
			else if (index == 2) {
				assertEquals("Taylor", item.getValue());
				assertEquals(Long.valueOf(2L), item.getCount());
			}
			else if (index == 3) {
				/*
                    Not checking value since it can be arbitrary.
                    All remaining values have count 1
                */
				assertEquals(Long.valueOf(1L), item.getCount());
			}

			index++;
		}
	}
	
	@Test
	public void testStringMaxLength() {
		assertEquals(maxLength, ((StringColumnStatistics) columnStats).getMaxLength());
	}
	
	
	@Test
	public void testStringMinLength() {
		assertEquals(minLength, ((StringColumnStatistics) columnStats).getMinLength());
	}
	
	
	@Test
	public void testStringLongestString() {
		assertEquals(longestString, ((StringColumnStatistics) columnStats).getLongestString());
	}
	
	
	@Test
	public void testStringShortestString() {
		assertEquals(shortestString, ((StringColumnStatistics) columnStats).getShortestString());
	}
	
	
	@Test
	public void testStringEmptyCount() {
		assertEquals(emptyCount, ((StringColumnStatistics) columnStats).getEmptyCount());
	}
	
	
	@Test
	public void testStringPercEmptyValues() {
		assertEquals(percEmptyValues, ((StringColumnStatistics) columnStats).getPercEmptyValues(), DataProfilerTest.epsilon);
	}
	
	
	@Test
	public void testStringMinStringCaseSensitive() {
		assertEquals(minStringCase, ((StringColumnStatistics) columnStats).getMinStringCase());
	}
	
	
	@Test
	public void testStringMaxStringCaseSensitive() {
		assertEquals(maxStringCase, ((StringColumnStatistics) columnStats).getMaxStringCase());
	}
	
	
	@Test
	public void testStringMinStringCaseInsensitive() {
		assertEquals(minStringICase, ((StringColumnStatistics) columnStats).getMinStringICase());
	}
	
	
	@Test
	public void testStringMaxStringCaseInsensitive() {
		assertEquals(maxStringICase, ((StringColumnStatistics) columnStats).getMaxStringICase());
	}
	
	
	@AfterClass
	public static void tearDownClass() {
		System.out.println("\t*** Completed run for StringColumnTestCase2 ***");
	}

}
