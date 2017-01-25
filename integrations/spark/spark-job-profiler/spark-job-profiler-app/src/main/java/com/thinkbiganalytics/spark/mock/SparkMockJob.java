package com.thinkbiganalytics.spark.mock;

/*-
 * #%L
 * thinkbig-spark-job-profiler-app
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;

import java.io.Serializable;

/**
 * Created by Jeremy Merrifield on 3/25/16.
 */
@SuppressWarnings("UnusedAssignment")
public class SparkMockJob implements Serializable {

    public SparkMockJob() {
        super();
        SparkContext sparkContext = SparkContext.getOrCreate();
        HiveContext hiveContext = new HiveContext(sparkContext);
        SQLContext sqlContext = new SQLContext(sparkContext);
    }


    @SuppressWarnings("AccessStaticViaInstance")
    public static void main(String[] args) {
        try {
            SparkConf conf = new SparkConf().setAppName("Mock Spark Job");
            JavaSparkContext sc = new JavaSparkContext(conf);
            SparkMockJob app = new SparkMockJob();
            Thread.currentThread().sleep(5000);
            sc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
