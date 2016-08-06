package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.TreeSet;

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
public class IntegerColumnTestCase1 {

	private static ColumnStatistics columnStats;
	private static long nullCount;
	private static long totalCount;
	private static long uniqueCount;
	private static double percNullValues;
	private static double percUniqueValues;
	private static double percDuplicateValues;
	private static TopNDataList topNValues;
	private static int max;
	private static int min;
	private static long sum;
	private static double mean;
	private static double stddev;
	private static double variance;
	
    @BeforeClass 
    public static void setUpClass() {      
        System.out.println("\t*** Starting run for IntegerColumnTestCase1 ***");
        columnStats = DataProfilerTest.columnStatsMap.get(0);	//id
        nullCount = 0L;
        totalCount = 10L;
        uniqueCount = 10L;
        percNullValues = 0.0d;
        percUniqueValues = 100.0d;
        percDuplicateValues = 0.0d;
        topNValues = columnStats.getTopNValues();
        max = 10;
        min = 1;
        sum = 55L;
        mean = 5.5d;
        stddev = 2.872281323d;
        variance = 8.25d;
        
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
        TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
        Iterator<TopNDataItem> iterator = items.descendingIterator();

        /* Verify that there are 3 items */
        assertEquals(3, items.size());

        /*
            Verify the top 3 item counts
            Not checking values since they can be arbitrary
            All values have count 1 for this column
         */

        int index = 1;
        while (iterator.hasNext()) {
            TopNDataItem item = iterator.next();
            if (index == 1) {
                assertEquals(Long.valueOf(1L), item.getCount());
            }
            else if (index == 2) {
                assertEquals(Long.valueOf(1L), item.getCount());
            }
            else if (index == 3) {
                assertEquals(Long.valueOf(1L), item.getCount());
            }

            index++;
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
    	System.out.println("\t*** Completed run for IntegerColumnTestCase1 ***");
    }
}    