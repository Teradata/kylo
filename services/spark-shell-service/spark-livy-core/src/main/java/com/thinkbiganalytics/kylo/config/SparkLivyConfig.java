package com.thinkbiganalytics.kylo.config;

/*-
 * #%L
 * kylo-spark-livy-core
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thinkbiganalytics.kylo.spark.livy.SparkLivyProcessManager;
import com.thinkbiganalytics.kylo.spark.livy.SparkLivyRestClient;
import com.thinkbiganalytics.kylo.utils.*;
import com.thinkbiganalytics.spark.conf.model.KerberosSparkProperties;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@PropertySource(value = {"classpath:spark.properties"}, ignoreResourceNotFound = true)
@ImportResource("classpath:/config/applicationContext-livy.xml")
@EnableAutoConfiguration  // needed for populating configuration POJOs
@AllProfiles( { "kylo-livy", "!kyloUpgrade" })
public class SparkLivyConfig {
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
     * Livy properties are properties not shared with spark shell
     */
    @Bean
    @ConfigurationProperties("spark.livy")
    public LivyProperties livyProperties() {
        return new LivyProperties();
    }

    /**
     * @return a Spark Livy client
     * @implNote required to start Kylo
     */
    @Bean
    public SparkShellRestClient restClient() {
        return new SparkLivyRestClient();
    }

    /**
     * @return
     * @implNote required to start Kylo
     */
    @Bean
    public SparkLivyProcessManager sparkShellProcessManager() {
        return new SparkLivyProcessManager();
    }

    @Bean
    public ScriptGenerator scriptGenerator() {
        return new ScriptGenerator();
    }

    @Bean
    public ScalaScriptService scalaScriptService() {
        return new ScalaScriptService();
    }

    /**
     * Gets the Hadoop File System.
     */
    @Bean
    public FileSystem fileSystem() throws IOException {
        return FileSystem.get(new org.apache.hadoop.conf.Configuration());
    }

    @Bean
    public ProcessRunner processRunner() {
        return new ProcessRunner();
    }

    @Bean
    public KerberosUtils kerberosUtils() {
        return new KerberosUtils();
    }


    @Bean
    public Map<String /* transformId */, Integer /* stmntId */> statementIdCache() {
         Cache<String,Integer> cache = LivyRestModelTransformer.statementIdCache;
         return cache.asMap();
    }

    /* Do any property validation/manipulation here */
    @PostConstruct
    public  void postConstruct() {
        // find a suitable kinit path
        KerberosSparkProperties ksp = kerberosSparkProperties();
        if( ksp.isKerberosEnabled() ) {
            File actualKinitPath = kerberosUtils().getKinitPath(ksp.getKinitPath());
            if( actualKinitPath == null ) {
                throw new IllegalStateException("Kerberos is enabled yet cannot find a valid path for kinit.  Check 'kerberos.spark.kinitPath' property");
            }
            ksp.setKinitPath(actualKinitPath);
        }
    }

}
