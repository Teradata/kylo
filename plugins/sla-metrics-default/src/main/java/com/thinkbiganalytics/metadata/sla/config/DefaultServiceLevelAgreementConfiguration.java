package com.thinkbiganalytics.metadata.sla.config;

/*-
 * #%L
 * thinkbig-sla-metrics-default
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

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.core.FeedFailureMetricAssessor;
import com.thinkbiganalytics.metadata.sla.api.core.FeedOnTimeArrivalMetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

/**
 * Spring bean configuration for the sla configurations
 */
@Configuration
public class DefaultServiceLevelAgreementConfiguration {

    @Bean(name = "onTimeAssessor")
    public MetricAssessor<? extends Metric, Serializable> onTimeMetricAssessor(@Qualifier("slaAssessor") ServiceLevelAssessor slaAssessor) {
        FeedOnTimeArrivalMetricAssessor metricAssr = new FeedOnTimeArrivalMetricAssessor();
        slaAssessor.registerMetricAssessor(metricAssr);
        return metricAssr;
    }

    @Bean(name = "feedFailureAssessor")
    public MetricAssessor<? extends Metric, Serializable> feedFailureAssessor(@Qualifier("slaAssessor") ServiceLevelAssessor slaAssessor) {
        FeedFailureMetricAssessor metricAssr = new FeedFailureMetricAssessor();
        slaAssessor.registerMetricAssessor(metricAssr);
        return metricAssr;
    }


}
