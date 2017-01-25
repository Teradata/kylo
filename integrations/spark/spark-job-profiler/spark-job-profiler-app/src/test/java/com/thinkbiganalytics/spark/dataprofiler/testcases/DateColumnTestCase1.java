package com.thinkbiganalytics.spark.dataprofiler.testcases;

/*-
 * #%L
 * thinkbig-spark-job-profiler-app
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.DateColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerTest;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.util.Iterator;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;


/**
 * Date Column Statistics Test Case
 * @author jagrut sharma
 *
 */
public class DateColumnTestCase1 extends ProfilerTest {

	private static ColumnStatistics columnStats;
	private static long nullCount;
	private static long totalCount;
	private static long uniqueCount;
	private static double percNullValues;
	private static double percUniqueValues;
	private static double percDuplicateValues;
	private static TopNDataList topNValues;
	private static Date maxDate;
	private static Date minDate;

    @Before
    public void setUp() {
        super.setUp();

        columnStats = columnStatsMap.get(6);	//joindate
        nullCount = 0L;
        totalCount = 10L;
        uniqueCount = 7L;
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
		assertEquals(percNullValues, columnStats.getPercNullValues(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testDatePercUniqueValues() {
		assertEquals(percUniqueValues, columnStats.getPercUniqueValues(), ProfilerTest.epsilon);
    }
    
    
    @Test
    public void testDatePercDuplicateValues() {
		assertEquals(percDuplicateValues, columnStats.getPercDuplicateValues(), ProfilerTest.epsilon);
    }


    @Test
    public void testDateTopNValues() {
        TreeSet<TopNDataItem> items = topNValues.getTopNDataItemsForColumn();
        Iterator<TopNDataItem> iterator = items.descendingIterator();

        //Verify that there are 3 items
        assertEquals(3, items.size());

        //Verify the top 3 item counts
        int index = 1;
        while (iterator.hasNext()) {
            TopNDataItem item = iterator.next();
            if (index == 1) {
                assertEquals(Date.valueOf("2011-08-08"), item.getValue());
                assertEquals(Long.valueOf(3L), item.getCount());
            }
            else if (index == 2) {
                assertEquals(Date.valueOf("1990-10-25"), item.getValue());
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
