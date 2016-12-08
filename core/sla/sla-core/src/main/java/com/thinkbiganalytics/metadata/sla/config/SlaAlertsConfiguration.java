/**
 *
 */
package com.thinkbiganalytics.metadata.sla.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.alerts.api.core.AggregatingAlertProvider;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.mem.InMemoryAlertManager;


@Configuration
public class SlaAlertsConfiguration {

    //TODO Replace with JCR backed alerts and move to modeshape
    @Bean(name = "slaAlertManager")
    public AlertManager alertManager(AggregatingAlertProvider provider) {
        AlertManager mgr = new InMemoryAlertManager();
        provider.addAlertManager(mgr);
        return mgr;
    }

}
