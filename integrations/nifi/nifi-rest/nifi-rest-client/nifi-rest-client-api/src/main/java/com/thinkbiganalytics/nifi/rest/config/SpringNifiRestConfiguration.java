package com.thinkbiganalytics.nifi.rest.config;

import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClientConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;

import java.net.MalformedURLException;
import java.net.URL;

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

    /**
     * Gets the configuration for the NiFi REST client.
     *
     * <p>Looks for {@code thinkbig.nifi.rest} properties first then for {@code nifi.rest} properties.</p>
     *
     * @return the NiFi REST client configuration
     */
    @Bean(name = "nifiRestClientConfig")
    @ConfigurationProperties(prefix = "nifi.rest")
    public NifiRestClientConfig nifiRestClientConfig() {
        final NifiRestClientConfig config = new NifiRestClientConfig();
        config.setUsername(env.getProperty("thinkbig.nifi.rest.username"));
        config.setPassword(env.getProperty("thinkbig.nifi.rest.password"));

        final String host = env.getProperty("thinkbig.nifi.rest.host");
        if (host != null) {
            try {
                final URL url = new URL(host);
                config.setHost(url.getHost());
                config.setPort((url.getPort() > -1) ? url.getPort() : 8079);
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException("Invalid thinkbig.nifi.rest.host: " + host, e);
            }
        }

        return config;
    }

    @Bean
    public NifiRestClientAroundAspect nifiRestClientAroundAspect() {
        return new NifiRestClientAroundAspect();
    }


}
