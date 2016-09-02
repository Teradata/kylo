package com.thinkbiganalytics.jobrepo.nifi.config;


import com.thinkbiganalytics.jobrepo.jpa.ExecutionContextSerializationHelper;
import com.thinkbiganalytics.jobrepo.nifi.provenance.FlowFileEventProvider;
import com.thinkbiganalytics.jobrepo.nifi.provenance.InMemoryFlowFileEventProvider;
import com.thinkbiganalytics.jobrepo.nifi.provenance.NifiFailureEventJmsReceiver;
import com.thinkbiganalytics.jobrepo.nifi.provenance.NifiStatsJmsReceiver;
import com.thinkbiganalytics.jobrepo.nifi.provenance.ProvenanceEventApplicationStartupListener;
import com.thinkbiganalytics.jobrepo.repository.dao.NifJobRepositoryFactoryBean;

import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.XStreamExecutionContextStringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Created by sr186054 on 2/26/16.
 */
@Configuration
public class DatabaseConfig {

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("transactionManager")
    private PlatformTransactionManager transactionManager;


    @Bean(name = "nifiJobRepository")
    public NifJobRepositoryFactoryBean nifiJobRepository(@Qualifier("dataSource") DataSource dataSource) {
        NifJobRepositoryFactoryBean repository = new NifJobRepositoryFactoryBean();
        repository.setDataSource(dataSource);
        // PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        repository.setTransactionManager(transactionManager);
        return repository;
    }

    @Bean(name = "FlowFileEventProvider")
    public FlowFileEventProvider flowFileEventProvider() {
        return new InMemoryFlowFileEventProvider();
    }

    @Bean
    public ProvenanceEventApplicationStartupListener provenanceEventStartupListener() {
        return new ProvenanceEventApplicationStartupListener();
    }



    @Bean
    public NifiStatsJmsReceiver nifiStatsJmsReceiver() {
        return new NifiStatsJmsReceiver();
    }

    @Bean
    public NifiFailureEventJmsReceiver nifiFailureEventJmsReceiver() {
        return new NifiFailureEventJmsReceiver();
    }

    @Bean
    public ExecutionContextSerializer executionContextSerializer() throws Exception{
        XStreamExecutionContextStringSerializer defaultSerializer = new XStreamExecutionContextStringSerializer();
        defaultSerializer.afterPropertiesSet();
        return defaultSerializer;
    }

    @Bean
    ExecutionContextSerializationHelper executionContextSerializationHelper() {
        return new ExecutionContextSerializationHelper();
    }

}