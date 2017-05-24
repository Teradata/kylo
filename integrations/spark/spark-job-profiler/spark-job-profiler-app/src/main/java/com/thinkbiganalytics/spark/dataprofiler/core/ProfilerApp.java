package com.thinkbiganalytics.spark.dataprofiler.core;

/*-
 * #%L
 * kylo-spark-job-profiler-app
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

import com.thinkbiganalytics.spark.dataprofiler.ProfilerConfiguration;
import com.thinkbiganalytics.spark.dataprofiler.StatisticsModel;
import com.thinkbiganalytics.spark.dataprofiler.columns.BigDecimalColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.BooleanColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ByteColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.DateColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.DoubleColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.FloatColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.IntegerColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.LongColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.ShortColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.StandardColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.StringColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.TimestampColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.columns.UnsupportedColumnStatistics;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputWriter;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataItem;
import com.thinkbiganalytics.spark.dataprofiler.topn.TopNDataList;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

@Configuration
public class ProfilerApp {

    @Bean
    public ProfilerConfiguration profilerConfiguration() {
        return new ProfilerConfiguration();
    }

    @Bean
    public SQLContext sqlContext(final ProfilerConfiguration profilerConfiguration) {
        SparkConf conf = new SparkConf();
        conf = configureEfficientSerialization(conf);

        HiveContext hiveContext = new HiveContext(new SparkContext(conf));
        hiveContext.setConf("spark.sql.dialect", profilerConfiguration.getSqlDialect());
        return hiveContext;
    }

    /**
     * Configure efficient serialization via Kryo.
     */
    @Nonnull
    private SparkConf configureEfficientSerialization(@Nonnull final SparkConf conf) {
        List<Class<?>> serializeClassesList;
        Class<?>[] serializeClassesArray;

        conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");

        serializeClassesList = new ArrayList<>();
        serializeClassesList.add(StandardColumnStatistics.class);
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
}
