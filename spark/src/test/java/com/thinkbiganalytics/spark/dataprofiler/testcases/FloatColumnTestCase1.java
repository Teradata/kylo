package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.FloatColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.testdriver.DataProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;

/**
 * Float Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class FloatColumnTestCase1 {
	
	static ColumnStatistics columnStats;
	static long nullCount;
	static long totalCount;
	static long uniqueCount;
	static double percNullValues;
	static double percUniqueValues;
	static double percDuplicateValues;
	static TopNDataList topNValues;
	static float max;
	static float min;
	static double sum;
	static double mean;
	static double stddev;
	static double variance;
	
	
	@BeforeClass 
    public static void setUpClass() {      
		
        System.out.println("\t*** Starting run for FloatColumnTestCase1 ***");
        columnStats = DataProfilerTest.columnStatsMap.get(10);	//weight
        nullCount = 3l;
        totalCount = 10l;
        uniqueCount = 6l;
        percNullValues = 30.0d;
        percUniqueValues = 60.0d;
        percDuplicateValues = 40.0d;
        topNValues = columnStats.getTopNValues();
        max = 180.6f;
        min = 40.2f;
        sum = 918.5f;
        mean = 131.2142857f;
        stddev = 44.56079086f;
        variance = 1985.664082f;
        
    }
	
	@Test
    public void testFloatNullCount() {
    	assertEquals(nullCount, columnStats.getNullCount());
    }
    
    
    @Test
    public void testFloatTotalCount() {
		assertEquals(totalCount, columnStats.getTotalCount());
    }
    
    
    @Test
    public void testFloatUniqueCount() {
		assertEquals(uniqueCount, columnStats.getUniqueCount());
    }
    
    
    @Test
    public void testFloatPercNullValues() {
		assertEquals(percNullValues, columnStats.getPercNullValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testFloatPercUniqueValues() {
		assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testFloatPercDuplicateValues() {
		assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testFloatTopNValues() {
	 Object[] topNDataItems;
	 topNDataItems = topNValues.getTopNDataItemsForColumnInReverse().toArray();
	 Arrays.sort(topNDataItems);
	 int itemCount = topNDataItems.length;
	 assertEquals(null, ((TopNDataItem)topNDataItems[itemCount-1]).getValue());
	 assertEquals(Long.valueOf(3l), ((TopNDataItem)topNDataItems[itemCount-1]).getCount());

	 //A tie for count=2. Just check that the next two values have count=2.
	 assertEquals(Long.valueOf(2l), ((TopNDataItem)topNDataItems[itemCount-2]).getCount());
	 assertEquals(Long.valueOf(2l), ((TopNDataItem)topNDataItems[itemCount-3]).getCount());

	 for (int i = 0; i < (itemCount - 3 ); i++) {
		 assertEquals(Long.valueOf(1l),((TopNDataItem)(topNDataItems[i])).getCount());
	 }
 }

    
    
    @Test
    public void testFloatMax() {
    	assertEquals(max, ((FloatColumnStatistics)columnStats).getMax(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testFloatMin() {
    	assertEquals(min, ((FloatColumnStatistics)columnStats).getMin(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testFloatSum() {
    	assertEquals(sum, ((FloatColumnStatistics)columnStats).getSum(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testFloatMean() {
    	assertEquals(mean, ((FloatColumnStatistics)columnStats).getMean(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testFloatStddev() {
    	assertEquals(stddev, ((FloatColumnStatistics)columnStats).getStddev(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testFloatVariance() {
    	assertEquals(variance, ((FloatColumnStatistics)columnStats).getVariance(), DataProfilerTest.epsilon);
    }
    
    
    @AfterClass
    public static void tearDownClass() {
    	System.out.println("\t*** Completed run for FloatColumnTestCase1 ***");
    }
}   