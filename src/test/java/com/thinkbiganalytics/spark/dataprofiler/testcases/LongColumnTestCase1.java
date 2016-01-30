package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.LongColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.testdriver.DataProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;


/**
 * Long Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class LongColumnTestCase1 {

	static ColumnStatistics columnStats;
	static long nullCount;
	static long totalCount;
	static long uniqueCount;
	static double percNullValues;
	static double percUniqueValues;
	static double percDuplicateValues;
	static TopNDataList topNValues;
	static long max;
	static long min;
	static long sum;
	static double mean;
	static double stddev;
	static double variance;
	
	
	@BeforeClass 
    public static void setUpClass() {      
		
        System.out.println("\t*** Starting run for LongColumnTestCase1 ***");
        columnStats = DataProfilerTest.columnStatsMap.get(9);	//phash
        nullCount = 2l;
        totalCount = 10l;
        uniqueCount = 7l;
        percNullValues = 20.0d;
        percUniqueValues = 70.0d;
        percDuplicateValues = 30.0d;
        topNValues = columnStats.getTopNValues();
        max = 8782348100l;
        min = 1456890911l;
        sum = 43837413346l;
        mean = 5479676668.25d;
        stddev = 2930123653.19747d;
        variance = 8585624623027292200d;
      
    }
	
	
	@Test
    public void testLongNullCount() {
    	assertEquals(nullCount, columnStats.getNullCount());
    }
    
    
    @Test
    public void testLongTotalCount() {
		assertEquals(totalCount, columnStats.getTotalCount());
    }
    
    
    @Test
    public void testLongUniqueCount() {
		assertEquals(uniqueCount, columnStats.getUniqueCount());
    }
    
    
    @Test
    public void testLongPercNullValues() {
		assertEquals(percNullValues, columnStats.getPercNullValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testLongPercUniqueValues() {
		assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testLongPercDuplicateValues() {
		assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testLongTopNValues() {

		Object[] topNDataItems;
		topNDataItems = topNValues.getTopNDataItemsForColumnInReverse().toArray();
		Arrays.sort(topNDataItems);
		int itemCount = topNDataItems.length;
		
		assertEquals(2988626110l, ((TopNDataItem)topNDataItems[itemCount-1]).getValue());
		assertEquals(Long.valueOf(3l), ((TopNDataItem)topNDataItems[itemCount-1]).getCount());
		assertEquals(null, ((TopNDataItem)topNDataItems[itemCount-2]).getValue());
		assertEquals(Long.valueOf(2l), ((TopNDataItem)topNDataItems[itemCount-2]).getCount());
		
		for (int i = 0; i < (topNDataItems.length - 2); i++) {
			assertEquals(Long.valueOf(1l),((TopNDataItem)(topNDataItems[i])).getCount());
		}
	}
    
    
    @Test
    public void testLongMax() {
    	assertEquals(max, ((LongColumnStatistics)columnStats).getMax());
    }
    
    
    @Test
    public void testLongMin() {
    	assertEquals(min, ((LongColumnStatistics)columnStats).getMin());
    }
    
    
    @Test
    public void testLongSum() {
    	assertEquals(sum, ((LongColumnStatistics)columnStats).getSum());
    }
    
    
    @Test
    public void testLongMean() {
    	assertEquals(mean, ((LongColumnStatistics)columnStats).getMean(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testLongStddev() {
    	assertEquals(stddev, ((LongColumnStatistics)columnStats).getStddev(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testLongVariance() {
    	//Due to the results being extremely large numbers, epsilon2 is used.
    	assertEquals(variance, ((LongColumnStatistics)columnStats).getVariance(), DataProfilerTest.epsilon2);
    }
    
    
    @AfterClass
    public static void tearDownClass() {
    	System.out.println("\t*** Completed run for LongColumnTestCase1 ***");
    }
}
