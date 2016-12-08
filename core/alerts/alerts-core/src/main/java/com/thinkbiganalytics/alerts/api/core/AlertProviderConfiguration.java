/**
 *
 */
package com.thinkbiganalytics.alerts.api.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AlertProviderConfiguration {

    @Bean(name = "alertProvider")
    public AggregatingAlertProvider alertProvider() {
        return new AggregatingAlertProvider();
    }
}
