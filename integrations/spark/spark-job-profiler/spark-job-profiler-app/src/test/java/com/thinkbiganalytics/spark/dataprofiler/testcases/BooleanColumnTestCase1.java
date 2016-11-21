package com.thinkbiganalytics.spark.dataprofiler.testcases;

import com.thinkbiganalytics.spark.dataprofiler.columns.BooleanColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

/**
 * Boolean Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class BooleanColumnTestCase1 extends ProfilerTest {

	private static ColumnStatistics columnStats;
	private static long nullCount;
	private static long totalCount;
	private static long uniqueCount;
	private static double percNullValues;
	private static double percUniqueValues;
	private static double percDuplicateValues;
	private static TopNDataList topNValues;
	private static long trueCount;
	private static long falseCount;


	@Before
	public void setUp() {
		super.setUp();

		columnStats = columnStatsMap.get(7);	//lifemember
        nullCount = 2L;
        totalCount = 10L;
        uniqueCount = 3L;
        percNullValues = 20.0d;
        percUniqueValues = 30.0d;
        percDuplicateValues = 70.0d;
        topNValues = columnStats.getTopNValues();
        trueCount = 5L;
        falseCount = 3L;
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
		assertEquals(percNullValues, columnStats.getPercNullValues(), ProfilerTest.epsilon);
	}
	
	
	@Test
	public void testBooleanPercUniqueValues() {
		assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), ProfilerTest.epsilon);
	}
	
	
	@Test
	public void testBooleanPercDuplicateValues() {
		assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), ProfilerTest.epsilon);
	}
	

	@Test
	public void testBooleanTopNValues() {
		TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
		Iterator<TopNDataItem> iterator = items.descendingIterator();

		//Verify that there are 3 items
		assertEquals(3, items.size());

		//Verify the top 3 item counts
		int index = 1;
		while (iterator.hasNext()) {
			TopNDataItem item = iterator.next();
			if (index == 1) {
				assertEquals(Boolean.TRUE, item.getValue());
				assertEquals(Long.valueOf(5L), item.getCount());
			}
			else if (index == 2) {
				assertEquals(Boolean.FALSE, item.getValue());
				assertEquals(Long.valueOf(3L), item.getCount());
			}
			else if (index == 3) {
				assertEquals(null, item.getValue());
				assertEquals(Long.valueOf(2L), item.getCount());
			}

			index++;
		}
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
