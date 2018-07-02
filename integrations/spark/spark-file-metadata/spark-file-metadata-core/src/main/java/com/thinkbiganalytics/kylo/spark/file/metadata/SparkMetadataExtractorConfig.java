package com.thinkbiganalytics.kylo.spark.file.metadata;

/*-
 * #%L
 * spark-file-metadata-core
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

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SparkMetadataExtractorConfig {

    /*
        https://spark.apache.org/docs/1.6.0/api/java/org/apache/spark/SparkContext.html#stop()
        https://spark.apache.org/docs/2.0.2/api/java/org/apache/spark/SparkContext.html#stop()
    */
    @Bean(destroyMethod = "stop")
    @Scope("prototype")
    public SparkContext sparkContext() {
        SparkConf conf = new SparkConf();
        return SparkContext.getOrCreate(conf);
    }

    @Bean
    public SQLContext sqlContext(final SparkContext sparkContext) {
        HiveContext hiveContext = new HiveContext(sparkContext);
        return hiveContext;
    }
}
