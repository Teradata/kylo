package com.thinkbiganalytics.spark.dataprofiler.testsuites;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.thinkbiganalytics.spark.dataprofiler.testcases.ShortColumnTestCase1;


@RunWith(value = Suite.class)
@SuiteClasses(value = { ShortColumnTestCase1.class })


/**
 * Short Column Statistics Test Suite
 * @author jagrut sharma
 *
 */
public class ShortColumnTestSuite {
	
	@BeforeClass
	public static void setUpClass() {
		System.out.println("=== Starting run for ShortColumnTestSuite ===");
	}

	@AfterClass
	public static void tearDownClass() {
		System.out.println("=== Completed run for ShortColumnTestSuite ===");
	}

}