package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.IntegerColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.testdriver.DataProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;


/**
 * Integer Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class IntegerColumnTestCase2 {

	static ColumnStatistics columnStats;
	static long nullCount;
	static long totalCount;
	static long uniqueCount;
	static double percNullValues;
	static double percUniqueValues;
	static double percDuplicateValues;
	static TopNDataList topNValues;
	static int max;
	static int min;
	static long sum;
	static double mean;
	static double stddev;
	static double variance;
	
	@BeforeClass 
    public static void setUpClass() {      
        System.out.println("\t*** Starting run for IntegerColumnTestCase2 ***");
        columnStats = DataProfilerTest.columnStatsMap.get(3);	//age
        nullCount = 1l;
        totalCount = 10l;
        uniqueCount = 7l;
        percNullValues = 10.0d;
        percUniqueValues = 70.0d;
        percDuplicateValues = 30.0d;
        topNValues = columnStats.getTopNValues();
        max = 65;
        min = 11;
        sum = 272l;
        mean = 30.22222222d;
        stddev = 16.37598362d;
        variance = 268.1728395d;
        
    }
	
	@Test
    public void testIntegerNullCount() {
    	assertEquals(nullCount, columnStats.getNullCount());
    }
    
    
    @Test
    public void testIntegerTotalCount() {
		assertEquals(totalCount, columnStats.getTotalCount());
    }
    
    
    @Test
    public void testIntegerUniqueCount() {
		assertEquals(uniqueCount, columnStats.getUniqueCount());
    }
    
    
    @Test
    public void testIntegerPercNullValues() {
		assertEquals(percNullValues, columnStats.getPercNullValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testIntegerPercUniqueValues() {
		assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testIntegerPercDuplicateValues() {
		assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testIntegerTopNValues() {

		Object[] topNDataItems;
		topNDataItems = topNValues.getTopNDataItemsForColumnInReverse().toArray();
		Arrays.sort(topNDataItems);
		int itemCount = topNDataItems.length;
		assertEquals(Integer.valueOf(40), ((TopNDataItem)topNDataItems[itemCount-1]).getValue());
		assertEquals(Long.valueOf(3l), ((TopNDataItem)topNDataItems[itemCount-1]).getCount());
		assertEquals(Integer.valueOf(22), ((TopNDataItem)topNDataItems[itemCount-2]).getValue());
		assertEquals(Long.valueOf(2l), ((TopNDataItem)topNDataItems[itemCount-2]).getCount());
		
		for (int i = 0; i < (itemCount - 2 ); i++) {
			assertEquals(Long.valueOf(1l),((TopNDataItem)(topNDataItems[i])).getCount());
		}
	}
    
    
    @Test
    public void testIntegerMax() {
    	assertEquals(max, ((IntegerColumnStatistics)columnStats).getMax());
    }
    
    
    @Test
    public void testIntegerMin() {
    	assertEquals(min, ((IntegerColumnStatistics)columnStats).getMin());
    }
    
    
    @Test
    public void testIntegerSum() {
    	assertEquals(sum, ((IntegerColumnStatistics)columnStats).getSum());
    }
    
    
    @Test
    public void testIntegerMean() {
    	assertEquals(mean, ((IntegerColumnStatistics)columnStats).getMean(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testIntegerStddev() {
    	assertEquals(stddev, ((IntegerColumnStatistics)columnStats).getStddev(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testIntegerVariance() {
    	assertEquals(variance, ((IntegerColumnStatistics)columnStats).getVariance(), DataProfilerTest.epsilon);
    }
    
    
    @AfterClass
    public static void tearDownClass() {
    	System.out.println("\t*** Completed run for IntegerColumnTestCase2 ***");
    }
	
}
