package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.spark.dataprofiler.columns.ByteColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.testdriver.DataProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;


/**
 * Byte Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class ByteColumnTestCase1 {
	static ColumnStatistics columnStats;
	static long nullCount;
	static long totalCount;
	static long uniqueCount;
	static double percNullValues;
	static double percUniqueValues;
	static double percDuplicateValues;
	static TopNDataList topNValues;
	static byte max;
	static byte min;
	static long sum;
	static double mean;
	static double stddev;
	static double variance;
	
	@BeforeClass 
    public static void setUpClass() {      
		
        System.out.println("\t*** Starting run for ByteColumnTestCase1 ***");
        columnStats = DataProfilerTest.columnStatsMap.get(12);	//ccode
        nullCount = 3l;
        totalCount = 10l;
        uniqueCount = 4l;
        percNullValues = 30.0d;
        percUniqueValues = 40.0d;
        percDuplicateValues = 60.0d;
        topNValues = columnStats.getTopNValues();
        max = Byte.valueOf((byte) 99);
        min = Byte.valueOf((byte) 2);
        sum = 537l;
        mean = 76.71428571d;
        stddev = 36.67118092d;
        variance = 1344.77551d;
	}
	
	
	@Test
    public void testByteNullCount() {
    	assertEquals(nullCount, columnStats.getNullCount());
    }
    
    
    @Test
    public void testByteTotalCount() {
		assertEquals(totalCount, columnStats.getTotalCount());
    }
    
    
    @Test
    public void testByteUniqueCount() {
		assertEquals(uniqueCount, columnStats.getUniqueCount());
    }
    
    
    @Test
    public void testBytePercNullValues() {
		assertEquals(percNullValues, columnStats.getPercNullValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testBytePercUniqueValues() {
		assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testBytePercDuplicateValues() {
		assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), DataProfilerTest.epsilon);
    }
        
    
    @Test
    public void testDoubleTopNValues() {
		 Object[] topNDataItems;
		 topNDataItems = topNValues.getTopNDataItemsForColumnInReverse().toArray();
		 Arrays.sort(topNDataItems);
		 int itemCount = topNDataItems.length;
		 assertEquals(Byte.valueOf((byte)99), ((TopNDataItem)topNDataItems[itemCount-1]).getValue());
		 assertEquals(Long.valueOf(5l), ((TopNDataItem)topNDataItems[itemCount-1]).getCount());
		 assertEquals(null, ((TopNDataItem)topNDataItems[itemCount-2]).getValue());
		 assertEquals(Long.valueOf(3l), ((TopNDataItem)topNDataItems[itemCount-2]).getCount());

		 for (int i = 0; i < (itemCount - 2 ); i++) {
			 assertEquals(Long.valueOf(1l),((TopNDataItem)(topNDataItems[i])).getCount());
		 }
	 }
    
    
    @Test
    public void testByteMax() {
    	assertEquals(max, ((ByteColumnStatistics)columnStats).getMax());
    }
    
    
    @Test
    public void testByteMin() {
    	assertEquals(min, ((ByteColumnStatistics)columnStats).getMin());
    }
    
    
    @Test
    public void testByteSum() {
    	assertEquals(sum, ((ByteColumnStatistics)columnStats).getSum());
    }
    
    
    @Test
    public void testByteMean() {
    	assertEquals(mean, ((ByteColumnStatistics)columnStats).getMean(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testByteStddev() {
    	assertEquals(stddev, ((ByteColumnStatistics)columnStats).getStddev(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testByteVariance() {
    	assertEquals(variance, ((ByteColumnStatistics)columnStats).getVariance(), DataProfilerTest.epsilon);
    }
    
    
    @AfterClass
    public static void tearDownClass() {
    	System.out.println("\t*** Completed run for ByteColumnTestCase1 ***");
    }
}    