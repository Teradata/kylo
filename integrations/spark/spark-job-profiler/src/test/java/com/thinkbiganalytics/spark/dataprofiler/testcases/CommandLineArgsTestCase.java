package com.thinkbiganalytics.spark.dataprofiler.testcases;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.thinkbiganalytics.spark.dataprofiler.core.Profiler;
import com.thinkbiganalytics.spark.dataprofiler.core.ProfilerConfiguration;

/**
 * Command Line Arguments Test Case
 * @author jagrut sharma
 *
 */
public class CommandLineArgsTestCase {

	@BeforeClass
	public static void setUpClass() {
		System.out.println("\t*** Starting run for CommandLineArgsTestCase ***");
	}

	@Before
	public void initializeConfiguration() {
		ProfilerConfiguration.initialize();
	}

	@Test
	public void testCountOfCommandLineArgs() {
		String[] args = new String[4];
		args[0] = "table";
		args[1] = "profiler.people";
		args[2] = "3";
		args[3] = "profileresult";

		String retVal = Profiler.checkCommandLineArgs(args);
		assertNotNull(retVal);
	}


	@Test
	public void testCountOfCommandLineArgs2() {
		String[] args = new String[2];
		args[0] = "table";
		args[1] = "profiler.people";

		String retVal = Profiler.checkCommandLineArgs(args);
		assertNull(retVal);
	}


	@Test
	public void testObjectTypeCommandLineArg() {
		String args[] = new String[4];
		args[0] = "procedure";
		args[1] = "profiler.people";
		args[2] = "3";
		args[3] = "profileresult";

		String retVal = Profiler.checkCommandLineArgs(args);
		assertNull(retVal);
	}


	@Test
	public void testTopNCommandLineArg() {
		String args[] = new String[4];
		args[0] = "table";
		args[1] = "profiler.people";
		args[2] = "-4";
		args[3] = "profileresult";

		String retVal = Profiler.checkCommandLineArgs(args);
		assertNull(retVal);
	}


	@Test
	public void testTableTypeCommandLineArg() {
		String[] args = new String[4];
		args[0] = "table";
		args[1] = "profiler.people";
		args[2] = "3";
		args[3] = "profileresult";

		String expectedRetVal = "select * from `profiler`.`people`";

		String retVal = Profiler.checkCommandLineArgs(args);
		assertEquals(expectedRetVal, retVal);
	}


	@Test
	public void testQueryTypeCommandLineArg() {
		String[] args = new String[4];
		args[0] = "query";
		args[1] = "select * from profiler.people where id < 100";
		args[2] = "3";
		args[3] = "profileresult";

		String expectedRetVal = args[1];

		String retVal = Profiler.checkCommandLineArgs(args);
		assertEquals(expectedRetVal, retVal);
	}

	@Test
	public void testOutputTableCommandLineArg() {
		String[] args = new String[4];
		args[0] = "table";
		args[1] = "profiler.people";
		args[2] = "3";
		args[3] = "profileresult";

		String expectedRetVal = args[3];
		Profiler.checkCommandLineArgs(args);
		assertEquals(expectedRetVal, ProfilerConfiguration.OUTPUT_TABLE_NAME);
	}

	@Test
	public void testInputAndOutputPartitionKey() {
		String[] args = new String[5];
		args[0] = "table";
		args[1] = "profiler.people";
		args[2] = "3";
		args[3] = "profilerresult";
		args[4] = "part01";

		String expectedRetVal = args[4];
		Profiler.checkCommandLineArgs(args);
		assertEquals(expectedRetVal, ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY);
	}

	@Test
	public void testInputAndOutputPartitionKey2() {
		String[] args = new String[4];
		args[0] = "table";
		args[1] = "profiler.people";
		args[2] = "3";
		args[3] = "profilerresult";

		String expectedRetVal = "ALL";
		Profiler.checkCommandLineArgs(args);
		assertEquals(expectedRetVal, ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY);
	}

	@Test
	public void testOutputDefaultDBAndTable() {
		String[] args = new String[4];
		args[0] = "table";
		args[1] = "profiler.people";
		args[2] = "3";
		args[3] = "profilerresult";

		String expectedRetVal1 = "default";
		String expectedRetVal2 = args[3];

		Profiler.checkCommandLineArgs(args);
		assertEquals(expectedRetVal1, ProfilerConfiguration.OUTPUT_DB_NAME);
		assertEquals(expectedRetVal2, ProfilerConfiguration.OUTPUT_TABLE_NAME);
	}


	@Test
	public void testOutputDBAndTable() {
		String[] args = new String[4];
		args[0] = "table";
		args[1] = "profiler.people";
		args[2] = "3";
		args[3] = "statsdb.profilerresult";

		String expectedRetVal1 = "statsdb";
		String expectedRetVal2 = "profilerresult";

		Profiler.checkCommandLineArgs(args);
		assertEquals(expectedRetVal1, ProfilerConfiguration.OUTPUT_DB_NAME);
		assertEquals(expectedRetVal2, ProfilerConfiguration.OUTPUT_TABLE_NAME);
	}

	@Test
	public void testInputAndOutputPartitionKeyWithOutputDBAndTable() {
		String[] args = new String[4];
		args[0] = "table";
		args[1] = "profiler.people";
		args[2] = "3";
		args[3] = "profilerresult";

		String expectedRetVal1 = "default";
		String expectedRetVal2 = args[3];
		String expectedRetVal3 = "ALL";

		Profiler.checkCommandLineArgs(args);
		assertEquals(expectedRetVal1, ProfilerConfiguration.OUTPUT_DB_NAME);
		assertEquals(expectedRetVal2, ProfilerConfiguration.OUTPUT_TABLE_NAME);
		assertEquals(expectedRetVal3, ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY);

	}

	@Test
	public void testInputAndOutputPartitionKeyWithOutputDBAndTable2() {
		String[] args = new String[5];
		args[0] = "table";
		args[1] = "profiler.people";
		args[2] = "3";
		args[3] = "mystatsdb.profilerresult2";
		args[4] = "partition_00";


		String expectedRetVal1 = "mystatsdb";
		String expectedRetVal2 = "profilerresult2";
		String expectedRetVal3 = "partition_00";

		Profiler.checkCommandLineArgs(args);
		assertEquals(expectedRetVal1, ProfilerConfiguration.OUTPUT_DB_NAME);
		assertEquals(expectedRetVal2, ProfilerConfiguration.OUTPUT_TABLE_NAME);
		assertEquals(expectedRetVal3, ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY);

	}

	@Test
	public void testInputTableWithPartitionKey() {
		String[] args = new String[5];
		args[0] = "table";
		args[1] = "profiler.people";
		args[2] = "3";
		args[3] = "mystatsdb.profilerresult2";
		args[4] = "partition_00";

		String expectedRetVal = "select * from `profiler`.`people` where `processing_dttm` = \"" + args[4] + "\"";

		String retVal = Profiler.checkCommandLineArgs(args);
		assertEquals(expectedRetVal, retVal);
	}



	@AfterClass
    public static void tearDownClass() {
    	System.out.println("\t*** Completed run for CommandLineArgsTestCase ***");
    }

}
