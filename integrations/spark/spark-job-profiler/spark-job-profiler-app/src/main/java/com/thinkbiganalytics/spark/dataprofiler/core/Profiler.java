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

import com.google.common.collect.Lists;
import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.dataprofiler.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.ProfilerConfiguration;
import com.thinkbiganalytics.spark.dataprofiler.StatisticsModel;
import com.thinkbiganalytics.spark.dataprofiler.config.LivyProfilerConfig;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputWriter;
import com.thinkbiganalytics.spark.policy.FieldPolicyLoader;
import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        try (final ConfigurableApplicationContext ctx = new AnnotationConfigApplicationContext("com.thinkbiganalytics.spark")) {
            final Profiler profiler = new Profiler(ctx.getBean(FieldPolicyLoader.class), ctx.getBean(com.thinkbiganalytics.spark.dataprofiler.Profiler.class), ctx.getBean(ProfilerConfiguration.class),
                                                   ctx.getBean(SparkContextService.class), ctx.getBean(SQLContext.class));
            profiler.run(args);
        }
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
        try {
            /* Variables */
            ProfilerArguments profilerArgs = parseCommandLineArgs(args);

            /* Run query and get result */
            log.info("[PROFILER-INFO] Analyzing profile statistics for: [{}]", describeSql(profilerArgs));
            DataSet resultDF = getProfileDataset(profilerArgs);

            /* Get profile statistics and write to table */
            final StatisticsModel statisticsModel = profiler.profile(resultDF, profilerConfiguration);

            if (statisticsModel != null) {
                OutputWriter.writeModel(statisticsModel, profilerConfiguration, sqlContext, sparkContextService);
            } else {
                log.info("[PROFILER-INFO] No data to process. Hence, no profile statistics generated.");
            }

            /* Wrap up */
            log.info("[PROFILER-INFO] Profiling finished.");
        } catch (IllegalArgumentException e) {
            showCommandLineArgs();
        }
    }

    // should be called from spark REPL like this:
    // spark-shell --master local --conf spark.network.timeout=120s --conf spark.executor.cores=1 --conf spark.executor.instances=1 --conf spark.executor.memory=512m --conf spark.driver.memory=1024m --jars <SNIP>/kylo/integrations/spark/spark-job-profiler/spark-job-profiler-app/target/kylo-spark-job-profiler-app-0.9.1-SNAPSHOT.jar
    // spark> val exampleArgs = Array("table", "system.userdata1_valid", "10", "system.userdata1_profile", "/tmp/userdata1_field_policy.json", "1526946300553");
    // spark> Profiler.runProfiler( sc, sqlContext, exampleArgs )
    public static void runProfiler(SparkContext sc, SQLContext sqlContext, String [] args ) {
        ApplicationContext ctx = createSpringContext(sc, sqlContext);

        System.out.println(Arrays.asList(ctx.getBeanDefinitionNames()));

        final Profiler profiler = new Profiler(ctx.getBean(FieldPolicyLoader.class), ctx.getBean(com.thinkbiganalytics.spark.dataprofiler.Profiler.class), ctx.getBean(ProfilerConfiguration.class),
                ctx.getBean(SparkContextService.class), ctx.getBean(SQLContext.class));
        profiler.run(args);
    }


    public static List<OutputRow> profileDataFrame(SparkContextService sparkContextService,
                                                   com.thinkbiganalytics.spark.dataprofiler.Profiler profiler,
                                                   DataFrame dataFrame) {
        DataSet dataSet16 = sparkContextService.toDataSet(dataFrame);

        // Profile data set
        ProfilerConfiguration profilerConfiguration = new ProfilerConfiguration();
        profilerConfiguration.setNumberOfTopNValues(50);
        profilerConfiguration.setBins(35);
        final StatisticsModel dataStats = profiler.profile(dataSet16, profilerConfiguration);

        // Add stats to result
        if (dataStats != null) {
            List<OutputRow> profile = new ArrayList<OutputRow>(dataStats.getColumnStatisticsMap().size());

            for (final ColumnStatistics columnStats : dataStats.getColumnStatisticsMap().values()) {
                profile.addAll(columnStats.getStatistics());
            }
            return profile;
        }

        // no stats
        return Lists.newArrayList();
    }


    public static ApplicationContext createSpringContext(final SparkContext sc, final SQLContext sqlContext) {
        LivyProfilerConfig.setSparkContext(sc);
        LivyProfilerConfig.setSqlContext(sqlContext);
        ApplicationContext context = new AnnotationConfigApplicationContext(LivyProfilerConfig.class);
        return context;
    }

    /**
     * @return the configured SQL or the equivalent if table-based.
     */
    private Object describeSql(ProfilerArguments profilerArgs) {
        if (profilerArgs.isTableBased()) {
            String filter = getFilter(profilerArgs);
            return "select " + StringUtils.join(profilerArgs.getProfiledColumns(), ",") + (filter != null ? " where " + filter : "");
        } else {
            return profilerArgs.getSql();
        }
    }

    /**
     * Check command line arguments
     *
     * @param args list of command line arguments
     * @return query to run (null if invalid arguments)
     */
    @Nullable
    private ProfilerArguments parseCommandLineArgs(final String[] args) {
        ProfilerArguments profilerArgs = new ProfilerArguments(args, this.loader);
        profilerConfiguration.setInputAndOutputTablePartitionKey(profilerArgs.getInputAndOutputTablePartitionKey());
        
        if (!setOutputTableDBAndName(profilerArgs.getProfileOutputTable(), profilerConfiguration)) {
            log.error("Illegal command line argument for output table ({})", profilerArgs.getProfileOutputTable());
            throw new IllegalArgumentException("Illegal command line argument for output table (" + profilerArgs.getProfileOutputTable() + ")");
        }
        
        return profilerArgs;
    }
    
    private DataSet getValidDataSet(ProfilerArguments args) {
        return this.sparkContextService.toDataSet(this.sqlContext, args.getTable());
    }
    
    private DataSet getProfileDataset(ProfilerArguments args) {
        if (args.isTableBased()) {
            DataSet valid = getValidDataSet(args);
            String filter = getFilter(args);
            
            if (filter != null) {
                return valid.select(toSelectColumns(args)).filter(filter).drop(this.profilerConfiguration.getInputTablePartitionColumnName());
            } else {
                return valid.select(toSelectColumns(args)).drop(this.profilerConfiguration.getInputTablePartitionColumnName());
            }
        } else {
            return this.sparkContextService.sql(this.sqlContext, args.getSql());
        }
    }
    
    private Column[] toSelectColumns(ProfilerArguments args) {
        List<String> profiledCols = args.getProfiledColumns();
        
        if (args.getInputAndOutputTablePartitionKey() != null && ! "ALL".equalsIgnoreCase(args.getInputAndOutputTablePartitionKey())) {
            profiledCols.add(HiveUtils.quoteIdentifier(profilerConfiguration.getInputTablePartitionColumnName()));
        }
        
        Column[] columns = new Column[profiledCols.size()];
        
        for (int idx = 0; idx < profiledCols.size(); idx++) {
            Column column = new Column(profiledCols.get(idx));
            columns[idx] = column;
        }
        
        return columns;
    }

    private String getFilter(ProfilerArguments args) {
        if (args.isTableBased()) {
            if (! args.getProfiledColumns().isEmpty()) {
                if (args.getInputAndOutputTablePartitionKey() != null && ! "ALL".equalsIgnoreCase(args.getInputAndOutputTablePartitionKey())) {
                    return HiveUtils.quoteIdentifier(profilerConfiguration.getInputTablePartitionColumnName()) + " = " + HiveUtils.quoteString(args.getInputAndOutputTablePartitionKey());
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return args.getSql();
        }
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
