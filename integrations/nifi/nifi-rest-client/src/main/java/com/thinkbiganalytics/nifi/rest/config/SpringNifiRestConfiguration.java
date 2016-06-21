package com.thinkbiganalytics.nifi.rest.config;

import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * Created by sr186054 on 4/19/16.
 */
@Configuration
@EnableAspectJAutoProxy
public class SpringNifiRestConfiguration {

    @Autowired
    private Environment env;

    @Bean(name = "nifiRestClient")
    public NifiRestClient nifiRestClient(@Qualifier("nifiRestClientConfig") NifiRestClientConfig clientConfig) {

        return new NifiRestClient(clientConfig);

    }

    @Bean(name = "nifiRestClientConfig")
    public NifiRestClientConfig nifiRestClientConfig() {
        String host = env.getProperty("nifi.rest.host");
        String configPort = env.getProperty("nifi.rest.port");
        Integer port = null;
        try {
            port = Integer.parseInt(configPort);
        } catch (NumberFormatException e) {
            Assert.notNull(port,
                           "The Nifi Port property 'nifi.rest.port' must be configured and must be a valid number in the config.properties file. ");
        }
        Assert.notNull(host,
                       "The Nifi Host property: 'nifi.rest.host' must be configured in the config.properties file. ");

        NifiRestClientConfig config = new NifiRestClientConfig();
        config.setHost(host);
        config.setPort(port);
        return config;
    }

    @Bean
    public NifiRestClientAroundAspect nifiRestClientAroundAspect() {
        return new NifiRestClientAroundAspect();
    }


}
