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

import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerTest;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Histogram Statistics Test Case
 */
public class HistogramCase1Test extends ProfilerTest {

    @AfterClass
    public static void tearDownClass() {
        System.out.println("\t*** Completed run for HistogramCase1Test ***");
    }

    @Before
    public void setUp() {
        super.setUp();
    }

    private void check(int index, String value, boolean strictMode) {
        int EXPECTED_ROWS_IN_STATS = 17;
        int HISTO_ROW_INDEX = 16;

        assertNotNull(columnStatsMap.get(index).getStatistics());
        assertEquals(EXPECTED_ROWS_IN_STATS, columnStatsMap.get(index).getStatistics().size());
        assertEquals("HISTO", columnStatsMap.get(index).getStatistics().get(HISTO_ROW_INDEX).getMetricType());

        if (strictMode) {
            assertEquals(value, columnStatsMap.get(index).getStatistics().get(HISTO_ROW_INDEX).getMetricValue());
        } else {
            assertNotNull(columnStatsMap.get(index).getStatistics().get(HISTO_ROW_INDEX).getMetricValue());
        }
    }

    @Test
    public void testIntegerHistogram_1() {
        //id
        check(0, "{\"_1\":[1.0,2.8,4.6,6.4,8.2,10.0],\"_2\":[2,2,2,2,2]}", true);
    }

    @Test
    public void testIntegerHistogram_2() {
        //age
        check(3, "{\"_1\":[11.0,21.8,32.6,43.4,54.2,65.0],\"_2\":[3,2,3,0,1]}", true);
    }

    @Test
    public void testDoubleHistogram() {
        //height
        check(5, "{\"_1\":[4.37,4.74,5.11,5.48,5.85,6.22],\"_2\":[1,1,0,4,1]}", true);
    }

    @Test
    public void testFloatHistogram() {
        //weight
        check(10, "{\"_1\":[40.2,68.28,96.35999999999999,124.43999999999998,152.51999999999998,180.6],\"_2\":[1,0,2,0,4]}", false);
    }

    @Test
    public void testShortHistogram() {
        //credits
        check(11, "{\"_1\":[0.0,1000.0,2000.0,3000.0,4000.0,5000.0],\"_2\":[5,2,0,0,1]}", true);
    }

    @Test
    public void testByteHistogram() {
        //ccode
        check(12, "{\"_1\":[2.0,21.4,40.8,60.2,79.6,99.0],\"_2\":[1,1,0,0,5]}", true);
    }
}
