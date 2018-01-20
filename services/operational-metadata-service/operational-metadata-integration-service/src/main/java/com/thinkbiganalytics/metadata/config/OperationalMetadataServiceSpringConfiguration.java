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
import com.thinkbiganalytics.metadata.cache.AlertsCache;
import com.thinkbiganalytics.metadata.cache.CacheService;
import com.thinkbiganalytics.metadata.cache.CategoryFeedService;
import com.thinkbiganalytics.metadata.cache.DataConfidenceJobsCache;
import com.thinkbiganalytics.metadata.cache.FeedHealthSummaryCache;
import com.thinkbiganalytics.metadata.cache.RunningJobsCache;
import com.thinkbiganalytics.metadata.cache.ServiceStatusCache;
import com.thinkbiganalytics.metadata.jobrepo.StreamingFeedService;
import com.thinkbiganalytics.metadata.jobrepo.nifi.provenance.NifiBulletinExceptionExtractor;
import com.thinkbiganalytics.metadata.jobrepo.nifi.provenance.NifiStatsJmsReceiver;
import com.thinkbiganalytics.metadata.jobrepo.nifi.provenance.ProvenanceEventFeedUtil;
import com.thinkbiganalytics.metadata.jobrepo.nifi.provenance.ProvenanceEventReceiver;
import com.thinkbiganalytics.metadata.jobrepo.nifi.provenance.RetryProvenanceEventWithDelay;
import com.thinkbiganalytics.metadata.sla.DefaultServiceLevelAgreementScheduler;
import com.thinkbiganalytics.metadata.sla.JpaJcrServiceLevelAgreementChecker;
import com.thinkbiganalytics.metadata.sla.ServiceLevelAgreementActionAlertResponderFactory;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementChecker;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementScheduler;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Spring configuration class for the beans in the current module
 */
@Configuration
public class OperationalMetadataServiceSpringConfiguration {



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
    @Profile("!kyloUpgrade")
    public RetryProvenanceEventWithDelay retryProvenanceEventWithDelay() {
            return new RetryProvenanceEventWithDelay();
    }

    @Bean
    @Profile("!kyloUpgrade")
    public ProvenanceEventReceiver provenanceEventReceiver(){
        return new ProvenanceEventReceiver();
    }

    @Bean
    @Profile("!kyloUpgrade")
    public NifiStatsJmsReceiver nifiStatsJmsReceiver() {
        return new NifiStatsJmsReceiver();
    }

    @Bean
    public NifiBulletinExceptionExtractor nifiBulletinExceptionExtractor(){
        return new NifiBulletinExceptionExtractor();
    }



    @Bean
    public CacheService cacheService(){
        return new CacheService();
    }
    @Bean
    public AlertsCache alertsCache(){
        return new AlertsCache();
    }
    @Bean
    public DataConfidenceJobsCache dataConfidenceJobsCache(){
        return new DataConfidenceJobsCache();
    }
    @Bean
    public FeedHealthSummaryCache feedHealthSummaryCache(){
        return new FeedHealthSummaryCache();
    }
    @Bean
    public RunningJobsCache runningJobsCache(){
        return new RunningJobsCache();
    }
    @Bean
    public ServiceStatusCache serviceStatusCache(){
        return new ServiceStatusCache();
    }

    @Bean
    public CategoryFeedService categoryFeedService() {
        return new CategoryFeedService();
    }

}
