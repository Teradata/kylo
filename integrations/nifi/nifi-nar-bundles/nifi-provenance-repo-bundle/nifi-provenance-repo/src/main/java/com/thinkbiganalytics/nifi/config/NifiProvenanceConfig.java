package com.thinkbiganalytics.nifi.config;

import com.thinkbiganalytics.util.SpringApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Created by sr186054 on 3/3/16.
 */
@Configuration
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
@PropertySource("file:/opt/nifi/ext-config/config.properties")
public class NifiProvenanceConfig {

    private static final Logger log = LoggerFactory.getLogger(NifiProvenanceConfig.class);
    @Autowired
    private Environment env;

    @Bean(name = "thinkbigNifiDataSource")
    public DataSource thinkbigNifiDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("thinkbig.provenance.datasource.driverClassName"));
        dataSource.setUrl(env.getProperty("thinkbig.provenance.datasource.url"));
        dataSource.setUsername(env.getProperty("thinkbig.provenance.datasource.username"));
        dataSource.setPassword(env.getProperty("thinkbig.provenance.datasource.password"));
        return dataSource;
    }

    @Bean(name = "jdbcThinkbigNifi")
    public JdbcTemplate jdbcThinkbigNifiTemplate(@Qualifier("thinkbigNifiDataSource") DataSource thinkbigNifiDataSource) {
        return new JdbcTemplate(thinkbigNifiDataSource);
    }

    @Bean
    public SpringApplicationContext springApplicationContext() {
        log.info("CREATE springApplicationContext in Spring ");
        return new SpringApplicationContext();
    }
}
