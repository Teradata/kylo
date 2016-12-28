package com.thinkbiganalytics.hive.config;

//import com.thinkbiganalytics.hive.service.RefreshableDataSource;

import com.thinkbiganalytics.hive.service.HiveService;
import com.thinkbiganalytics.hive.service.RefreshableDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Created by sr186054 on 4/1/16.
 */
@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {"com.thinkbiganalytics.hive.service"})
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


    @Bean(name = "hiveService")
    public HiveService hiveService() {
        return new HiveService();
    }


    @Bean(name="hiveDataSource")
    public DataSource dataSource() {

        //  DataSource ds = DataSourceBuilder.create().build();
        // return ds;

        RefreshableDataSource ds = new RefreshableDataSource("hive.datasource");
        return ds;
    }


    @Bean(name="hiveMetastoreDataSource")
    @ConfigurationProperties(prefix = "hive.metastore.datasource")
    public DataSource metadataDataSource() {
        DataSource ds = DataSourceBuilder.create().build();
        return ds;


    }

}
