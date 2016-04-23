package com.thinkbiganalytics.spark.dataprofiler.testsuites;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.thinkbiganalytics.spark.dataprofiler.testcases.LongColumnTestCase1;

@RunWith(value = Suite.class)
@SuiteClasses(value = { LongColumnTestCase1.class })


/**
 * Long Column Statistics Test Suite
 * @author jagrut sharma
 *
 */
public class LongColumnTestSuite {
	
	@BeforeClass
	public static void setUpClass() {
		System.out.println("=== Starting run for LongColumnTestSuite ===");
	}

	@AfterClass
	public static void tearDownClass() {
		System.out.println("=== Completed run for LongColumnTestSuite ===");
	}

}