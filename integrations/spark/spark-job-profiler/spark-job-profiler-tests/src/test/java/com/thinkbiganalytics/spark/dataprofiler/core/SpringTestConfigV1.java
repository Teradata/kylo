package com.thinkbiganalytics.spark.dataprofiler.core;

/*-
 * #%L
 * thinkbig-spark-job-profiler-tests
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

import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.SparkContextService16;
import com.thinkbiganalytics.spark.policy.FieldPolicyLoader;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
@Profile("spark-v1")
public class SpringTestConfigV1 {

    @Bean
    public FieldPolicyLoader fieldPolicyLoader() {
        return new FieldPolicyLoader();
    }

    @Bean
    public SparkContextService sparkContextService() {
        return new SparkContextService16();
    }

    @Bean
    public SQLContext sqlContext() {
        final SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("Profiler Test");
        final SparkContext sc = new SparkContext(conf);
        return new SQLContext(sc);
    }
}
