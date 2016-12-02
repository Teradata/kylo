/**
 *
 */
package com.thinkbiganalytics.testing.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

import com.querydsl.jpa.impl.JPAQueryFactory;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.thinkbiganalytics"})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"com.thinkbiganalytics.metadata.jpa"}, transactionManagerRef = "operationalMetadataTransactionManager",
                       entityManagerFactoryRef = "operationalMetadataEntityManagerFactory")
public class TestJpaConfiguration {


    /**
     * This is the datasource used by JPA
     */
    @Bean(name = "dataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource", locations = "classpath:test-jpa-application.properties")
    public DataSource dataSource() {
        DataSource newDataSource = DataSourceBuilder.create().build();

        return newDataSource;
    }
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


//    @Bean(name = "operationalMetadataAccess")
//    public MetadataAccess metadataAccess() {
//        return new OperationalMetadataTransactionTemplateMetadataAccess();
//    }
//
//    @Bean
//    public ExecutionContextSerializer executionContextSerializer() throws Exception {
//        XStreamExecutionContextStringSerializer defaultSerializer = new XStreamExecutionContextStringSerializer();
//        defaultSerializer.afterPropertiesSet();
//        return defaultSerializer;
//    }
//
//    @Bean
//    ExecutionContextSerializationHelper executionContextSerializationHelper() {
//        return new ExecutionContextSerializationHelper();
//    }

}
