package com.thinkbiganalytics.kylo.spark.config;

/*-
 * #%L
 * kylo-spark-livy-server
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
import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.kylo.spark.client.DefaultLivyClient;
import com.thinkbiganalytics.kylo.spark.client.LivyClient;
import com.thinkbiganalytics.kylo.spark.client.ReliableLivyClient;
import com.thinkbiganalytics.kylo.spark.client.jersey.LivyRestClient;
import com.thinkbiganalytics.kylo.spark.client.livy.LivyHeartbeatMonitor;
import com.thinkbiganalytics.kylo.spark.client.model.LivyServer;
import com.thinkbiganalytics.kylo.spark.cluster.DefaultSparkShellClusterListener;
import com.thinkbiganalytics.kylo.spark.livy.SparkLivyProcessManager;
import com.thinkbiganalytics.kylo.spark.livy.SparkLivyRestClient;
import com.thinkbiganalytics.kylo.utils.KerberosUtils;
import com.thinkbiganalytics.kylo.utils.LivyRestModelTransformer;
import com.thinkbiganalytics.kylo.utils.ProcessRunner;
import com.thinkbiganalytics.kylo.utils.ScalaScriptService;
import com.thinkbiganalytics.kylo.utils.ScriptGenerator;
import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.spark.conf.model.KerberosSparkProperties;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;

import org.apache.hadoop.fs.FileSystem;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

@Configuration
@PropertySource(value = {"classpath:sparkDefaults.properties", "classpath:spark.properties", "classpath:sparkDevOverride.properties"}, ignoreResourceNotFound = true)
@ImportResource("classpath:/config/applicationContext-livy.xml")
@EnableConfigurationProperties  // needed for populating configuration POJOs
@AllProfiles({"kylo-livy", "!kyloUpgrade"})
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


    @Bean
    public DefaultLivyClient defaultLivyClient() {
        LivyProperties livyProperties = livyProperties();
        return new DefaultLivyClient(livyServer(livyProperties), livyProperties);
    }

    @Bean
    public LivyClient livyClient() {
        return new ReliableLivyClient(defaultLivyClient());
    }

    /**
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
        Cache<String, Integer> cache = LivyRestModelTransformer.statementIdCache;
        return cache.asMap();
    }

    /**
     * Listens for spark process events and updates the cluster.
     */
    @Bean
    @ConditionalOnProperty("kylo.cluster.jgroupsConfigFile")
    public DefaultSparkShellClusterListener clusterListener(final ClusterService clusterService, final SparkLivyProcessManager sparkLivyProcessManager) {
        final DefaultSparkShellClusterListener clusterListener = new DefaultSparkShellClusterListener(clusterService, sparkLivyProcessManager);
        sparkLivyProcessManager.addListener(clusterListener);
        return clusterListener;
    }

    @Bean
    public LivyHeartbeatMonitor livyHeartbeatMonitor() {
        // LivyHeartbeatMonitor gets it's own jersey client
        LivyProperties livyProperties = livyProperties();
        final JerseyClientConfig config = new JerseyClientConfig();
        config.setHost(livyProperties.getHostname());
        config.setPort(livyProperties.getPort());

        if (livyProperties().getTruststorePassword() != null) {
            config.setHttps(true);
            config.setTruststorePath(livyProperties.getTruststorePath());
            config.setTruststorePassword(livyProperties.getTruststorePassword());
            config.setTrustStoreType(livyProperties.getTruststoreType());
        } // end if

        LivyRestClient.setKerberosSparkProperties(kerberosSparkProperties());  // all clients will have kerberos
        JerseyRestClient livyRestClient = new LivyRestClient(config);

        return new LivyHeartbeatMonitor(livyClient(), livyRestClient, livyServer(livyProperties()), livyProperties());
    }

    /**
     * status kept up to date by:  Heartbeat thread.  Entries are removed if Livy no longer aware of the session
     */
    @Bean
    public LivyServer livyServer(LivyProperties livyProperties) {
        return new LivyServer(livyProperties.getHostname(), livyProperties.getPort());
    }


    /* Do any property validation/manipulation here */
    @PostConstruct
    public void postConstruct() {
        // find a suitable kinit path
        KerberosSparkProperties ksp = kerberosSparkProperties();
        if (ksp.isKerberosEnabled()) {
            File actualKinitPath = kerberosUtils().getKinitPath(ksp.getKinitPath());
            if (actualKinitPath == null) {
                throw new IllegalStateException("Kerberos is enabled yet cannot find a valid path for kinit.  Check 'kerberos.spark.kinitPath' property");
            }
            ksp.setKinitPath(actualKinitPath);
        }
    }

}
