package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

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
public class StringColumnTestCase3 {
	
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
	static String longestString;
	static String shortestString;
	static long emptyCount;
	static double percEmptyValues;
	
	
	@BeforeClass 
    public static void setUpClass() {      
        System.out.println("\t*** Starting run for StringColumnTestCase3 ***");
        columnStats = DataProfilerTest.columnStatsMap.get(4);	//description (comes as varchar(n))
        nullCount = 1l;
        totalCount = 10l;
        uniqueCount = 9l;
        percNullValues = 10.0d;
        percUniqueValues = 90.0d;
        percDuplicateValues = 10.0d;
        topNValues = columnStats.getTopNValues();
        maxLength = 17;
        minLength = 5;
        longestString = "Elizabeth::Taylor";
        shortestString = "Jon::";
        emptyCount = 0;
        percEmptyValues = 0.0d;
        
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
		assertEquals("Rachael::", ((TopNDataItem)topNDataItems[itemCount-1]).getValue());
		assertEquals(Long.valueOf(2l), ((TopNDataItem)topNDataItems[itemCount-1]).getCount());
		
		for (int i = 0; i < (itemCount - 1 ); i++) {
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
	
	
	@AfterClass
	public static void tearDownClass() {
		System.out.println("\t*** Completed run for StringColumnTestCase1 ***");
	}
}    