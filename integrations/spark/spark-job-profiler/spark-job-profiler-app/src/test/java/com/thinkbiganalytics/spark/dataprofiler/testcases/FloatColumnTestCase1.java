package com.thinkbiganalytics.spark.dataprofiler.testcases;

import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.FloatColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.TreeSet;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Float Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class FloatColumnTestCase1 extends ProfilerTest {
	
	private static ColumnStatistics columnStats;
    private static long nullCount;
    private static long totalCount;
    private static long uniqueCount;
    private static double percNullValues;
	static private double percUniqueValues;
    private static double percDuplicateValues;
    private static TopNDataList topNValues;
    private static float max;
    private static float min;
    private static double sum;
    private static double mean;
    private static double stddev;
    private static double variance;


    @Before
    public void setUp() {
        super.setUp();

        columnStats = columnStatsMap.get(10);	//weight
        nullCount = 3L;
        totalCount = 10L;
        uniqueCount = 6L;
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
		assertEquals(percNullValues, columnStats.getPercNullValues(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testFloatPercUniqueValues() {
		assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testFloatPercDuplicateValues() {
		assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), ProfilerTest.epsilon);
    }


    @Test
    public void testFloatTopNValues() {
        TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
        Iterator<TopNDataItem> iterator = items.descendingIterator();

        //Verify that there are 3 items
        assertEquals(3, items.size());

        //Verify the top 3 item counts
        int index = 1;
        while (iterator.hasNext()) {
            TopNDataItem item = iterator.next();
            if (index == 1) {
                assertEquals(null, item.getValue());
                assertEquals(Long.valueOf(3L), item.getCount());
            }
            else if (index == 2) {
                // A tie for count 2
                assertThat(Float.valueOf(item.getValue().toString()),
                        anyOf(is(110.5f), is(160.7f)));
                assertEquals(Long.valueOf(2L), item.getCount());
            }
            else if (index == 3) {
                // A tie for count 2
                assertThat(Float.valueOf(item.getValue().toString()),
                        anyOf(is(110.5f), is(160.7f)));
                assertEquals(Long.valueOf(2L), item.getCount());
            }

            index++;
        }
    }
    
    
    @Test
    public void testFloatMax() {
    	assertEquals(max, ((FloatColumnStatistics)columnStats).getMax(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testFloatMin() {
    	assertEquals(min, ((FloatColumnStatistics)columnStats).getMin(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testFloatSum() {
    	assertEquals(sum, ((FloatColumnStatistics)columnStats).getSum(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testFloatMean() {
    	assertEquals(mean, ((FloatColumnStatistics)columnStats).getMean(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testFloatStddev() {
    	assertEquals(stddev, ((FloatColumnStatistics)columnStats).getStddev(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testFloatVariance() {
    	assertEquals(variance, ((FloatColumnStatistics)columnStats).getVariance(), ProfilerTest.epsilon);
    }
    
    
    @AfterClass
    public static void tearDownClass() {
    	System.out.println("\t*** Completed run for FloatColumnTestCase1 ***");
    }
}   