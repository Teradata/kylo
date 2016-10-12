package com.thinkbiganalytics.nifi.rest.config;

import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;

/**
 * Created by sr186054 on 4/19/16.
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class SpringNifiRestConfiguration {

    @Autowired
    private Environment env;

    @Bean(name = "nifiRestClient")
    public LegacyNifiRestClient nifiRestClient() {

        LegacyNifiRestClient restClient = new LegacyNifiRestClient();
        return restClient;

    }


    @Bean(name = "nifiRestClientConfig")
    @ConfigurationProperties(prefix = "nifi.rest")
    public NifiRestClientConfig nifiRestClientConfig() {
        return new NifiRestClientConfig();
    }

    @Bean
    public NifiRestClientAroundAspect nifiRestClientAroundAspect() {
        return new NifiRestClientAroundAspect();
    }


}
