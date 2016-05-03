package com.thinkbiganalytics.nifi.config;

import com.thinkbiganalytics.util.SpringApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

/**
 * Created by sr186054 on 3/3/16.
 */
@Configuration
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
@PropertySource("classpath:config.properties")
public class NifiProvenanceConfig {


    @Bean
    public SpringApplicationContext springApplicationContext() {
        System.out.println("CREATE springApplicationContext in Spring ");
        return new SpringApplicationContext();
    }
}
