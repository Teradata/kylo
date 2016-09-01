/**
 *
 */
package com.thinkbiganalytics.jobrepo.config;


import com.querydsl.jpa.impl.JPAQueryFactory;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 *
 *
 */
@Configuration
//@EnableAutoConfiguration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.thinkbiganalytics.jobrepo",entityManagerFactoryRef = "jobRepositoryEntityManagerFactory")
public class JobRepositoryJpaConfiguration {

    @Bean(name = "jobRepositoryDateTimeFormatter")
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
    @Bean(name = "jobRepositoryEntityManager")
    public EntityManager entityManager(@Qualifier("dataSource") DataSource dataSource) {
        return entityManagerFactory(dataSource).createEntityManager();
    }

    @Bean(name = "jobRepositoryTransactionManager")
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


    @Bean(name = "jobRepositoryEntityManagerFactory")
    public EntityManagerFactory entityManagerFactory(@Qualifier("dataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
        emfBean.setDataSource(dataSource);
        emfBean.setPackagesToScan("com.thinkbiganalytics.jobrepo.jpa");
        emfBean.setJpaVendorAdapter(jpaVendorAdapter());
        emfBean.afterPropertiesSet();
        return emfBean.getObject();
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory(@Qualifier("jobRepositoryEntityManagerFactory") EntityManager em) {
        return new JPAQueryFactory(em);
    }


    @Bean(name = "operationalMetadataAccess")
    public OperationalMetadataAccess metadataAccess() {
        return new JobRepositoryTransactionTemplateMetadataAccess();
    }

}
