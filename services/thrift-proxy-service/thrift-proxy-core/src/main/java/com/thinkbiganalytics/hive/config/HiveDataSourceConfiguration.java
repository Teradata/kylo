package com.thinkbiganalytics.hive.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Created by sr186054 on 4/1/16.
 */
@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages={"com.thinkbiganalytics"})
public class HiveDataSourceConfiguration {

    @Autowired
    private Environment env;


    @Bean(name="hiveJdbcTemplate")
    public JdbcTemplate hiveJdbcTemplate(@Qualifier("hiveDataSource")   DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


    @Bean(name="hiveMetatoreJdbcTemplate")
    public JdbcTemplate hiveMetatoreJdbcTemplate(@Qualifier("hiveMetastoreDataSource")  DataSource hiveMetastoreDataSource) {
        return new JdbcTemplate(hiveMetastoreDataSource);
    }





    @Bean(name="hiveDataSource")
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("hive.datasource.driverClassName"));
        dataSource.setUrl(env.getProperty("hive.datasource.url"));
        dataSource.setUsername(env.getProperty("hive.datasource.username"));
        dataSource.setPassword(env.getProperty("hive.datasource.password"));
        return dataSource;
    }

    @Bean(name="hiveMetastoreDataSource")
    public DataSource hiveMetastoreDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("hive.metastore.datasource.driverClassName"));
        dataSource.setUrl(env.getProperty("hive.metastore.datasource.url"));
        dataSource.setUsername(env.getProperty("hive.metastore.datasource.username"));
        dataSource.setPassword(env.getProperty("hive.metastore.datasource.password"));
        return dataSource;
    }

}
