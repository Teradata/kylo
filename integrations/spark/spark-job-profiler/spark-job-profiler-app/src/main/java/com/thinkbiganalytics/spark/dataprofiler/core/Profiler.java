package com.thinkbiganalytics.spark.dataprofiler.core;

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

import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.policy.FieldPolicy;
import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.dataprofiler.ProfilerConfiguration;
import com.thinkbiganalytics.spark.dataprofiler.StatisticsModel;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputWriter;
import com.thinkbiganalytics.spark.policy.FieldPolicyLoader;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.sql.SQLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Generate data profile statistics for a table/query, and write result to a table
 */
public class Profiler {

    private static final Logger log = LoggerFactory.getLogger(Profiler.class);

    private FieldPolicyLoader loader;

    private com.thinkbiganalytics.spark.dataprofiler.Profiler profiler;

    private ProfilerConfiguration profilerConfiguration;

    private SparkContextService sparkContextService;

    private SQLContext sqlContext;

    /**
     * Main entry point into program
     *
     * @param args: list of args
     */
    public static void main(String[] args) {
        final ApplicationContext ctx = new AnnotationConfigApplicationContext("com.thinkbiganalytics.spark");
        final Profiler profiler = new Profiler(ctx.getBean(FieldPolicyLoader.class), ctx.getBean(com.thinkbiganalytics.spark.dataprofiler.Profiler.class), ctx.getBean(ProfilerConfiguration.class),
                                               ctx.getBean(SparkContextService.class), ctx.getBean(SQLContext.class));
        profiler.run(args);
    }

    public Profiler(FieldPolicyLoader loader, com.thinkbiganalytics.spark.dataprofiler.Profiler profiler, ProfilerConfiguration profilerConfiguration,
                    SparkContextService sparkContextService, SQLContext sqlContext) {
        this.loader = loader;
        this.profiler = profiler;
        this.profilerConfiguration = profilerConfiguration;
        this.sparkContextService = sparkContextService;
        this.sqlContext = sqlContext;
    }

    public void run(String[] args) {
        /* Variables */
        DataSet resultDF;
        String queryString;

        /* Check command line arguments and get query to run. */
        if ((queryString = checkCommandLineArgs(args)) == null) {
            return;
        }

        /* Run query and get result */
        log.info("[PROFILER-INFO] Analyzing profile statistics for: [{}]", queryString);
        resultDF = sparkContextService.sql(sqlContext, queryString);

        /* Get profile statistics and write to table */
        final StatisticsModel statisticsModel = profiler.profile(resultDF, profilerConfiguration);

        if (statisticsModel != null) {
            OutputWriter.writeModel(statisticsModel, profilerConfiguration, sqlContext, sparkContextService);
        } else {
            log.info("[PROFILER-INFO] No data to process. Hence, no profile statistics generated.");
        }

        /* Wrap up */
        log.info("[PROFILER-INFO] Profiling finished.");
    }

    /**
     * Check command line arguments
     *
     * @param args list of command line arguments
     * @return query to run (null if invalid arguments)
     */
    @Nullable
    private String checkCommandLineArgs(final String[] args) {
        if (log.isInfoEnabled()) {
            log.info("Running Spark Profiler with the following command line {} args (comma separated): {}", args.length, StringUtils.join(args, ","));
        }

        if (args.length < 5) {
            log.error("Invalid number of command line arguments ({})", args.length);
            showCommandLineArgs();
            return null;
        }

        String retVal;

        String profileObjectType = args[0];
        String profileObjectDesc = args[1];
        Integer n = Integer.valueOf(args[2]);
        String profileOutputTable = args[3];
        String fieldPolicyJsonPath = args[4];
        Map<String, FieldPolicy> policyMap = loader.loadFieldPolicy(fieldPolicyJsonPath);

        String inputAndOutputTablePartitionKey = "ALL";

        if (args.length >= 6) {
            inputAndOutputTablePartitionKey = args[5];
        }

        switch (profileObjectType) {
            case "table":
                // Quote source table
                final String[] tableRef = profileObjectDesc.split("\\.", 2);
                final String safeTable = tableRef.length == 1 ? HiveUtils.quoteIdentifier(tableRef[0]) : HiveUtils.quoteIdentifier(tableRef[0], tableRef[1]);

                // Create SQL
                List<String> profiledColumns = new ArrayList<>();
                for (FieldPolicy fieldPolicy : policyMap.values()) {
                    if (fieldPolicy.isProfile()) {
                        profiledColumns.add(HiveUtils.quoteIdentifier(fieldPolicy.getField().toLowerCase()));
                    }
                }

                if (!profiledColumns.isEmpty()) {
                    retVal = "select " + StringUtils.join(profiledColumns, ',') + " from " + safeTable;
                    if (inputAndOutputTablePartitionKey != null && !"ALL".equalsIgnoreCase(inputAndOutputTablePartitionKey)) {
                        retVal += " where " + HiveUtils.quoteIdentifier(profilerConfiguration.getInputTablePartitionColumnName()) + " = " + HiveUtils.quoteString(inputAndOutputTablePartitionKey);
                    }
                } else {
                    retVal = null;
                }
                break;
            case "query":
                retVal = profileObjectDesc;
                break;
            default:
                log.error("Illegal command line argument for object type ({})", profileObjectType);
                showCommandLineArgs();
                return null;
        }

        if (n <= 0) {
            log.error("Illegal command line argument for n for top_n values ({})", n);
            showCommandLineArgs();
            return null;
        } else {
            profilerConfiguration.setNumberOfTopNValues(n);
        }

        if (!setOutputTableDBAndName(profileOutputTable, profilerConfiguration)) {
            log.error("Illegal command line argument for output table ({})", profileOutputTable);
            showCommandLineArgs();
            return null;
        }

        profilerConfiguration.setInputAndOutputTablePartitionKey(inputAndOutputTablePartitionKey);

        return retVal;
    }


    /*
     * Set output database and table
     */
    private boolean setOutputTableDBAndName(@Nonnull final String profileOutputTable, @Nonnull final ProfilerConfiguration profilerConfiguration) {

        Boolean retVal = true;
        String[] tableNameParts = profileOutputTable.split("\\.");

        if (tableNameParts.length == 1) {
            //output db remains as 'default'
            profilerConfiguration.setOutputTableName(tableNameParts[0]);
        } else if (tableNameParts.length == 2) {
            profilerConfiguration.setOutputDbName(tableNameParts[0]);
            profilerConfiguration.setOutputTableName(tableNameParts[1]);
        } else {
            retVal = false;
        }

        return retVal;
    }

    /**
     * Show required command-line arguments.
     */
    private void showCommandLineArgs() {
        log.info("*** \nInfo: Required command line arguments:\n"
                 + "1. object type: valid values are {table, query}\n"
                 + "2. object description: valid values are {<database.table>, <query>}\n"
                 + "3. n for top_n values: valid value is {<integer>}\n"
                 + "4. output table: valid values are {<table>, <database.table>}"
                 + "5. full path to policy file "
                 + "\n"
                 + "Info: Optional command line argument:\n"
                 + "6. partition_key: valid value is {<string>}\n\n"
                 + "(Note: Only alphanumeric and underscore characters for table names and partition key)"
                 + "\n***");
    }
}
