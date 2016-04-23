package com.thinkbiganalytics.spark.mock;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;

import java.io.Serializable;

/**
 * Created by Jeremy Merrifield on 3/25/16.
 */
public class SparkMockJob implements Serializable {

    /* Initialize Spark */
    private HiveContext hiveContext;
    private SQLContext sqlContext;

    public SparkMockJob() {
        super();
        SparkContext sparkContext = SparkContext.getOrCreate();
        hiveContext = new org.apache.spark.sql.hive.HiveContext(sparkContext);
        sqlContext = new SQLContext(sparkContext);
    }


    public static void main(String[] args) {
        try {
            SparkConf conf = new SparkConf().setAppName("Mock Spark Job");
            JavaSparkContext sc = new JavaSparkContext(conf);
            SparkMockJob app = new SparkMockJob();
            Thread.currentThread().sleep(5000);
            sc.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}