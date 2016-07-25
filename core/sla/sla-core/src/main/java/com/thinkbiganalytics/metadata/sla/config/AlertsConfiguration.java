/**
 *
 */
package com.thinkbiganalytics.metadata.sla.config;

import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.api.core.AggregatingAlertProvider;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.mem.InMemoryAlertManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AlertsConfiguration {

    //TODO Replace with JCR backed alerts and move to modeshape
    @Bean(name = "alertProvider")
    public AlertProvider alertProvider() {
        AggregatingAlertProvider provider = new AggregatingAlertProvider();
        provider.addAlertManager(alertManager());
        return provider;
    }




    //TODO Replace with JCR backed alerts and move to modeshape
    @Bean(name = "slaAlertManager")
    public AlertManager alertManager() {
        return new InMemoryAlertManager();
    }

}
