package com.thinkbiganalytics.spark.dataprofiler.testcases;

import static org.junit.Assert.assertEquals;

import java.sql.Date;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.DateColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.testdriver.DataProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;


/**
 * Date Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class DateColumnTestCase1 {

	static ColumnStatistics columnStats;
	static long nullCount;
	static long totalCount;
	static long uniqueCount;
	static double percNullValues;
	static double percUniqueValues;
	static double percDuplicateValues;
	static TopNDataList topNValues;
	static Date maxDate;
	static Date minDate;
	
	@BeforeClass
	public static void setUpClass() {
		System.out.println("\t*** Starting run for DateColumnTestCase1 ***");
        columnStats = DataProfilerTest.columnStatsMap.get(6);	//joindate
        nullCount = 0l;
        totalCount = 10l;
        uniqueCount = 7l;
        percNullValues = 0.0d;
        percUniqueValues = 70.0d;
        percDuplicateValues = 30.0d;
        topNValues = columnStats.getTopNValues();
        maxDate = Date.valueOf("2011-08-08");
        minDate = Date.valueOf("1956-11-12");
	}
	
	
	@Test
    public void testDateNullCount() {
    	assertEquals(nullCount, columnStats.getNullCount());
    }
    
    
    @Test
    public void testDateTotalCount() {
		assertEquals(totalCount, columnStats.getTotalCount());
    }
    
    
    @Test
    public void testDateUniqueCount() {
		assertEquals(uniqueCount, columnStats.getUniqueCount());
    }
    
    
    @Test
    public void testDatePercNullValues() {
		assertEquals(percNullValues, columnStats.getPercNullValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testDatePercUniqueValues() {
		assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testDatePercDuplicateValues() {
		assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), DataProfilerTest.epsilon);
    }
    
    
    @Test
    public void testDateTopNValues() {
    	Object[] topNDataItems;
		topNDataItems = topNValues.getTopNDataItemsForColumnInReverse().toArray();
		Arrays.sort(topNDataItems);
		int itemCount = topNDataItems.length;
		
		assertEquals(Date.valueOf("2011-08-08"), ((TopNDataItem)topNDataItems[itemCount-1]).getValue());
		assertEquals(Long.valueOf(3l), ((TopNDataItem)topNDataItems[itemCount-1]).getCount());
		assertEquals(Date.valueOf("1990-10-25"), ((TopNDataItem)topNDataItems[itemCount-2]).getValue());
		assertEquals(Long.valueOf(2l), ((TopNDataItem)topNDataItems[itemCount-2]).getCount());
		
		for (int i = 0; i < (topNDataItems.length - 2); i++) {
			assertEquals(Long.valueOf(1l),((TopNDataItem)(topNDataItems[i])).getCount());
		}

    }
    
    
    @Test
    public void testDateMaxDate() {
    	assertEquals(maxDate, ((DateColumnStatistics) columnStats).getMaxDate());
    }
    
    
    @Test
    public void testDateMinDate() {
    	assertEquals(minDate, ((DateColumnStatistics)columnStats).getMinDate());
    }
    
    
    @AfterClass
    public static void tearDownClass() {
    	System.out.println("\t*** Completed run for DateColumnTestCase1 ***");
    }
}
