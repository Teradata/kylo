/**
 * 
 */
package com.thinkbiganalytics.alerts.spi.kylo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlertRepository;

/**
 *
 * @author Sean Felten
 */
@Configuration
@EnableJpaRepositories(basePackages = {"com.thinkbiganalytics.alerts.spi.kylo"}, 
                       transactionManagerRef = "operationalMetadataTransactionManager",
                       entityManagerFactoryRef = "operationalMetadataEntityManagerFactory")
public class KyloAlertManagerConfig {
    
    @Bean(name="kyloAlertManager")
    public KyloAlertManager kyloAlertManager(JpaAlertRepository repo)  {
        return new KyloAlertManager(repo);
    }

}
