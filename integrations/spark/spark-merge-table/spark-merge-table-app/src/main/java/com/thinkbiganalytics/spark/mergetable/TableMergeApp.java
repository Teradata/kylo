package com.thinkbiganalytics.spark.mergetable;

/*-
 * #%L
 * kylo-spark-validate-cleanse-app
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

import com.thinkbiganalytics.spark.mergetable.TableMergeConfig.MergeStrategy;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.hive.HiveContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.PrintStream;

import javax.annotation.Nonnull;

public class TableMergeApp {

    private static final Logger log = LoggerFactory.getLogger(TableMergeApp.class);

    public static void main(String[] args) {
        if (log.isInfoEnabled()) {
            log.info("Running Spark TableMergeApp with the following command line args (comma separated):{}", StringUtils.join(args, ","));
        }
        
        try {
            new TableMergeApp().run(System.out, args);
        } finally {
            // The context being stopped was created in the run method.
            SparkContext.getOrCreate().stop();
        }
    }

    private void run(@Nonnull final PrintStream out, @Nonnull final String... args) {
        log.info("TableMergeApp running...");
        final SparkContext sparkContext = SparkContext.getOrCreate();
        
        try {
            final TableMergeArguments mergeArgs = new TableMergeArguments(args);

            // Initialize Spring context
            try (final ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext("com.thinkbiganalytics.spark")) {
                final TableMerger merger = ctx.getBean(TableMerger.class);
    
                // Prepare Hive context
                final HiveContext hiveContext = getHiveContext(sparkContext);
                
                switch (mergeArgs.getStrategy()) {
                    case MERGE:
                    case DEDUPE_AND_MERGE:
                        merger.merge(hiveContext, mergeArgs, mergeArgs.getPartitionValue(), mergeArgs.getStrategy() == MergeStrategy.DEDUPE_AND_MERGE);
                        break;
                    case SYNC:
                    case ROLLING_SYNC:
                        merger.synchronize(hiveContext, mergeArgs, mergeArgs.getPartitionValue(), mergeArgs.getStrategy() == MergeStrategy.ROLLING_SYNC);
                        break;
                    case PK_MERGE:
                        merger.mergeOnPrimaryKey(hiveContext, mergeArgs, mergeArgs.getPartitionValue());
                        break;
                }
            }
            
            log.info("TableMergeApp finished");
        } catch (Exception e) {
            log.error("Failed to perform validation: {}", e.toString(), e);
            throw e;
        }
    }

    private HiveContext getHiveContext(final SparkContext sparkContext) {
        HiveContext context = new HiveContext(sparkContext);
        context.setConf("hive.exec.dynamic.partition", "true");
        context.setConf("hive.exec.dynamic.partition.mode", "nonstrict");
        context.setConf("hive.optimize.index.filter", "false");
        return context;
    }
}
