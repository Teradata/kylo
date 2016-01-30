package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ShortColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.testdriver.DataProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;


/**
 * Short Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class ShortColumnTestCase1 {
	static ColumnStatistics columnStats;
	static long nullCount;
	static long totalCount;
	static long uniqueCount;
	static double percNullValues;
	static double percUniqueValues;
	static double percDuplicateValues;
	static TopNDataList topNValues;
	static short max;
	static short min;
	static long sum;
	static double mean;
	static double stddev;
	static double variance;
	
	@BeforeClass 
    public static void setUpClass() {    
		
        System.out.println("\t*** Starting run for ShortColumnTestCase1 ***");
        columnStats = DataProfilerTest.columnStatsMap.get(11);	//credits
        nullCount = 2l;
        totalCount = 10l;
        uniqueCount = 6l;
        percNullValues = 20.0d;
        percUniqueValues = 60.0d;
        percDuplicateValues = 40.0d;
        topNValues = columnStats.getTopNValues();
        max = (short)5000;
        min = (short)0;
        sum = 8600l;
        mean = 1075d;
        stddev = 1579.359047d;
        variance = 2494375d;
        
	}
	
	@Test
    public void testShortNullCount() {
    	assertEquals(nullCount, columnStats.getNullCount());
    }
    
    
    @Test
    public void testShortTotalCount() {
		assertEquals(totalCount, columnStats.getTotalCount());
    }
    
    
    @Test
    public void testShortUniqueCount() {
		assertEquals(uniqueCount, columnStats.getUniqueCount());
    }
    
    
    @Test
    public void testShortPercNullValues() {
		assertEquals(percNullValues, columnStats.getPercNullValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testShortPercUniqueValues() {
		assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testShortPercDuplicateValues() {
		assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testShortTopNValues() {

		Object[] topNDataItems;
		topNDataItems = topNValues.getTopNDataItemsForColumnInReverse().toArray();
		Arrays.sort(topNDataItems);
		int itemCount = topNDataItems.length;
		 assertEquals((short) 100, ((TopNDataItem)topNDataItems[itemCount-1]).getValue());
		 assertEquals(Long.valueOf(3l), ((TopNDataItem)topNDataItems[itemCount-1]).getCount());
		 
		 //Tie for count=2. Just checking counts for 2nd and 3rd items.
		 assertEquals(Long.valueOf(2l), ((TopNDataItem)topNDataItems[itemCount-2]).getCount());
		 assertEquals(Long.valueOf(2l), ((TopNDataItem)topNDataItems[itemCount-2]).getCount());
		 
		 for (int i = 0; i < (itemCount - 3 ); i++) {
			 assertEquals(Long.valueOf(1l),((TopNDataItem)(topNDataItems[i])).getCount());
		 }
	}
    
    
    @Test
    public void testShortMax() {
    	assertEquals(max, ((ShortColumnStatistics)columnStats).getMax());
    }
    
    
    @Test
    public void testShortMin() {
    	assertEquals(min, ((ShortColumnStatistics)columnStats).getMin());
    }
    
    
    @Test
    public void testShortSum() {
    	assertEquals(sum, ((ShortColumnStatistics)columnStats).getSum());
    }
    
    
    @Test
    public void testShortMean() {
    	assertEquals(mean, ((ShortColumnStatistics)columnStats).getMean(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testShortStddev() {
    	assertEquals(stddev, ((ShortColumnStatistics)columnStats).getStddev(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testShortVariance() {
    	assertEquals(variance, ((ShortColumnStatistics)columnStats).getVariance(), DataProfilerTest.epsilon);
    }
    
    
    @AfterClass
    public static void tearDownClass() {
    	System.out.println("\t*** Completed run for ShortColumnTestCase1 ***");
    }
}   