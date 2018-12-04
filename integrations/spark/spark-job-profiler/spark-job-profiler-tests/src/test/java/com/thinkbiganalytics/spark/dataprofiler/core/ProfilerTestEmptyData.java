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
import com.thinkbiganalytics.spark.dataprofiler.ProfilerConfiguration;
import com.thinkbiganalytics.spark.dataprofiler.StatisticsModel;
import com.thinkbiganalytics.spark.dataprofiler.columns.StandardColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.config.ProfilerConfig;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
@ContextConfiguration(classes = {ProfilerConfig.class, SpringTestConfigV1.class, SpringTestConfigV2.class})
@ActiveProfiles("spark-v1")
public class ProfilerTestEmptyData {

    //columnStatsMap is static to be shared between multiple sub-classes
    protected static Map<Integer, StandardColumnStatistics> columnStatsMap;
    private JavaSparkContext sc;

    @Inject
    private com.thinkbiganalytics.spark.dataprofiler.Profiler profiler;

    @Inject
    private SparkContextService scs;

    @Inject
    private SQLContext sqlContext;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        if (columnStatsMap == null) {
            StructField[] schemaFields = new StructField[3];
            schemaFields[0] = DataTypes.createStructField("id", DataTypes.IntegerType, true);
            schemaFields[1] = DataTypes.createStructField("first_name", DataTypes.StringType, true);
            schemaFields[2] = DataTypes.createStructField("last_name", DataTypes.StringType, true);
            StructType schema = DataTypes.createStructType(schemaFields);

            final JavaSparkContext javaSparkContext = JavaSparkContext.fromSparkContext(sqlContext.sparkContext());
            JavaRDD dataRDD = javaSparkContext.emptyRDD();
            DataSet dataDF = scs.toDataSet(sqlContext.createDataFrame(dataRDD, schema));

            ProfilerConfiguration configuration = new ProfilerConfiguration();
            configuration.setNumberOfTopNValues(3);
            StatisticsModel statsModel = profiler.profile(dataDF, configuration);
            columnStatsMap = (statsModel != null) ? (Map) statsModel.getColumnStatisticsMap() : (Map<Integer, StandardColumnStatistics>) Collections.EMPTY_MAP;
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
