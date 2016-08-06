package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.TreeSet;

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
	
	private static ColumnStatistics columnStats;
	private static long nullCount;
	private static long totalCount;
	private static long uniqueCount;
	private static double percNullValues;
	private static double percUniqueValues;
	private static double percDuplicateValues;
	private static TopNDataList topNValues;
	private static Timestamp maxTimestamp;
	private static Timestamp minTimestamp;
	
	
	@BeforeClass 
    public static void setUpClass() {      
        System.out.println("\t*** Starting run for TimestampColumnTestCase1 ***");
        columnStats = DataProfilerTest.columnStatsMap.get(8);	//lastlogin
        nullCount = 0L;
        totalCount = 10L;
        uniqueCount = 5L;
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
	public void testTimestampTopNValues() {
		TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
		Iterator<TopNDataItem> iterator = items.descendingIterator();

		//Verify that there are 3 items
		assertEquals(3, items.size());

		//Verify the top 3 item counts
		int index = 1;
		while (iterator.hasNext()) {
			TopNDataItem item = iterator.next();
			if (index == 1) {
				assertEquals(Timestamp.valueOf("2008-05-06 23:10:10"), item.getValue());
				assertEquals(Long.valueOf(4L), item.getCount());
			}
			else if (index == 2) {
				assertEquals(Timestamp.valueOf("2011-01-08 11:25:45"), item.getValue());
				assertEquals(Long.valueOf(3L), item.getCount());
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
