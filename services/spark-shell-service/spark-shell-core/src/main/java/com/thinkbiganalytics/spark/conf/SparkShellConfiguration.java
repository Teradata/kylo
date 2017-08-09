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

import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.spark.conf.model.KerberosSparkProperties;
import com.thinkbiganalytics.spark.conf.model.SparkShellProperties;
import com.thinkbiganalytics.spark.shell.DefaultProcessManager;
import com.thinkbiganalytics.spark.shell.JerseySparkShellRestClient;
import com.thinkbiganalytics.spark.shell.MultiUserProcessManager;
import com.thinkbiganalytics.spark.shell.ServerProcessManager;
import com.thinkbiganalytics.spark.shell.SparkShellProcessManager;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;
import com.thinkbiganalytics.spark.shell.cluster.SparkShellClusterDelegate;
import com.thinkbiganalytics.spark.shell.cluster.SparkShellClusterListener;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.Ssl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * Configures the Spark Shell controller for communicating with the Spark Shell process.
 */
@Configuration
@PropertySource("classpath:spark.properties")
public class SparkShellConfiguration {

    /**
     * Listens for cluster events and updates the process manager.
     */
    @Bean
    @ConditionalOnBean(SparkShellClusterDelegate.class)
    public SparkShellClusterListener clusterListener(final ClusterService clusterService, final SparkShellClusterDelegate delegate) {
        final SparkShellClusterListener clusterListener = new SparkShellClusterListener(clusterService, delegate);
        if (delegate instanceof SparkShellProcessManager) {
            ((SparkShellProcessManager) delegate).addListener(clusterListener);
        }
        return clusterListener;
    }

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
     * @param sparkShellProperties the Spark Shell properties
     * @param kerberosProperties   the Kerberos properties for the Spark Shell client
     * @param users                mapping of username to password
     * @return a Spark Shell process manager
     */
    @Bean
    public SparkShellProcessManager processManager(final SparkShellProperties sparkShellProperties, final KerberosSparkProperties kerberosProperties,
                                                   @Qualifier("sparkLoginUsers") final Optional<Properties> users) {
        if (sparkShellProperties.getServer() != null) {
            return new ServerProcessManager(sparkShellProperties);
        } else if (!users.isPresent()) {
            throw new IllegalArgumentException("Invalid Spark configuration. Either set spark.shell.server.host and spark.shell.server.port in spark.properties or add the auth-spark Spring profile"
                                               + " to application.properties.");
        } else if (sparkShellProperties.isProxyUser()) {
            return new MultiUserProcessManager(sparkShellProperties, kerberosProperties, users.get());
        } else {
            return new DefaultProcessManager(sparkShellProperties, kerberosProperties, users.get());
        }
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
    public SparkShellProperties sparkShellProperties(@Nonnull final ServerProperties server) {
        final SparkShellProperties properties = new SparkShellProperties();

        // Automatically determine registration url
        if (properties.getRegistrationUrl() == null) {
            // Get protocol
            final String protocol = Optional.ofNullable(server.getSsl()).map(Ssl::isEnabled).orElse(false) ? "https" : "http";

            // Get hostname
            String address = null;
            try {
                address = (server.getAddress() != null) ? server.getAddress().getHostName() : InetAddress.getLocalHost().getHostName();
            } catch (final UnknownHostException e) {
                // ignored
            }

            // Set registration url
            if (address != null) {
                properties.setRegistrationUrl(protocol + "://" + address + ":8400/proxy/v1/spark/shell/register");
            }
        }

        return properties;
    }
}
