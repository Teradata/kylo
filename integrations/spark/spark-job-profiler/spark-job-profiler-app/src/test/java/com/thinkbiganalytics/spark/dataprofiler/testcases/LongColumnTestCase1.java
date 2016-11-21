package com.thinkbiganalytics.spark.dataprofiler.testcases;

import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.LongColumnStatistics;
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
 * Long Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class LongColumnTestCase1 extends ProfilerTest {

	private static ColumnStatistics columnStats;
	private static long nullCount;
	private static long totalCount;
	private static long uniqueCount;
	private static double percNullValues;
	private static double percUniqueValues;
	private static double percDuplicateValues;
	private static TopNDataList topNValues;
	private static long max;
	private static long min;
	private static long sum;
	private static double mean;
	private static double stddev;
	private static double variance;


    @Before
    public void setUp() {
        super.setUp();

        columnStats = columnStatsMap.get(9);	//phash
        nullCount = 2L;
        totalCount = 10L;
        uniqueCount = 7L;
        percNullValues = 20.0d;
        percUniqueValues = 70.0d;
        percDuplicateValues = 30.0d;
        topNValues = columnStats.getTopNValues();
        max = 8782348100L;
        min = 1456890911L;
        sum = 43837413346L;
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
		assertEquals(percNullValues, columnStats.getPercNullValues(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testLongPercUniqueValues() {
		assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testLongPercDuplicateValues() {
		assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), ProfilerTest.epsilon);
    }
    

    @Test
    public void testLongTopNValues() {
        TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
        Iterator<TopNDataItem> iterator = items.descendingIterator();

        //Verify that there are 3 items
        assertEquals(3, items.size());

        //Verify the top 3 item counts
        int index = 1;
        while (iterator.hasNext()) {
            TopNDataItem item = iterator.next();
            if (index == 1) {
                assertEquals(2988626110L, item.getValue());
                assertEquals(Long.valueOf(3L), item.getCount());
            }
            else if (index == 2) {
                assertEquals(null, item.getValue());
                assertEquals(Long.valueOf(2L), item.getCount());
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
    	assertEquals(mean, ((LongColumnStatistics)columnStats).getMean(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testLongStddev() {
    	assertEquals(stddev, ((LongColumnStatistics)columnStats).getStddev(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testLongVariance() {
    	//Due to the results being extremely large numbers, epsilon2 is used.
    	assertEquals(variance, ((LongColumnStatistics)columnStats).getVariance(), ProfilerTest.epsilon2);
    }
    
    
    @AfterClass
    public static void tearDownClass() {
    	System.out.println("\t*** Completed run for LongColumnTestCase1 ***");
    }
}
