package com.thinkbiganalytics.spark.conf.model;

/*-
 * #%L
 * kylo-commons-spark-shell-plugin-shared
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

import com.thinkbiganalytics.spark.scala.ScalaImportManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import java.util.Map;

import javax.annotation.Resource;



@Configuration
@ImportResource("classpath:/config/applicationContext-sharedSparkShellPlugin.xml")
public class SparkShellCommonConfig {
    @Resource
    private Map<String,String> optimizedImports;

    @Bean
    public ScalaImportManager scalaImportManager() {
        return new ScalaImportManager(optimizedImports);
    }
}
