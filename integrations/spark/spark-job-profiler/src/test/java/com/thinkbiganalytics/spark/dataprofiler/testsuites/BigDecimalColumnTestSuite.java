package com.thinkbiganalytics.spark.dataprofiler.testsuites;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.thinkbiganalytics.spark.dataprofiler.testcases.BigDecimalColumnTestCase1;

@RunWith(value = Suite.class)
@SuiteClasses(value = { BigDecimalColumnTestCase1.class })

/**
 * Big Decimal Column Statistics Test Suite
 * @author jagrut sharma
 *
 */
public class BigDecimalColumnTestSuite {
	
	@BeforeClass
	public static void setUpClass() {
		System.out.println("=== Starting run for BigDecimalColumnTestSuite ===");
	}

	@AfterClass
	public static void tearDownClass() {
		System.out.println("=== Completed run for BigDecimalColumnTestSuite ===");
	}

}