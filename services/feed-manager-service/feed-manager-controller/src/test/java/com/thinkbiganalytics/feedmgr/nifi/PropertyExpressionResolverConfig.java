package com.thinkbiganalytics.feedmgr.nifi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/** Configuration for {@link PropertyExpressionResolverTest} */
@Configuration
@PropertySource("classpath:/application.properties")
public class PropertyExpressionResolverConfig {

    @Bean
    public SpringEnvironmentProperties getEnvironmentProperties() {
        return new SpringEnvironmentProperties();
    }

    @Bean
    public PropertyExpressionResolver getResolver() {
        return new PropertyExpressionResolver();
    }
}
