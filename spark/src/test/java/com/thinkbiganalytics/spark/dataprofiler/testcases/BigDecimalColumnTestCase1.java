package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.spark.dataprofiler.columns.BigDecimalColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.testdriver.DataProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;

/**
 * Big Decimal Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class BigDecimalColumnTestCase1 {

	static ColumnStatistics columnStats;
	static long nullCount;
	static long totalCount;
	static long uniqueCount;
	static double percNullValues;
	static double percUniqueValues;
	static double percDuplicateValues;
	static TopNDataList topNValues;
	static BigDecimal max;
	static BigDecimal min;
	static BigDecimal sum;
	static BigDecimal topFirstValue;
	static BigDecimal topSecondValue;

	@BeforeClass 
	public static void setUpClass() {      

		System.out.println("\t*** Starting run for BigDecimalColumnTestCase1 ***");
		columnStats = DataProfilerTest.columnStatsMap.get(13);	//id
		nullCount = 2l;
		totalCount = 10l;
		uniqueCount = 4l;
		percNullValues = 20.0d;
		percUniqueValues = 40.0d;
		percDuplicateValues = 60.0d;
		topNValues = columnStats.getTopNValues();
		max = new BigDecimal(String.valueOf(8.223)).setScale(5, BigDecimal.ROUND_HALF_UP);
		min = new BigDecimal(String.valueOf(1.567)).setScale(5, BigDecimal.ROUND_HALF_UP);
		sum = new BigDecimal(String.valueOf(30.296)).setScale(5, BigDecimal.ROUND_HALF_UP);
		topFirstValue = new BigDecimal(String.valueOf(4.343)).setScale(5, BigDecimal.ROUND_HALF_UP);
		topSecondValue = new BigDecimal(String.valueOf(1.567)).setScale(5, BigDecimal.ROUND_HALF_UP);

	}

	@Test
	public void testBigDecimalNullCount() {
		assertEquals(nullCount, columnStats.getNullCount());
	}


	@Test
	public void testBigDecimalTotalCount() {
		assertEquals(totalCount, columnStats.getTotalCount());
	}


	@Test
	public void testBigDecimalUniqueCount() {
		assertEquals(uniqueCount, columnStats.getUniqueCount());
	}


	@Test
	public void testBigDecimalPercNullValues() {
		assertEquals(percNullValues, columnStats.getPercNullValues(), DataProfilerTest.epsilon);
	}


	@Test
	public void testBigDecimalPercUniqueValues() {
		assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), DataProfilerTest.epsilon);
	}


	@Test
	public void testBigDecimalPercDuplicateValues() {
		assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), DataProfilerTest.epsilon);
	}

	
	@Test
	public void testBigDecimalTopNValues() {
		
		Object[] topNDataItems;
		topNDataItems = topNValues.getTopNDataItemsForColumnInReverse().toArray();
		Arrays.sort(topNDataItems);
		int itemCount = topNDataItems.length;
		
		assertEquals(topFirstValue, new BigDecimal(String.valueOf(((TopNDataItem)topNDataItems[itemCount-1]).getValue())).setScale(5, BigDecimal.ROUND_HALF_UP));
		assertEquals(Long.valueOf(4l), ((TopNDataItem)topNDataItems[itemCount-1]).getCount());
		assertEquals(topSecondValue, new BigDecimal(String.valueOf(((TopNDataItem)topNDataItems[itemCount-2]).getValue())).setScale(5, BigDecimal.ROUND_HALF_UP));
		assertEquals(Long.valueOf(3l), ((TopNDataItem)topNDataItems[itemCount-2]).getCount());
		assertEquals(null, ((TopNDataItem)topNDataItems[itemCount-3]).getValue());
		assertEquals(Long.valueOf(2l), ((TopNDataItem)topNDataItems[itemCount-3]).getCount());
		
		for (int i = 0; i < (itemCount - 3 ); i++) {
			assertEquals(Long.valueOf(1l),((TopNDataItem)(topNDataItems[i])).getCount());
		}
	}
	
	
	@Test
    public void testBigDecimalMax() {
    	assertEquals(max, ((BigDecimalColumnStatistics)columnStats).getMax().setScale(5, BigDecimal.ROUND_HALF_UP));
    }
    
    
    @Test
    public void testBigDecimalMin() {
    	assertEquals(min, ((BigDecimalColumnStatistics)columnStats).getMin().setScale(5, BigDecimal.ROUND_HALF_UP));
    }
    
    
    @Test
    public void testBigDecimalSum() {
    	assertEquals(sum, ((BigDecimalColumnStatistics)columnStats).getSum().setScale(5, BigDecimal.ROUND_HALF_UP));
    }
    
    @AfterClass
    public static void tearDownClass() {
    	System.out.println("\t*** Completed run for BigDecimalColumnTestCase1 ***");
    }
}
