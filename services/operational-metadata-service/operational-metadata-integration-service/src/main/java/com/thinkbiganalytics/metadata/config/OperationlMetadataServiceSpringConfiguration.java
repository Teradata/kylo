package com.thinkbiganalytics.metadata.config;

/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.metadata.jobrepo.StreamingFeedService;
import com.thinkbiganalytics.metadata.jobrepo.nifi.provenance.NifiStatsJmsReceiver;
import com.thinkbiganalytics.metadata.jobrepo.nifi.provenance.ProvenanceEventFeedUtil;
import com.thinkbiganalytics.metadata.sla.DefaultServiceLevelAgreementScheduler;
import com.thinkbiganalytics.metadata.sla.JpaJcrServiceLevelAgreementChecker;
import com.thinkbiganalytics.metadata.sla.ServiceLevelAgreementActionAlertResponderFactory;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementChecker;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementScheduler;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class for the beans in the current module
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

    @Bean
    public ProvenanceEventFeedUtil provenanceEventFeedUtil(){
        return new ProvenanceEventFeedUtil();
    }


    @Bean
    public StreamingFeedService streamingFeedService() {
        return new StreamingFeedService();
    }

}
