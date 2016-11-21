package com.thinkbiganalytics.spark.dataprofiler.testcases;

import com.thinkbiganalytics.spark.dataprofiler.columns.ByteColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
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
 * Byte Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class ByteColumnTestCase1 extends ProfilerTest {
	private static ColumnStatistics columnStats;
	private static long nullCount;
	private static long totalCount;
	private static long uniqueCount;
	private static double percNullValues;
	private static double percUniqueValues;
	private static double percDuplicateValues;
	private static TopNDataList topNValues;
	private static byte max;
	private static byte min;
	private static long sum;
	private static double mean;
	private static double stddev;
	private static double variance;

    @Before
    public void setUp() {
        super.setUp();

        columnStats = columnStatsMap.get(12);	//ccode
        nullCount = 3L;
        totalCount = 10L;
        uniqueCount = 4L;
        percNullValues = 30.0d;
        percUniqueValues = 40.0d;
        percDuplicateValues = 60.0d;
        topNValues = columnStats.getTopNValues();
        max = (byte) 99;
        min = (byte) 2;
        sum = 537L;
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
		assertEquals(percNullValues, columnStats.getPercNullValues(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testBytePercUniqueValues() {
		assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testBytePercDuplicateValues() {
		assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), ProfilerTest.epsilon);
    }


    @Test
    public void testByteTopNValues() {
        TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
        Iterator<TopNDataItem> iterator = items.descendingIterator();

        //Verify that there are 3 items
        assertEquals(3, items.size());

        //Verify the top 3 item counts
        int index = 1;
        while (iterator.hasNext()) {
            TopNDataItem item = iterator.next();
            if (index == 1) {
                assertEquals((byte) 99, item.getValue());
                assertEquals(Long.valueOf(5L), item.getCount());
            }
            else if (index == 2) {
                assertEquals(null, item.getValue());
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
    	assertEquals(mean, ((ByteColumnStatistics)columnStats).getMean(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testByteStddev() {
    	assertEquals(stddev, ((ByteColumnStatistics)columnStats).getStddev(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testByteVariance() {
    	assertEquals(variance, ((ByteColumnStatistics)columnStats).getVariance(), ProfilerTest.epsilon);
    }
    
    
    @AfterClass
    public static void tearDownClass() {
    	System.out.println("\t*** Completed run for ByteColumnTestCase1 ***");
    }
}    