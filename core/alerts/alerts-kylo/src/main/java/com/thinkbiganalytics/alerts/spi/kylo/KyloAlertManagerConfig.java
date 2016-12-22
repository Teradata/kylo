/**
 * 
 */
package com.thinkbiganalytics.alerts.spi.kylo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.alerts.spi.AlertSourceAggregator;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlertRepository;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class KyloAlertManagerConfig {
    
    @Bean(name="kyloAlertManager")
    public KyloAlertManager kyloAlertManager(JpaAlertRepository repo, AlertSourceAggregator aggregator)  {
        KyloAlertManager mgr = new KyloAlertManager(repo);
        aggregator.addAlertManager(mgr);
        return mgr;
    }

}
