/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.jpa.category.JpaCategoryProvider;
import com.thinkbiganalytics.metadata.jpa.feedmgr.category.JpaFeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.jpa.feedmgr.feed.JpaFeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.jpa.feedmgr.template.JpaFeedManagerTemplateProvider;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.SessionFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasourceProvider;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeedProvider;
import com.thinkbiganalytics.metadata.jpa.op.JpaDataOperationsProvider;
import com.thinkbiganalytics.metadata.jpa.sla.JpaServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;

/**
 *
 * @author Sean Felten
 */
@Configuration
@EnableAutoConfiguration
public class JpaConfiguration {
    
    @Bean(name="metadataDateTimeFormatter")
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss");
    }
    
    @Bean(name="metadataDatasource")
    @ConfigurationProperties(prefix = "metadata.datasource")
    public DataSource metadataDataSource() {
        DataSource ds = DataSourceBuilder.create().build();
        
        return ds;
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
@Bean(name="metadataEntityManager")
    public EntityManager entityManager() {
    return entityManagerFactory().createEntityManager();
    }
    
    @Bean(name="metadataTransactionManager")
    public PlatformTransactionManager transactionManager() {
//        HibernateTransactionManager xtnMgr = new HibernateTransactionManager();
//        xtnMgr.setSessionFactory(sessionFactory());
        JpaTransactionManager xtnMgr = new JpaTransactionManager(entityManagerFactory());
        xtnMgr.setDataSource(metadataDataSource());
        xtnMgr.setTransactionSynchronization(AbstractPlatformTransactionManager.SYNCHRONIZATION_ALWAYS);
        
        return xtnMgr;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter(){
        return new HibernateJpaVendorAdapter();
    }


        @Bean(name="metadataEntityManagerFactory")
    public EntityManagerFactory entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
        emfBean.setDataSource(metadataDataSource());
        emfBean.setDataSource(metadataDataSource());
        emfBean.setPackagesToScan("com.thinkbiganalytics.metadata.jpa");
        emfBean.setJpaVendorAdapter(jpaVendorAdapter());
            emfBean.afterPropertiesSet();
        return emfBean.getObject();
    }

    
    @Bean
    public MetadataAccess metadataAccess() {
        return new TransactionTemplateMetadataAccess();
    }
    
    @Bean
    public FeedProvider feedProvider() {
        return new JpaFeedProvider();
    }

    @Bean
    public DatasourceProvider datasetProvider() {
        return new JpaDatasourceProvider();
    }
    
    @Bean
    public DataOperationsProvider dataOperationsProvider() {
        return new JpaDataOperationsProvider();
    }

    @Bean
    public ServiceLevelAgreementProvider slaProvider() {
        return new JpaServiceLevelAgreementProvider();
    }


    @Bean
    public FeedManagerFeedProvider jpaFeedManagerFeedProvider() {
        return new JpaFeedManagerFeedProvider();
    }

    @Bean
    public FeedManagerCategoryProvider jpaFeedManagerCategoryProvider() {
        return new JpaFeedManagerCategoryProvider();
    }

    @Bean
    public JpaCategoryProvider jpaCategoryProvider() {
        return new JpaCategoryProvider();
    }


    @Bean
    public FeedManagerTemplateProvider jpaFeedManagerTemplateProvider() {
        return new JpaFeedManagerTemplateProvider();
    }

}
