package com.thinkbiganalytics.spark;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import com.thinkbiganalytics.spark.conf.LivyWranglerConfig;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 */
public class LivyWrangler {

    /**
     * Livy will initialize each session by calling this method
     *
     * @param sc the spark context that was created by Livy
     * @param sqlContext  the sqlContext as created by Livy
     * @return a spring application context with services needed for wrangling
     */
    public static ApplicationContext createSpringContext(SparkContext sc, SQLContext sqlContext) {
        LivyWranglerConfig.setSparkContext(sc);
        LivyWranglerConfig.setSqlContext(sqlContext);
        ApplicationContext context = new AnnotationConfigApplicationContext(new Class[]{LivyWranglerConfig.class});

        return context;
    }

} // end class
