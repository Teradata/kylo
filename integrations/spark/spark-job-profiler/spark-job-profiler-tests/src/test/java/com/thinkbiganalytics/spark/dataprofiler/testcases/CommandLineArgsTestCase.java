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


import org.junit.Ignore;

/**
 * Command Line Arguments Test Case
 */
@Ignore
public class CommandLineArgsTestCase {

//    @BeforeClass
//    public static void setUpClass() {
//        System.out.println("\t*** Starting run for CommandLineArgsTestCase ***");
//    }
//
//    @Before
//    public void initializeConfiguration() {
//        ProfilerConfiguration.initialize();
//    }
//
//    @Test
//    public void testCountOfCommandLineArgs() {
//        String[] args = new String[4];
//        args[0] = "table";
//        args[1] = "profiler.people";
//        args[2] = "3";
//        args[3] = "profileresult";
//
//        Profiler profiler = new Profiler();
//
//        String retVal = profiler.checkCommandLineArgs(args);
//        assertNotNull(retVal);
//    }
//
//
//    @Test
//    public void testCountOfCommandLineArgs2() {
//        String[] args = new String[2];
//        args[0] = "table";
//        args[1] = "profiler.people";
//
//        Profiler profiler = new Profiler();
//
//        String retVal = profiler.checkCommandLineArgs(args);
//        assertNull(retVal);
//    }
//
//
//    @Test
//    public void testObjectTypeCommandLineArg() {
//        String args[] = new String[4];
//        args[0] = "procedure";
//        args[1] = "profiler.people";
//        args[2] = "3";
//        args[3] = "profileresult";
//
//        Profiler profiler = new Profiler();
//
//        String retVal = profiler.checkCommandLineArgs(args);
//        assertNull(retVal);
//    }
//
//
//    @Test
//    public void testTopNCommandLineArg() {
//        String args[] = new String[4];
//        args[0] = "table";
//        args[1] = "profiler.people";
//        args[2] = "-4";
//        args[3] = "profileresult";
//
//        Profiler profiler = new Profiler();
//
//        String retVal = profiler.checkCommandLineArgs(args);
//        assertNull(retVal);
//    }
//
//
//    @Test
//    public void testTableTypeCommandLineArg() {
//        String[] args = new String[4];
//        args[0] = "table";
//        args[1] = "profiler.people";
//        args[2] = "3";
//        args[3] = "profileresult";
//
//        String expectedRetVal = "select * from " + args[1];
//
//        Profiler profiler = new Profiler();
//
//        String retVal = profiler.checkCommandLineArgs(args);
//        assertEquals(expectedRetVal, retVal);
//    }
//
//
//    @Test
//    public void testQueryTypeCommandLineArg() {
//        String[] args = new String[4];
//        args[0] = "query";
//        args[1] = "select * from profiler.people where id < 100";
//        args[2] = "3";
//        args[3] = "profileresult";
//
//        String expectedRetVal = args[1];
//
//        Profiler profiler = new Profiler();
//
//        String retVal = profiler.checkCommandLineArgs(args);
//        assertEquals(expectedRetVal, retVal);
//    }
//
//    @Test
//    public void testOutputTableCommandLineArg() {
//        String[] args = new String[4];
//        args[0] = "table";
//        args[1] = "profiler.people";
//        args[2] = "3";
//        args[3] = "profileresult";
//
//        String expectedRetVal = args[3];
//        Profiler profiler = new Profiler();
//
//        profiler.checkCommandLineArgs(args);
//        assertEquals(expectedRetVal, ProfilerConfiguration.OUTPUT_TABLE_NAME);
//    }
//
//    @Test
//    public void testInputAndOutputPartitionKey() {
//        String[] args = new String[5];
//        args[0] = "table";
//        args[1] = "profiler.people";
//        args[2] = "3";
//        args[3] = "profilerresult";
//        args[4] = "part01";
//
//        String expectedRetVal = args[4];
//        Profiler profiler = new Profiler();
//        profiler.checkCommandLineArgs(args);
//        assertEquals(expectedRetVal, ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY);
//    }
//
//    @Test
//    public void testInputAndOutputPartitionKey2() {
//        String[] args = new String[4];
//        args[0] = "table";
//        args[1] = "profiler.people";
//        args[2] = "3";
//        args[3] = "profilerresult";
//
//        String expectedRetVal = "ALL";
//        Profiler profiler = new Profiler();
//        profiler.checkCommandLineArgs(args);
//        assertEquals(expectedRetVal, ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY);
//    }
//
//    @Test
//    public void testOutputDefaultDBAndTable() {
//        String[] args = new String[4];
//        args[0] = "table";
//        args[1] = "profiler.people";
//        args[2] = "3";
//        args[3] = "profilerresult";
//
//        String expectedRetVal1 = "default";
//        String expectedRetVal2 = args[3];
//
//        Profiler profiler = new Profiler();
//        profiler.checkCommandLineArgs(args);
//        assertEquals(expectedRetVal1, ProfilerConfiguration.OUTPUT_DB_NAME);
//        assertEquals(expectedRetVal2, ProfilerConfiguration.OUTPUT_TABLE_NAME);
//    }
//
//
//    @Test
//    public void testOutputDBAndTable() {
//        String[] args = new String[4];
//        args[0] = "table";
//        args[1] = "profiler.people";
//        args[2] = "3";
//        args[3] = "statsdb.profilerresult";
//
//        String expectedRetVal1 = "statsdb";
//        String expectedRetVal2 = "profilerresult";
//
//        Profiler profiler = new Profiler();
//        profiler.checkCommandLineArgs(args);
//        assertEquals(expectedRetVal1, ProfilerConfiguration.OUTPUT_DB_NAME);
//        assertEquals(expectedRetVal2, ProfilerConfiguration.OUTPUT_TABLE_NAME);
//    }
//
//    @Test
//    public void testInputAndOutputPartitionKeyWithOutputDBAndTable() {
//        String[] args = new String[4];
//        args[0] = "table";
//        args[1] = "profiler.people";
//        args[2] = "3";
//        args[3] = "profilerresult";
//
//        String expectedRetVal1 = "default";
//        String expectedRetVal2 = args[3];
//        String expectedRetVal3 = "ALL";
//
//        Profiler profiler = new Profiler();
//        profiler.checkCommandLineArgs(args);
//        assertEquals(expectedRetVal1, ProfilerConfiguration.OUTPUT_DB_NAME);
//        assertEquals(expectedRetVal2, ProfilerConfiguration.OUTPUT_TABLE_NAME);
//        assertEquals(expectedRetVal3, ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY);
//
//    }
//
//    @Test
//    public void testInputAndOutputPartitionKeyWithOutputDBAndTable2() {
//        String[] args = new String[5];
//        args[0] = "table";
//        args[1] = "profiler.people";
//        args[2] = "3";
//        args[3] = "mystatsdb.profilerresult2";
//        args[4] = "partition_00";
//
//        String expectedRetVal1 = "mystatsdb";
//        String expectedRetVal2 = "profilerresult2";
//        String expectedRetVal3 = "partition_00";
//
//        Profiler profiler = new Profiler();
//        profiler.checkCommandLineArgs(args);
//        assertEquals(expectedRetVal1, ProfilerConfiguration.OUTPUT_DB_NAME);
//        assertEquals(expectedRetVal2, ProfilerConfiguration.OUTPUT_TABLE_NAME);
//        assertEquals(expectedRetVal3, ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY);
//
//    }
//
//    @Test
//    public void testInputTableWithPartitionKey() {
//        String[] args = new String[5];
//        args[0] = "table";
//        args[1] = "profiler.people";
//        args[2] = "3";
//        args[3] = "mystatsdb.profilerresult2";
//        args[4] = "partition_00";
//
//        String expectedRetVal = "select * from profiler.people where processing_dttm = '" + args[4] + "'";
//
//        Profiler profiler = new Profiler();
//
//        String retVal = profiler.checkCommandLineArgs(args);
//        assertEquals(expectedRetVal, retVal);
//    }
//
//
//    @AfterClass
//    public static void tearDownClass() {
//        System.out.println("\t*** Completed run for CommandLineArgsTestCase ***");
//    }

}
