package com.thinkbiganalytics.jobrepo.nifi.config;


import com.thinkbiganalytics.jobrepo.repository.dao.NifJobRepositoryFactoryBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import javax.sql.DataSource;

/**
 * Created by sr186054 on 2/26/16.
 */
@Configuration
public class DatabaseConfig {
    @Autowired
    private Environment env;

    @Bean(name="thinkbigNifiDataSource")
    public DataSource thinkbigNifiDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("thinkbig.nifi.datasource.driverClassName"));
        dataSource.setUrl(env.getProperty("thinkbig.nifi.datasource.url"));
        dataSource.setUsername(env.getProperty("thinkbig.nifi.datasource.username"));
        dataSource.setPassword(env.getProperty("thinkbig.nifi.datasource.password"));
        return dataSource;
    }

    @Bean(name = "jdbcThinkbigNifi")
    public JdbcTemplate jdbcThinkbigNifiTemplate(@Qualifier("thinkbigNifiDataSource") DataSource thinkbigNifiDataSource) {
        return new JdbcTemplate(thinkbigNifiDataSource);
    }


    @Bean(name="nifiJobRepository")
    public NifJobRepositoryFactoryBean nifiJobRepository(@Qualifier("dataSource") DataSource dataSource, @Qualifier("thinkbigNifiDataSource") DataSource nifiDataSource){
        NifJobRepositoryFactoryBean repository = new NifJobRepositoryFactoryBean();
        repository.setDataSource(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        repository.setTransactionManager(transactionManager);
        repository.setNifiDataSource(nifiDataSource);
        return repository;
    }



}