package com.thinkbiganalytics.metadata.config;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.XStreamExecutionContextStringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.metadata.jpa.jobrepo.ExecutionContextSerializationHelper;
import com.thinkbiganalytics.metadata.jpa.sla.JpaServiceLevelAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;

/**
 * Created by sr186054 on 9/15/16.
 */
@Configuration
//@EnableAutoConfiguration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.thinkbiganalytics.metadata.jpa"}, transactionManagerRef = "operationalMetadataTransactionManager",
                       entityManagerFactoryRef = "operationalMetadataEntityManagerFactory")
public class OperationalMetadataConfig {

    @Bean(name = "operationalMetadataDateTimeFormatter")
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");
    }


    /*
    @Bean(name="metadataSessionFactory")
    @ConfigurationProperties(prefix = "metadata.sessionfactory")
    public FactoryBean<SessionFactory> sessionFactory() {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(metadataDataSource());
        factory.setPackagesToScan("com.thinkbiganalytics.metadata.jpa");
        return factory;
    }
    */
    @Bean(name = "operationalMetadataJpaEntityManager")
    public EntityManager entityManager(@Qualifier("dataSource") DataSource dataSource) {
        return entityManagerFactory(dataSource).createEntityManager();
    }

    @Bean(name = "operationalMetadataTransactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
//        HibernateTransactionManager xtnMgr = new HibernateTransactionManager();
//        xtnMgr.setSessionFactory(sessionFactory());
        JpaTransactionManager xtnMgr = new JpaTransactionManager(entityManagerFactory(dataSource));
        xtnMgr.setDataSource(dataSource);
        xtnMgr.setTransactionSynchronization(AbstractPlatformTransactionManager.SYNCHRONIZATION_ALWAYS);

        return xtnMgr;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }


    @Bean(name = "operationalMetadataEntityManagerFactory")
    public EntityManagerFactory entityManagerFactory(@Qualifier("dataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
        emfBean.setDataSource(dataSource);
        emfBean.setPackagesToScan("com.thinkbiganalytics.jobrepo.jpa", "com.thinkbiganalytics.metadata.jpa");
        emfBean.setJpaVendorAdapter(jpaVendorAdapter());
        emfBean.afterPropertiesSet();
        return emfBean.getObject();
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory(@Qualifier("operationalMetadataEntityManagerFactory") EntityManager em) {
        return new JPAQueryFactory(em);
    }


    @Bean(name = "operationalMetadataAccess")
    public OperationalMetadataTransactionTemplateMetadataAccess metadataAccess() {
        return new OperationalMetadataTransactionTemplateMetadataAccess();
    }


    @Bean(name = "slaAssessor")
    public ServiceLevelAssessor serviceLevelAssessor() {
        return new JpaServiceLevelAssessor();
    }


    @Bean
    public ExecutionContextSerializer executionContextSerializer() throws Exception {
        XStreamExecutionContextStringSerializer defaultSerializer = new XStreamExecutionContextStringSerializer();
        defaultSerializer.afterPropertiesSet();
        return defaultSerializer;
    }

    @Bean
    ExecutionContextSerializationHelper executionContextSerializationHelper() {
        return new ExecutionContextSerializationHelper();
    }


}
