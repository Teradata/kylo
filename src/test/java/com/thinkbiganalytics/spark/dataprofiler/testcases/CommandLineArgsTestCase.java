package com.thinkbiganalytics.spark.dataprofiler.testcases;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.AfterClass;
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
		
		String expectedRetVal = "select * from " + args[1];
		
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
		assertEquals(expectedRetVal, ProfilerConfiguration.OUTPUT_TABLE_NAME);
	}
	
	@AfterClass
    public static void tearDownClass() {
    	System.out.println("\t*** Completed run for CommandLineArgsTestCase ***");
    }
	
}
