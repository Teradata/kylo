package com.thinkbiganalytics.spark.datavalidator;

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

import com.thinkbiganalytics.policy.FieldPolicy;
import com.thinkbiganalytics.spark.policy.FieldPolicyLoader;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.storage.StorageLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.PrintStream;
import java.util.Map;

import javax.annotation.Nonnull;

public class Validator {

    private static final Logger log = LoggerFactory.getLogger(Validator.class);

    public static void main(String[] args) {
        if (log.isInfoEnabled()) {
            log.info("Running Spark Validator with the following command line args (comma separated):{}", StringUtils.join(args, ","));
        }

        try {
            final int result = new Validator().run(System.out, args);
            System.exit(result);
        } catch (final Exception e) {
            log.error("Failed to perform validation: ", e.toString(), e);
            System.out.println(e);
            System.exit(1);
        }
    }

    private int run(@Nonnull final PrintStream out, @Nonnull final String... args) {
        // Check how many arguments were passed in
        if (args.length < 4) {
            out.println("Proper Usage is: <targetDatabase> <entity> <partition> <path-to-policy-file>");
            out.println("You can optionally add: --hiveConf hive.setting=value --hiveConf hive.other.setting=value");
            out.println("You can optionally add: --storageLevel rdd_persistence_level_value");
            out.println("You can optionally add: --numPartitions number_of_rdd_partitions");
            out.println("You provided " + args.length + " args which are (comma separated): " + StringUtils.join(args, ","));
            return 1;
        }

        final ValidatorConfiguration params = new ValidatorConfiguration(args);

        // Initialize Spring context
        final ApplicationContext ctx = new AnnotationConfigApplicationContext("com.thinkbiganalytics.spark");
        final DataValidator app = ctx.getBean(DataValidator.class);

        // Prepare Hive context
        final HiveContext hiveContext = new HiveContext(SparkContext.getOrCreate());

        for (final Param param : params.getHiveParams()) {
            log.info("Adding Hive parameter {}={}", param.getName(), param.getValue());
            hiveContext.setConf(param.getName(), param.getValue());
        }

        log.info("Deployment Mode - {}", hiveContext.sparkContext().getConf().get("spark.submit.deployMode"));
        Map<String, FieldPolicy> policyMap = ctx.getBean(FieldPolicyLoader.class).loadFieldPolicy(params.getFieldPolicyJsonPath());

        // Run validation
        final DataValidatorResult results = app.validateTable(params.getTargetDatabase(), params.getFeedTableName(), params.getValidTableName(), params.getPartition(), params.getNumPartitions(),
                                                              policyMap, hiveContext);

        log.info("Persistence level: {}", params.getStorageLevel());
        results.persist(StorageLevel.fromString(params.getStorageLevel()));

        app.saveInvalidToTable(params.getTargetDatabase(), params.getInvalidTableName(), results, hiveContext);
        app.saveValidToTable(params.getTargetDatabase(), params.getFeedTableName(), params.getValidTableName(), results, hiveContext);
        app.saveProfileToTable(params.getTargetDatabase(), params.getProfileTableName(), params.getPartition(), results, hiveContext);
        results.unpersist();

        return 0;
    }
}
