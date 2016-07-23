/**
 *
 */
package com.thinkbiganalytics.metadata.sla.config;

import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.api.core.AggregatingAlertProvider;
import com.thinkbiganalytics.alerts.spi.AlertManager;
import com.thinkbiganalytics.alerts.spi.mem.InMemoryAlertManager;
import com.thinkbiganalytics.metadata.sla.alerts.ServiceLevelAgreementActionAlertResponderFactory;

import org.springframework.beans.factory.annotation.Qualifier;
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


    @Bean(name = "slaActionAlertResponder")
    public ServiceLevelAgreementActionAlertResponderFactory slaActionResponder(@Qualifier("alertProvider") AlertProvider alertProvider) {
        ServiceLevelAgreementActionAlertResponderFactory responder = new ServiceLevelAgreementActionAlertResponderFactory();
        alertProvider.addResponder(responder);
        return responder;
    }

    //TODO Replace with JCR backed alerts and move to modeshape
    @Bean(name = "slaAlertManager")
    public AlertManager alertManager() {
        return new InMemoryAlertManager();
    }

}
