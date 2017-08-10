package com.thinkbiganalytics.metadata.jpa.audit;

import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.spi.AlertManager;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TestSpringConfiguration {

    @Bean
    AlertProvider alertProvider(){
        return Mockito.mock(AlertProvider.class);
    }

    @Bean(name = "kyloAlertManager")
    AlertManager alertManager(){
        return Mockito.mock(AlertManager.class);
    }
}
