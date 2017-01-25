package com.thinkbiganalytics.spark.conf;

/*-
 * #%L
 * Spark Shell Core
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

import com.thinkbiganalytics.spark.conf.model.KerberosSparkProperties;
import com.thinkbiganalytics.spark.conf.model.SparkShellProperties;
import com.thinkbiganalytics.spark.shell.JerseySparkShellRestClient;
import com.thinkbiganalytics.spark.shell.ServerProcessManager;
import com.thinkbiganalytics.spark.shell.SparkShellProcessManager;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configures the Spark Shell controller for communicating with the Spark Shell process.
 */
@Configuration
@PropertySource("classpath:spark.properties")
public class SparkShellConfiguration {

    /**
     * Loads the properties for acquiring a Kerberos ticket.
     *
     * @return the Kerberos properties
     */
    @Bean
    @ConfigurationProperties("kerberos.spark")
    public KerberosSparkProperties kerberosSparkProperties() {
        return new KerberosSparkProperties();
    }

    /**
     * Creates a Spark Shell process manager for creating new Spark Shell instances.
     *
     * @param properties the Spark Shell properties
     * @return a Spark Shell process manager
     */
    @Bean
    public SparkShellProcessManager processManager(final SparkShellProperties properties) {
        return new ServerProcessManager(properties);
    }

    /**
     * Creates a REST client for communicating with the Spark Shell processes.
     *
     * @return a Spark Shell REST client
     */
    @Bean
    public SparkShellRestClient restClient() {
        return new JerseySparkShellRestClient();
    }

    /**
     * Loads the properties for the Spark Shell service.
     *
     * @return the Spark Shell properties
     */
    @Bean
    @ConfigurationProperties("spark.shell")
    public SparkShellProperties sparkShellproperties() {
        return new SparkShellProperties();
    }
}
