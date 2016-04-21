package com.thinkbiganalytics.spark.dataprofiler.testsuites;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.thinkbiganalytics.spark.dataprofiler.testcases.IntegerColumnTestCase1;
import com.thinkbiganalytics.spark.dataprofiler.testcases.IntegerColumnTestCase2;

@RunWith(value = Suite.class)
@SuiteClasses(value = { IntegerColumnTestCase1.class, IntegerColumnTestCase2.class })


/**
 * Integer Column Statistics Test Suite
 * @author jagrut sharma
 *
 */
public class IntegerColumnTestSuite {
	
	@BeforeClass
	public static void setUpClass() {
		System.out.println("=== Starting run for IntegerColumnTestSuite ===");
	}

	@AfterClass
	public static void tearDownClass() {
		System.out.println("=== Completed run for IntegerColumnTestSuite ===");
	}

}