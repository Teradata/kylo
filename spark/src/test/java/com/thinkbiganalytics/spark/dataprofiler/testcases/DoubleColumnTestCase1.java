package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.DoubleColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.testdriver.DataProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;


/**
 * Double Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class DoubleColumnTestCase1 {
	
	static ColumnStatistics columnStats;
	static long nullCount;
	static long totalCount;
	static long uniqueCount;
	static double percNullValues;
	static double percUniqueValues;
	static double percDuplicateValues;
	static TopNDataList topNValues;
	static double max;
	static double min;
	static double sum;
	static double mean;
	static double stddev;
	static double variance;
	
	@BeforeClass
	public static void setUpClass() {
		System.out.println("\t*** Starting run for DoubleColumnTestCase1 ***");
		columnStats = DataProfilerTest.columnStatsMap.get(5);	//height
		nullCount = 3l;
		totalCount = 10l;
		uniqueCount = 5l;
		percNullValues = 30.0d;
		percUniqueValues = 50.0d;
		percDuplicateValues = 50.0d;
		topNValues = columnStats.getTopNValues();
		max = 6.22d;
		min = 4.37d;
		sum = 38.87d;
		mean = 5.552857143d;
		stddev = 0.615298169d;
		variance = 0.378591837d;
	}
	
	@Test
	public void testDoubleNullCount() {
		assertEquals(nullCount, columnStats.getNullCount());
	}
	
	
	 @Test
	 public void testDoubleTotalCount() {
		 assertEquals(totalCount, columnStats.getTotalCount());
	 }


	 @Test
	 public void testDoubleUniqueCount() {
		 assertEquals(uniqueCount, columnStats.getUniqueCount());
	 }


	 @Test
	 public void testDoublePercNullValues() {
		 assertEquals(percNullValues, columnStats.getPercNullValues(), DataProfilerTest.epsilon);
	 }


	 @Test
	 public void testDoublePercUniqueValues() {
		 assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), DataProfilerTest.epsilon);
	 }


	 @Test
	 public void testDoublePercDuplicateValues() {
		 assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), DataProfilerTest.epsilon);
	 }

	 
	 @Test
	    public void testDoubleTopNValues() {
		 Object[] topNDataItems;
		 topNDataItems = topNValues.getTopNDataItemsForColumnInReverse().toArray();
		 Arrays.sort(topNDataItems);
		 int itemCount = topNDataItems.length;
		 assertEquals(Double.valueOf(5.85d), ((TopNDataItem)topNDataItems[itemCount-1]).getValue());
		 assertEquals(Long.valueOf(4l), ((TopNDataItem)topNDataItems[itemCount-1]).getCount());
		 assertEquals(null, ((TopNDataItem)topNDataItems[itemCount-2]).getValue());
		 assertEquals(Long.valueOf(3l), ((TopNDataItem)topNDataItems[itemCount-2]).getCount());

		 for (int i = 0; i < (itemCount - 2 ); i++) {
			 assertEquals(Long.valueOf(1l),((TopNDataItem)(topNDataItems[i])).getCount());
		 }
	 }

	 @Test
	    public void testDoubleMax() {
	    	assertEquals(max, ((DoubleColumnStatistics)columnStats).getMax(), DataProfilerTest.epsilon);
	    }
	    
	    
	    @Test
	    public void testDoubleMin() {
	    	assertEquals(min, ((DoubleColumnStatistics)columnStats).getMin(), DataProfilerTest.epsilon);
	    }
	    
	    
	    @Test
	    public void testDoubleSum() {
	    	assertEquals(sum, ((DoubleColumnStatistics)columnStats).getSum(), DataProfilerTest.epsilon);
	    }
	    
	    
	    @Test
	    public void testDoubleMean() {
	    	assertEquals(mean, ((DoubleColumnStatistics)columnStats).getMean(), DataProfilerTest.epsilon);
	    }
	    
	    
	    @Test
	    public void testDoubleStddev() {
	    	assertEquals(stddev, ((DoubleColumnStatistics)columnStats).getStddev(), DataProfilerTest.epsilon);
	    }
	    
	    
	    @Test
	    public void testDoubleVariance() {
	    	assertEquals(variance, ((DoubleColumnStatistics)columnStats).getVariance(), DataProfilerTest.epsilon);
	    }
	    
	    
	    @AfterClass
	    public static void tearDownClass() {
	    	System.out.println("\t*** Completed run for DoubleColumnTestCase1 ***");
	    }
}   