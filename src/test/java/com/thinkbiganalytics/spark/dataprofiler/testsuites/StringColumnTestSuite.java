package com.thinkbiganalytics.spark.dataprofiler.testsuites;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.thinkbiganalytics.spark.dataprofiler.testcases.StringColumnTestCase1;
import com.thinkbiganalytics.spark.dataprofiler.testcases.StringColumnTestCase2;
import com.thinkbiganalytics.spark.dataprofiler.testcases.StringColumnTestCase3;


@RunWith(value = Suite.class)
@SuiteClasses(value = { StringColumnTestCase1.class, StringColumnTestCase2.class, StringColumnTestCase3.class })

/**
 * String Column Statistics Test Suite
 * @author jagrut sharma
 *
 */
public class StringColumnTestSuite {
	
	@BeforeClass
	public static void setUpClass() {
		System.out.println("=== Starting run for StringColumnTestSuite ===");
	}

	@AfterClass
	public static void tearDownClass() {
		System.out.println("=== Completed run for StringColumnTestSuite ===");
	}

}