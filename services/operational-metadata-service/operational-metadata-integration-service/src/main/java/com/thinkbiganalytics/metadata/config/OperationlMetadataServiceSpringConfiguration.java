package com.thinkbiganalytics.metadata.config;

import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.metadata.jobrepo.nifi.provenance.NifiStatsJmsReceiver;
import com.thinkbiganalytics.metadata.sla.DefaultServiceLevelAgreementScheduler;
import com.thinkbiganalytics.metadata.sla.JpaJcrServiceLevelAgreementChecker;
import com.thinkbiganalytics.metadata.sla.ServiceLevelAgreementActionAlertResponderFactory;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementChecker;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementScheduler;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sr186054 on 9/18/16.
 */
@Configuration
public class OperationlMetadataServiceSpringConfiguration {

    @Bean
    public NifiStatsJmsReceiver nifiStatsJmsReceiver() {
        return new NifiStatsJmsReceiver();
    }


    @Bean
    public ServiceLevelAgreementScheduler serviceLevelAgreementScheduler() {
        return new DefaultServiceLevelAgreementScheduler();
    }

    @Bean
    public ServiceLevelAgreementChecker serviceLevelAgreementChecker() {
        return new JpaJcrServiceLevelAgreementChecker();
    }

    @Bean(name = "slaActionAlertResponder")
    public ServiceLevelAgreementActionAlertResponderFactory slaActionResponder(@Qualifier("alertProvider") AlertProvider alertProvider) {
        ServiceLevelAgreementActionAlertResponderFactory responder = new ServiceLevelAgreementActionAlertResponderFactory();
        alertProvider.addResponder(responder);
        return responder;
    }


}
