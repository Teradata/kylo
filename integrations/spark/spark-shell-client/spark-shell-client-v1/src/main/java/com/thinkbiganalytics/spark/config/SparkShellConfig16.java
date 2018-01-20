package com.thinkbiganalytics.spark.config;

/*-
 * #%L
 * kylo-spark-shell-client-v1
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

import com.thinkbiganalytics.spark.metadata.TransformScript;
import com.thinkbiganalytics.spark.metadata.TransformScript16;
import com.thinkbiganalytics.spark.service.SparkListenerService;
import com.thinkbiganalytics.spark.service.SparkListenerService16;

import org.apache.spark.SparkContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

/**
 * Configures the Kylo Spark Shell for Spark 1.6.
 */
@Configuration
public class SparkShellConfig16 {

    /**
     * Creates a Spark listener service.
     */
    @Bean
    public SparkListenerService sparkListenerService(@Nonnull final SparkContext sparkContext) {
        return new SparkListenerService16(sparkContext);
    }

    /**
     * Gets the class for transform scripts.
     */
    @Bean
    public Class<? extends TransformScript> transformScriptClass() {
        return TransformScript16.class;
    }
}
