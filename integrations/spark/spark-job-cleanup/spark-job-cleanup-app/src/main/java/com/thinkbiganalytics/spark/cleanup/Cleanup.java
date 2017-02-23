package com.thinkbiganalytics.spark.cleanup;

/*-
 * #%L
 * kylo-spark-job-cleanup-app
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

import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.hive.HiveContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Perform cleanup
 */

/*
    TODO: Implement full functionality

    This implementation provides the skeleton layout to enable implementation of the full functionality.
     It demonstrates using Spark to run a row count for a schema.table in Hive.
     Tested on both Spark 1 and Spark 2.
     Please refer to README for commands to run application.
 */

@Component
public class Cleanup {

    private static final Logger log = LoggerFactory.getLogger(Cleanup.class);

    @Autowired
    private SparkContextService scs;

    private HiveContext hiveContext;
    private String categoryName;
    private String feedName;

    public static void main(String[] args) {
        log.info("Running Cleanup with these command line args: " + StringUtils.join(args, ","));

        if (args.length < 2) {
            System.out.println("Expected command line args: <hive-schema-name> <hive-table-name>");
            System.exit(1);
        }

        try {
            ApplicationContext ctx = new AnnotationConfigApplicationContext("com.thinkbiganalytics.spark");
            Cleanup app = ctx.getBean(Cleanup.class);
            app.setArguments(args[0], args[1]);
            app.doCleanup();
        } catch (Exception e) {
            log.error("Failed to perform cleanup: {}", e.getMessage());
            System.exit(1);
        }

        log.info("Cleanup has finished.");
    }

    public void setArguments(String categoryName, String feedName) {
        this.categoryName = categoryName;
        this.feedName = feedName;
    }

    protected HiveContext getHiveContext() {
        return hiveContext;
    }

    public void doCleanup() {
        try {
            SparkContext sparkContext = SparkContext.getOrCreate();
            hiveContext = new org.apache.spark.sql.hive.HiveContext(sparkContext);

            String sql = "SELECT COUNT(*) FROM " + categoryName + "." + feedName;
            log.info("Executing query {}", sql);
            DataSet dataFrame = scs.sql(getHiveContext(), sql);
            List<Row> resultRows = dataFrame.collectAsList();
            long rowCount = 0;
            if (resultRows.size() > 0) {
                rowCount = resultRows.get(0).getLong(0);
            }
            log.info("Total rows in {}.{}: {}", categoryName, feedName, rowCount);
        } catch (Exception e) {
            log.error("An error occurred during running cleanup: {}", e.getMessage());
            System.exit(1);
        }
    }
}
