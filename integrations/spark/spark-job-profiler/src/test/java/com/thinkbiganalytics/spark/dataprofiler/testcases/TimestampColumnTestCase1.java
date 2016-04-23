package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.TimestampColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.testdriver.DataProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;


/**
 * Timestamp Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class TimestampColumnTestCase1 {
	
	static ColumnStatistics columnStats;
	static long nullCount;
	static long totalCount;
	static long uniqueCount;
	static double percNullValues;
	static double percUniqueValues;
	static double percDuplicateValues;
	static TopNDataList topNValues;
	static Timestamp maxTimestamp;
	static Timestamp minTimestamp;
	
	
	@BeforeClass 
    public static void setUpClass() {      
        System.out.println("\t*** Starting run for TimestampColumnTestCase1 ***");
        columnStats = DataProfilerTest.columnStatsMap.get(8);	//lastlogin
        nullCount = 0l;
        totalCount = 10l;
        uniqueCount = 5l;
        percNullValues = 0.0d;
        percUniqueValues = 50.0d;
        percDuplicateValues = 50.0d;
        topNValues = columnStats.getTopNValues();
        maxTimestamp = Timestamp.valueOf("2016-01-14 14:20:20");
        minTimestamp = Timestamp.valueOf("2007-03-16 08:24:31");   
    }
	
	
	@Test
	public void testTimestampNullCount() {
		assertEquals(nullCount, columnStats.getNullCount());
	}
	
	
	@Test
	public void testTimestampTotalCount() {
		assertEquals(totalCount, columnStats.getTotalCount());
	}
	
	
	@Test
	public void testTimestampUniqueCount() {
		assertEquals(uniqueCount, columnStats.getUniqueCount());
	}
	
	
	@Test
	public void testTimestampPercNullValues() {
		assertEquals(percNullValues, columnStats.getPercNullValues(), DataProfilerTest.epsilon);
	}
	
	
	@Test
	public void testTimestampPercUniqueValues() {
		assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), DataProfilerTest.epsilon);
	}
	
	
	@Test
	public void testTimestampPercDuplicateValues() {
		assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), DataProfilerTest.epsilon);
	}
	
	
	@Test
	public void testStringTopNValues() {
		
		Object[] topNDataItems;
		topNDataItems = topNValues.getTopNDataItemsForColumnInReverse().toArray();
		Arrays.sort(topNDataItems);
		int itemCount = topNDataItems.length;
		
		assertEquals(Timestamp.valueOf("2008-05-06 23:10:10"), ((TopNDataItem)topNDataItems[itemCount-1]).getValue());
		assertEquals(Long.valueOf(4l), ((TopNDataItem)topNDataItems[itemCount-1]).getCount());
		assertEquals(Timestamp.valueOf("2011-01-08 11:25:45"), ((TopNDataItem)topNDataItems[itemCount-2]).getValue());
		assertEquals(Long.valueOf(3l), ((TopNDataItem)topNDataItems[itemCount-2]).getCount());
		
		
		for (int i = 0; i < (topNDataItems.length - 2); i++) {
			assertEquals(Long.valueOf(1l),((TopNDataItem)(topNDataItems[i])).getCount());
		}
	}
	
	
	@Test
	public void testTimestampMaxTimestamp() {
		assertEquals(maxTimestamp, ((TimestampColumnStatistics) columnStats).getMaxTimestamp());
	}
	
	
	@Test
	public void testTimestampMinTimestamp() {
		assertEquals(minTimestamp, ((TimestampColumnStatistics) columnStats).getMinTimestamp());
	}
	
	@AfterClass
	public static void tearDownClass() {
		System.out.println("\t*** Completed run for TimestampColumnTestCase1 ***");
	}
	
}
