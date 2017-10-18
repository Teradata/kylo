package com.thinkbiganalytics.metadata.config;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.metadata.jpa.feed.AugmentableQueryRepositoryFactoryBean;
import com.thinkbiganalytics.metadata.jpa.feed.OpsManagerFeedCacheById;
import com.thinkbiganalytics.metadata.jpa.feed.OpsManagerFeedCacheByName;
import com.thinkbiganalytics.metadata.jpa.feed.security.FeedAclCache;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.JobExecutionChangedNotifier;
import com.thinkbiganalytics.metadata.jpa.sla.JpaServiceLevelAssessor;
import com.thinkbiganalytics.metadata.jpa.sla.ServiceLevelAgreementDescriptionCache;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.spi.EvaluationContextExtension;
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
 * Spring configuration for JPA beans
 * Spring Data JPA Repositories are enabled here
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = {"com.thinkbiganalytics.metadata.jpa", "com.thinkbiganalytics.metadata.jpa.feed.security"},
    transactionManagerRef = "operationalMetadataTransactionManager",
    entityManagerFactoryRef = "operationalMetadataEntityManagerFactory",
    repositoryFactoryBeanClass = AugmentableQueryRepositoryFactoryBean.class)
public class OperationalMetadataConfig {

    @Bean(name = "operationalMetadataDateTimeFormatter")
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");
    }


    /**
     * Return the entity manager
     * the datasource bean is configured in the base services application
     *
     * @param dataSource the datasource to use as the connection for the entity manager
     * @return the entity manager
     */
    @Bean(name = "operationalMetadataJpaEntityManager")
    public EntityManager entityManager(@Qualifier("dataSource") DataSource dataSource) {
        return entityManagerFactory(dataSource).createEntityManager();
    }

    /**
     * Return the JPA transaction manager
     *
     * @return the JPA transaction manager
     */
    @Bean(name = "operationalMetadataTransactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        JpaTransactionManager xtnMgr = new JpaTransactionManager(entityManagerFactory(dataSource));
        xtnMgr.setDataSource(dataSource);
        xtnMgr.setTransactionSynchronization(AbstractPlatformTransactionManager.SYNCHRONIZATION_ALWAYS);

        return xtnMgr;
    }

    /**
     * Return the HibernateJpaVender
     *
     * @return the Hibernate JPA Vendor
     */
    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }


    /**
     * Return the entity manager factory for Hibernate
     *
     * @return the Hibernate entity manager factory
     */
    @Bean(name = "operationalMetadataEntityManagerFactory")
    public EntityManagerFactory entityManagerFactory(@Qualifier("dataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
        emfBean.setDataSource(dataSource);
        emfBean.setPackagesToScan("com.thinkbiganalytics.jobrepo.jpa", "com.thinkbiganalytics.metadata.jpa");
        emfBean.setJpaVendorAdapter(jpaVendorAdapter());
        emfBean.afterPropertiesSet();
        return emfBean.getObject();
    }

    /**
     * Return the QueryDSL JPA factory
     *
     * @return the Query DSL JPA factory
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory(@Qualifier("operationalMetadataEntityManagerFactory") EntityManager em) {
        return new JPAQueryFactory(em);
    }


    /**
     * Return the Transaction Manager wrapper
     *
     * @return the access manager
     */
    @Bean(name = "operationalMetadataAccess")
    public OperationalMetadataTransactionTemplateMetadataAccess metadataAccess() {
        return new OperationalMetadataTransactionTemplateMetadataAccess();
    }


    /**
     * Return the JPA Service Level Assessor uses to assess ServiceLevelAgreements
     *
     * @return the service level agreement assessor
     */
    @Bean(name = "slaAssessor")
    public ServiceLevelAssessor serviceLevelAssessor() {
        return new JpaServiceLevelAssessor();
    }


    @Bean
    public EvaluationContextExtension securityExtension() {
        return new RoleSetExposingSecurityEvaluationContextExtension();
    }

    @Bean
    public JobExecutionChangedNotifier jobExecutionChangedNotifier(){
        return new JobExecutionChangedNotifier();
    }

    @Bean
    public OpsManagerFeedCacheByName opsManagerFeedCacheByName() {
        return new OpsManagerFeedCacheByName();
    }

    @Bean
    public OpsManagerFeedCacheById opsManagerFeedCacheById() {
        return new OpsManagerFeedCacheById();
    }

    @Bean
    public FeedAclCache feedAclCache(){
        return new FeedAclCache();
    }

    @Bean
    public ServiceLevelAgreementDescriptionCache serviceLevelAgreementDescriptionCache(){
        return new ServiceLevelAgreementDescriptionCache();
    }

}
