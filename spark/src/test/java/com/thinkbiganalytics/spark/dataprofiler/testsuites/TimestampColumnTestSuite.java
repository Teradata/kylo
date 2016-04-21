package com.thinkbiganalytics.spark.dataprofiler.testsuites;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.thinkbiganalytics.spark.dataprofiler.testcases.TimestampColumnTestCase1;

@RunWith(value = Suite.class)
@SuiteClasses(value = { TimestampColumnTestCase1.class })


/**
 * Timestamp Column Statistics Test Suite
 * @author jagrut sharma
 *
 */
public class TimestampColumnTestSuite {
	
	@BeforeClass
	public static void setUpClass() {
		System.out.println("=== Starting run for TimestampColumnTestSuite ===");
	}

	@AfterClass
	public static void tearDownClass() {
		System.out.println("=== Completed run for TimestampColumnTestSuite ===");
	}

}