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
import com.thinkbiganalytics.spark.dataprofiler.columns.BigDecimalColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.BooleanColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ByteColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.DateColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.DoubleColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.FloatColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.IntegerColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.LongColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ShortColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.StringColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.TimestampColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.UnsupportedColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.model.SchemaInfo;
import com.thinkbiganalytics.spark.dataprofiler.model.StatisticsModel;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputWriter;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;
import com.thinkbiganalytics.spark.policy.FieldPolicyLoader;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.StructField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import scala.Tuple2;

/**
 * Generate data profile statistics for a table/query, and write result to a table
 */
@Configuration
public class Profiler {

    /* Schema lookup table */
    private Broadcast<Map<Integer, StructField>> bSchemaMap;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private ProfilerSparkContextService sparkContextService;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private ProfilerStrategy profilerStrategy;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private FieldPolicyLoader loader;

    private Map<String, FieldPolicy> policyMap = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(Profiler.class);

    /**
     * Main entry point into program
     *
     * @param args: list of args
     */
    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext("com.thinkbiganalytics.spark");
        Profiler app = ctx.getBean(Profiler.class);
        app.profile(args);
    }

    private void profile(String[] args) {

        /* Variables */
        SparkConf conf;
        JavaSparkContext sc;
        HiveContext hiveContext;
        DataSet resultDF;
        String queryString;

        /* Check command line arguments and get query to run. */
        if ((queryString = checkCommandLineArgs(args)) == null) {
            return;
        }

        /* Initialize and configure Spark */
        conf = new SparkConf().setAppName(ProfilerConfiguration.APP_NAME);

        if (ProfilerConfiguration.SERIALIZER.equals("kryo")) {
            conf = configureEfficientSerialization(conf);
        }

        sc = new JavaSparkContext(conf);
        hiveContext = new HiveContext(sc.sc());
        hiveContext.setConf("spark.sql.dialect", ProfilerConfiguration.SQL_DIALECT);

        /* Run query and get result */
        log.info("[PROFILER-INFO] Analyzing profile statistics for: [" + queryString + "]");
        resultDF = sparkContextService.sql(hiveContext, queryString);

        /* Update schema map and broadcast it*/
        bSchemaMap = populateAndBroadcastSchemaMap(resultDF, sc);

        /* Get profile statistics and write to table */
        profileStatistics(resultDF, bSchemaMap).writeModel(sc, hiveContext, sparkContextService);

        /* Wrap up */
        log.info("[PROFILER-INFO] Profiling finished.");
        sc.close();
    }


    public StatisticsModel profileStatistics(DataSet resultDF, Broadcast<Map<Integer, StructField>> bSchemaMap) {
        return profilerStrategy.profileStatistics(resultDF, bSchemaMap);
    }


    /**
     * Check command line arguments
     *
     * @param args list of command line arguments
     * @return query to run (null if invalid arguments)
     */
    public String checkCommandLineArgs(String[] args) {
        log.info("Running Spark Profiler with the following command line " + args.length + " args (comma separated): " + StringUtils.join(args, ","));
        if (args.length < 5) {
            log.error("Invalid number of command line arguments (" + args.length + ")");
            showCommandLineArgs();
            return null;
        }

        String retVal;

        String profileObjectType = args[0];
        String profileObjectDesc = args[1];
        Integer n = Integer.valueOf(args[2]);
        String profileOutputTable = args[3];
        String fieldPolicyJsonPath = args[4];
        policyMap = loader.loadFieldPolicy(fieldPolicyJsonPath);

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
                        profiledColumns.add(fieldPolicy.getField());
                    }
                }

                if (!profiledColumns.isEmpty()) {
                    retVal = "select " + StringUtils.join(profiledColumns, ',') + " from " + safeTable;
                    if (inputAndOutputTablePartitionKey != null && !"ALL".equalsIgnoreCase(inputAndOutputTablePartitionKey)) {
                        retVal += " where " + HiveUtils.quoteIdentifier(ProfilerConfiguration.INPUT_TABLE_PARTITION_COLUMN_NAME) + " = " + HiveUtils.quoteString(inputAndOutputTablePartitionKey);
                    }
                } else {
                    retVal = null;
                }
                break;
            case "query":
                retVal = profileObjectDesc;
                break;
            default:
                log.error("Illegal command line argument for object type (" + profileObjectType + ")");
                showCommandLineArgs();
                return null;
        }

        if (n <= 0) {
            log.error("Illegal command line argument for n for top_n values (" + n + ")");
            showCommandLineArgs();
            return null;
        } else {
            ProfilerConfiguration.NUMBER_OF_TOP_N_VALUES = n;
        }

        if (!setOutputTableDBAndName(profileOutputTable)) {
            log.error("Illegal command line argument for output table (" + profileOutputTable + ")");
            showCommandLineArgs();
            return null;
        }

        ProfilerConfiguration.INPUT_AND_OUTPUT_TABLE_PARTITION_KEY = inputAndOutputTablePartitionKey;

        return retVal;
    }


    /*
     * Set output database and table
     */
    private boolean setOutputTableDBAndName(String profileOutputTable) {

        Boolean retVal = true;
        String[] tableNameParts = profileOutputTable.split("\\.");

        if (tableNameParts.length == 1) {
            //output db remains as 'default'
            ProfilerConfiguration.OUTPUT_TABLE_NAME = tableNameParts[0];
        } else if (tableNameParts.length == 2) {
            ProfilerConfiguration.OUTPUT_DB_NAME = tableNameParts[0];
            ProfilerConfiguration.OUTPUT_TABLE_NAME = tableNameParts[1];
        } else {
            retVal = false;
        }

        return retVal;
    }


    /*
     * Configure efficient serialization via kryo
     */
    private SparkConf configureEfficientSerialization(SparkConf conf) {

        List<Class<?>> serializeClassesList;
        Class<?>[] serializeClassesArray;

        conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");

        serializeClassesList = new ArrayList<>();
        serializeClassesList.add(ColumnStatistics.class);
        serializeClassesList.add(BigDecimalColumnStatistics.class);
        serializeClassesList.add(BooleanColumnStatistics.class);
        serializeClassesList.add(ByteColumnStatistics.class);
        serializeClassesList.add(DateColumnStatistics.class);
        serializeClassesList.add(DoubleColumnStatistics.class);
        serializeClassesList.add(FloatColumnStatistics.class);
        serializeClassesList.add(IntegerColumnStatistics.class);
        serializeClassesList.add(LongColumnStatistics.class);
        serializeClassesList.add(ShortColumnStatistics.class);
        serializeClassesList.add(StringColumnStatistics.class);
        serializeClassesList.add(TimestampColumnStatistics.class);
        serializeClassesList.add(UnsupportedColumnStatistics.class);
        serializeClassesList.add(StatisticsModel.class);
        serializeClassesList.add(TopNDataItem.class);
        serializeClassesList.add(TopNDataList.class);
        serializeClassesList.add(OutputRow.class);
        serializeClassesList.add(OutputWriter.class);

        serializeClassesArray = new Class[serializeClassesList.size()];
        for (int i = 0; i < serializeClassesList.size(); i++) {
            serializeClassesArray[i] = serializeClassesList.get(i);
        }

        conf.registerKryoClasses(serializeClassesArray);
        return conf;
    }


    /*
     *  Print column value counts
     *  Only use for debugging, since output can be quite large in volume
     */
    @SuppressWarnings("unused")
    private void printColumnValueCounts(JavaPairRDD<Tuple2<Integer, Object>, Integer> columnValueCounts) {

        for (Tuple2<Tuple2<Integer, Object>, Integer> columnValueCount : columnValueCounts.collect()) {
            System.out.println(columnValueCount);
        }

    }


    /* Populate schema map */
    Broadcast<Map<Integer, StructField>> populateAndBroadcastSchemaMap(DataSet df, JavaSparkContext sc) {

        for (int i = 0; i < df.schema().fields().length; i++) {
            SchemaInfo.schemaMap.put(i, df.schema().fields()[i]);
        }

        bSchemaMap = sc.broadcast(SchemaInfo.schemaMap);
        return bSchemaMap;
    }


    /*
     * Print schema of query result
     * Only use for debugging, since this is already written out as result of profiling
     */
    @SuppressWarnings("unused")
    private void printSchema(StructField[] schemaFields) {

        System.out.println("=== Schema ===");
        System.out.println("[Field#\tName\tDataType\tNullable?\tMetadata]");
        for (int i = 0; i < schemaFields.length; i++) {
            String output = "Field #" + i + "\t"
                            + schemaFields[i].name() + "\t"
                            + schemaFields[i].dataType() + "\t"
                            + schemaFields[i].nullable() + "\t"
                            + schemaFields[i].metadata();

            System.out.println(output);
        }
    }


    /* Show required command-line arguments */
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
