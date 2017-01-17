package com.thinkbiganalytics.nifi.provenance.config;

import com.thinkbiganalytics.nifi.provenance.util.SpringApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 3/3/16.
 */
@Configuration
public class NifiProvenanceConfig {

    private static final Logger log = LoggerFactory.getLogger(NifiProvenanceConfig.class);

    @Bean
    public SpringApplicationContext springApplicationContext() {
        return new SpringApplicationContext();
    }

}
