package com.thinkbiganalytics.nifi.v1.security;

import com.thinkbiganalytics.nifi.security.KerberosProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringSecurityConfigurationV1 {

    @Bean
    public KerberosProperties kerberosProperties() {
        return new KerberosPropertiesV1();
    }
}
