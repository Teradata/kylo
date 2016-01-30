package com.thinkbiganalytics.spark.dataprofiler.testsuites;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.thinkbiganalytics.spark.dataprofiler.testcases.CommandLineArgsTestCase;
import com.thinkbiganalytics.spark.dataprofiler.testcases.TopNTestCase;


@RunWith(value = Suite.class)
@SuiteClasses(value = { CommandLineArgsTestCase.class, TopNTestCase.class })


/**
 * Data Checks Test Suite
 * @author jagrut sharma
 *
 */
public class DataChecksTestSuite {
	
	@BeforeClass
	public static void setUpClass() {
		System.out.println("=== Starting run for DataChecksTestSuite ===");
	}

	@AfterClass
	public static void tearDownClass() {
		System.out.println("=== Completed run for DataChecksTestSuite ===");
	}

}