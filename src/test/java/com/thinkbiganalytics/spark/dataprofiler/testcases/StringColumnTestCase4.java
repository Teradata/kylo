package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class StringColumnTestCase4 {

	static ColumnStatistics columnStats;
	static long nullCount;
	static long totalCount;
	static long uniqueCount;
	static double percNullValues;
	static double percUniqueValues;
	static double percDuplicateValues;
	static TopNDataList topNValues;
	static int maxLength;
	static int minLength;
	static List<String> longestStrings;
	static String shortestString;
	static long emptyCount;
	static double percEmptyValues;
	static String minStringCase;
	static String maxStringCase;
	static String minStringICase;
	static List<String> maxStringICaseList;
	
	@BeforeClass 
    public static void setUpClass() {      
        System.out.println("\t*** Starting run for StringColumnTestCase4 ***");
        columnStats = DataProfilerTest.columnStatsMap.get(14);	//favoritepet
        nullCount = 1l;
        totalCount = 10l;
        uniqueCount = 8l;
        percNullValues = 10.0d;
        percUniqueValues = 80.0d;
        percDuplicateValues = 20.0d;
        topNValues = columnStats.getTopNValues();
        maxLength = 9;
        minLength = 3;
        
        //two longest strings with same length
        longestStrings = new ArrayList<String>();
        longestStrings.add("alligator");
        longestStrings.add("albatross");
        
        shortestString = "Cat";
        emptyCount = 1;
        percEmptyValues = 10.0d;
        minStringCase = "Alpaca";
        maxStringCase = "alligator";
        minStringICase = "albatross";
        
        //same word when considered without case
        maxStringICaseList = new ArrayList<String>();
        maxStringICaseList.add("Zebra");
        maxStringICaseList.add("ZEBRA");
        
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
		Object[] topNDataItems;
		topNDataItems = topNValues.getTopNDataItemsForColumnInReverse().toArray();
		Arrays.sort(topNDataItems);
		int itemCount = topNDataItems.length;
		
		assertEquals("Cat", ((TopNDataItem)topNDataItems[itemCount-1]).getValue());
		assertEquals(Long.valueOf(3l), ((TopNDataItem)topNDataItems[itemCount-1]).getCount());
		
		for (int i = 0; i < (topNDataItems.length - 1); i++) {
			assertEquals(Long.valueOf(1l),((TopNDataItem)(topNDataItems[i])).getCount());
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
		assertThat(((StringColumnStatistics) columnStats).getLongestString(), 
				anyOf(is(longestStrings.get(0)), is(longestStrings.get(1))));
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
		assertThat(((StringColumnStatistics) columnStats).getMaxStringICase(), 
				anyOf(is(maxStringICaseList.get(0)), is(maxStringICaseList.get(1))));
	}
	
	
	@AfterClass
	public static void tearDownClass() {
		System.out.println("\t*** Completed run for StringColumnTestCase4 ***");
	}
}

