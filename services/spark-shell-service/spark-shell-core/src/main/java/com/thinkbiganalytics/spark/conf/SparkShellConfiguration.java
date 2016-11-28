package com.thinkbiganalytics.spark.conf;

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
