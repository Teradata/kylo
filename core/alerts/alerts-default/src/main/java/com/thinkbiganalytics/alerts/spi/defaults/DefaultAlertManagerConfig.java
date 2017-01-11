/**
 * 
 */
package com.thinkbiganalytics.alerts.spi.defaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.alerts.spi.AlertSourceAggregator;
import com.thinkbiganalytics.metadata.jpa.alerts.JpaAlertRepository;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class DefaultAlertManagerConfig {
    
    @Bean(name="kyloAlertManager")
    public DefaultAlertManager kyloAlertManager(JpaAlertRepository repo, AlertSourceAggregator aggregator)  {
        DefaultAlertManager mgr = new DefaultAlertManager(repo);
        aggregator.addAlertManager(mgr);
        return mgr;
    }

}
