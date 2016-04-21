package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.spark.dataprofiler.columns.BooleanColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.testdriver.DataProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;

/**
 * Boolean Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class BooleanColumnTestCase1 {

	static ColumnStatistics columnStats;
	static long nullCount;
	static long totalCount;
	static long uniqueCount;
	static double percNullValues;
	static double percUniqueValues;
	static double percDuplicateValues;
	static TopNDataList topNValues;
	static long trueCount;
	static long falseCount;
	
	
	@BeforeClass 
    public static void setUpClass() {      
        System.out.println("\t*** Starting run for BooleanColumnTestCase1 ***");
        columnStats = DataProfilerTest.columnStatsMap.get(7);	//lifemember
        nullCount = 2l;
        totalCount = 10l;
        uniqueCount = 3l;
        percNullValues = 20.0d;
        percUniqueValues = 30.0d;
        percDuplicateValues = 70.0d;
        topNValues = columnStats.getTopNValues();
        trueCount = 5l;
        falseCount = 3l;
	}
	
	
	@Test
	public void testBooleanNullCount() {
		assertEquals(nullCount, columnStats.getNullCount());
	}
	
	
	@Test
	public void testBooleanTotalCount() {
		assertEquals(totalCount, columnStats.getTotalCount());
	}
	
	
	@Test
	public void testBooleanUniqueCount() {
		assertEquals(uniqueCount, columnStats.getUniqueCount());
	}
	
	
	@Test
	public void testBooleanPercNullValues() {
		assertEquals(percNullValues, columnStats.getPercNullValues(), DataProfilerTest.epsilon);
	}
	
	
	@Test
	public void testBooleanPercUniqueValues() {
		assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), DataProfilerTest.epsilon);
	}
	
	
	@Test
	public void testBooleanPercDuplicateValues() {
		assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), DataProfilerTest.epsilon);
	}
	
	
	@Test
	public void testStringTopNValues() {
		
		Object[] topNDataItems;
		topNDataItems = topNValues.getTopNDataItemsForColumnInReverse().toArray();
		Arrays.sort(topNDataItems);
		int itemCount = topNDataItems.length;
		
		assertEquals(Boolean.TRUE, ((TopNDataItem)topNDataItems[itemCount-1]).getValue());
		assertEquals(Long.valueOf(5l), ((TopNDataItem)topNDataItems[itemCount-1]).getCount());
		assertEquals(Boolean.FALSE, ((TopNDataItem)topNDataItems[itemCount-2]).getValue());
		assertEquals(Long.valueOf(3l), ((TopNDataItem)topNDataItems[itemCount-2]).getCount());
	}
	
	
	@Test
	public void testBooleanTrueCount() {
		assertEquals(trueCount, ((BooleanColumnStatistics) columnStats).getTrueCount());
	}
	
	
	@Test
	public void testBooleanFalseCount() {
		assertEquals(falseCount, ((BooleanColumnStatistics) columnStats).getFalseCount());
	}
	
	 @AfterClass
	    public static void tearDownClass() {
	    	System.out.println("\t*** Completed run for BooleanColumnTestCase1 ***");
	    }
}
