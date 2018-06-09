package com.thinkbiganalytics.kylo.spark.ingest;

/*-
 * #%L
 * Kylo Spark Ingest for Spark 1
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import com.thinkbiganalytics.kylo.catalog.KyloCatalog;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;

import org.apache.spark.SparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigSparkV1 {

    @Bean
    public KyloCatalogClient<DataFrame> kyloCatalogClient(SQLContext sqlContext) {
        return KyloCatalog.builder(sqlContext).build();
    }

    @Bean
    public SparkContext sparkContext() {
        return new SparkContext();
    }

    @Bean
    public SQLContext sqlContext(SparkContext sparkContext) {
        return new HiveContext(sparkContext);
    }
}
