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

import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.dataprofiler.columns.ColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.model.StatisticsModel;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
@ContextConfiguration(classes = {Profiler.class, SpringTestConfigV1.class, SpringTestConfigV2.class})
@ActiveProfiles("spark-v1")
public abstract class ProfilerTest {

    public static final String EMPTY_STRING = "";
    public static final double epsilon = 0.0001d;
    public static final double epsilon2 = 3000.0d; //only used for long-variance, since they are extremely large numbers

    private JavaSparkContext sc;

    //columnStatsMap is static to be shared between multiple sub-classes
    protected static Map<Integer, ColumnStatistics> columnStatsMap;

    @Autowired
    private Profiler profiler;

    @SuppressWarnings("SpringJavaAutowiringInspection") //IntelliJ can't autowire for some reason
    @Autowired
    private SparkContextService scs;

    @Before
    public void setUp() {
        if (columnStatsMap == null) {
            StructField[] schemaFields = new StructField[15];
            schemaFields[0] = DataTypes.createStructField("id", DataTypes.IntegerType, true);
            schemaFields[1] = DataTypes.createStructField("firstname", DataTypes.StringType, true);
            schemaFields[2] = DataTypes.createStructField("lastname", DataTypes.StringType, true);
            schemaFields[3] = DataTypes.createStructField("age", DataTypes.IntegerType, true);
            schemaFields[4] = DataTypes.createStructField("description", DataTypes.StringType, true);
            schemaFields[5] = DataTypes.createStructField("height", DataTypes.DoubleType, true);
            schemaFields[6] = DataTypes.createStructField("joindate", DataTypes.DateType, true);
            schemaFields[7] = DataTypes.createStructField("lifemember", DataTypes.BooleanType, true);
            schemaFields[8] = DataTypes.createStructField("lastlogin", DataTypes.TimestampType, true);
            schemaFields[9] = DataTypes.createStructField("phash", DataTypes.LongType, true);
            schemaFields[10] = DataTypes.createStructField("weight", DataTypes.FloatType, true);
            schemaFields[11] = DataTypes.createStructField("credits", DataTypes.ShortType, true);
            schemaFields[12] = DataTypes.createStructField("ccode", DataTypes.ByteType, true);
            schemaFields[13] = DataTypes.createStructField("score", DataTypes.createDecimalType(7, 5), true);
            schemaFields[14] = DataTypes.createStructField("favoritepet", DataTypes.StringType, true);

            StructType schema = DataTypes.createStructType(schemaFields);

            List<Row> rows = new ArrayList<>();

            rows.add(RowFactory.create(
                1,
                "Jon",
                "Wright",
                14,
                "Jon::Wright",
                5.85d,
                Date.valueOf("2010-05-04"),
                Boolean.TRUE,
                Timestamp.valueOf("2008-05-06 23:10:10"),
                1456890911L,
                40.2f,
                (short) 100,
                (byte) 99,
                new BigDecimal(String.valueOf(1.567)),
                "Cat"));

            rows.add(RowFactory.create(
                2,
                "Jon",
                "Hudson",
                null,
                "Jon::Hudson",
                5.85d,
                Date.valueOf("1990-10-25"),
                null,
                Timestamp.valueOf("2011-01-08 11:25:45"),
                7638962135L,
                110.5f,
                (short) 100,
                (byte) 99,
                new BigDecimal(String.valueOf(8.223)),
                "alligator"));

            rows.add(RowFactory.create(
                3,
                "Rachael",
                "Hu",
                40,
                "Rachael::Hu",
                6.22d,
                Date.valueOf("1990-10-25"),
                Boolean.TRUE,
                Timestamp.valueOf("2011-01-08 11:25:45"),
                2988626110L,
                160.7f,
                (short) 1400,
                (byte) 99,
                new BigDecimal(String.valueOf(1.567)),
                "Alpaca"));

            rows.add(RowFactory.create(
                4,
                EMPTY_STRING,
                EMPTY_STRING,
                40,
                null,
                null,
                Date.valueOf("1956-11-12"),
                Boolean.TRUE,
                Timestamp.valueOf("2008-05-06 23:10:10"),
                2988626110L,
                null,
                null,
                (byte) 99,
                null,
                "Cat"));

            rows.add(RowFactory.create(
                5,
                "Rachael",
                EMPTY_STRING,
                22,
                "Rachael::",
                5.85d,
                Date.valueOf("2005-12-24"),
                Boolean.FALSE,
                Timestamp.valueOf("2008-05-06 23:10:10"),
                8260467621L,
                160.7f,
                (short) 100,
                null,
                new BigDecimal(String.valueOf(4.343)),
                "Zebra"));

            rows.add(RowFactory.create(
                6,
                "Elizabeth",
                "Taylor",
                40,
                "Elizabeth::Taylor",
                5.85d,
                Date.valueOf("2011-08-08"),
                null,
                Timestamp.valueOf("2016-01-14 14:20:20"),
                8732866249L,
                null,
                (short) 1400,
                null,
                new BigDecimal(String.valueOf(4.343)),
                "ZEBRA"));

            rows.add(RowFactory.create(
                7,
                "Jon",
                "Taylor",
                18,
                "Jon::Taylor",
                null,
                Date.valueOf("2011-08-08"),
                Boolean.TRUE,
                Timestamp.valueOf("2011-01-08 11:25:45"),
                2988626110L,
                110.5f,
                (short) 500,
                (byte) 40,
                new BigDecimal(String.valueOf(4.343)),
                null));

            rows.add(RowFactory.create(
                8,
                "Rachael",
                EMPTY_STRING,
                22,
                "Rachael::",
                4.37d,
                Date.valueOf("2011-08-08"),
                Boolean.FALSE,
                Timestamp.valueOf("2008-05-06 23:10:10"),
                8782348100L,
                null,
                null,
                null,
                null,
                "albatross"));

            rows.add(RowFactory.create(
                9,
                EMPTY_STRING,
                "Edmundson Jr",
                11,
                "::Edmundson Jr",
                4.88d,
                Date.valueOf("2007-06-07"),
                Boolean.FALSE,
                Timestamp.valueOf("2007-03-16 08:24:37"),
                null,
                155.3f,
                (short) 0,
                (byte) 99,
                new BigDecimal(String.valueOf(1.567)),
                EMPTY_STRING));

            rows.add(RowFactory.create(
                10,
                "Jon",
                EMPTY_STRING,
                65,
                "Jon::",
                null,
                Date.valueOf("1975-04-04"),
                Boolean.TRUE,
                Timestamp.valueOf("2007-03-16 08:24:31"),
                null,
                180.6f,
                (short) 5000,
                (byte) 2,
                new BigDecimal(String.valueOf(4.343)),
                "Cat"));

            String SPARK_MASTER = "local[*]";
            String APP_NAME = "Profiler Test";
            SparkConf conf = new SparkConf().setMaster(SPARK_MASTER).setAppName(APP_NAME);
            sc = new JavaSparkContext(conf);
            SQLContext sqlContext = new SQLContext(sc);

            JavaRDD<Row> dataRDD = sc.parallelize(rows);
            DataSet dataDF = scs.toDataSet(sqlContext.createDataFrame(dataRDD, schema));

            /* Enable to debug contents of test data */
            /*
            for (Row r: dataRDD.collect()) {
                System.out.println(r.toString());
            }
            */
            Broadcast<Map<Integer, StructField>> map = profiler.populateAndBroadcastSchemaMap(dataDF, sc);
            StatisticsModel statsModel = profiler.profileStatistics(dataDF, map);
            columnStatsMap = statsModel.getColumnStatisticsMap();
        }
    }

    @After
    public void tearDown() {
        if (sc != null) {
            sc.close();
            sc = null;
        }
    }

}
