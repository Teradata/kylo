package com.thinkbiganalytics.spark.dataprofiler.config;

/*-
 * #%L
 * kylo-spark-job-profiler-app
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;
import org.springframework.context.annotation.*;

@Profile("kylo-livy")
@Configuration
@ComponentScan(basePackages = {"com.thinkbiganalytics.spark"}, excludeFilters={
        @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=SparkContext.class),
        @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=SQLContext.class),
})
public class LivyProfilerConfig {
    private static SparkContext sparkContext;
    private static SQLContext sqlContext;

    public static void setSparkContext(SparkContext sparkContext) {
        LivyProfilerConfig.sparkContext = sparkContext;
    }

    public static void setSqlContext(SQLContext sqlContext) {
        LivyProfilerConfig.sqlContext = sqlContext;
    }

    @Bean
    public SparkContext sparkContext() {
        return sparkContext;
    }

    @Bean
    public SQLContext sqlContext() {
        return sqlContext;
    }

}
