package com.thinkbiganalytics.metadata.migration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

/**
 * Created by sr186054 on 6/15/16.
 */
@EnableAutoConfiguration
@Configuration
@ComponentScan("com.thinkbiganalytics.metadata.migration")
@ContextConfiguration(classes = {DatabaseConfiguration.class, MigrationConfiguration.class})
public class TestConfig {

}
