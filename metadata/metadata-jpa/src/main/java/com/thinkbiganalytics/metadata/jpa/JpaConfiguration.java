/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa;

import javax.inject.Named;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.jpa.datasource.JpaDatasourceProvider;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeedProvider;
import com.thinkbiganalytics.metadata.jpa.op.JpaDataOperationsProvider;

/**
 *
 * @author Sean Felten
 */
@Configuration
@EnableAutoConfiguration
public class JpaConfiguration {
    
    @Bean(name="metadataDatasource")
    @ConfigurationProperties(prefix = "metadata.datasource")
    public DataSource metadataDataSource() {
        DataSource ds = DataSourceBuilder.create().build();
        
        return ds;
    }
    
    @Bean(name="metadataSessionFactory")
    @ConfigurationProperties(prefix = "metadata.sessionfactory")
    public FactoryBean<SessionFactory> sessionFactory() {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(metadataDataSource());
        factory.setPackagesToScan("com.thinkbiganalytics.metadata.jpa");
        return factory;
    }
    
    @Bean(name="metadataTransactionManager")
    public PlatformTransactionManager transactionManager(@Named("metadataSessionFactory") SessionFactory sessFactory) {
        HibernateTransactionManager xtnMgr = new HibernateTransactionManager();
        xtnMgr.setSessionFactory(sessFactory);
        return xtnMgr;
    }
    
    @Bean
    @Primary
    public FeedProvider feedProvider() {
        return new JpaFeedProvider();
    }

    @Bean
    @Primary
    public DatasourceProvider datasetProvider() {
        return new JpaDatasourceProvider();
    }
    
    @Bean
    @Primary
    public DataOperationsProvider dataOperationsProvider() {
        return new JpaDataOperationsProvider();
    }

}
