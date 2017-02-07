package com.thinkbiganalytics.metadata.server;

/*-
 * #%L
 * thinkbig-metadata-rest-controller
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

import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 */
public class ServerConfigurationInitialization {

    @Inject
    ServiceLevelAssessor serviceLevelAssessor;

    @Inject
    MetricAssessor<?, ?> datasetUpdatedSinceMetricAssessor;

    @Inject
    MetricAssessor<?, ?> feedExecutedSinceFeedMetricAssessor;

    @Inject
    MetricAssessor<?, ?> feedExecutedSinceScheduleMetricAssessor;

    @Inject
    MetricAssessor<?, ?> datasourceUpdatedSinceFeedExecutedAssessor;

    @Inject
    MetricAssessor<?, ?> withinScheduleAssessor;


    @PostConstruct
    private void init() {
        serviceLevelAssessor.registerMetricAssessor(datasetUpdatedSinceMetricAssessor);
        serviceLevelAssessor.registerMetricAssessor(feedExecutedSinceFeedMetricAssessor);
        serviceLevelAssessor.registerMetricAssessor(feedExecutedSinceScheduleMetricAssessor);
        serviceLevelAssessor.registerMetricAssessor(datasourceUpdatedSinceFeedExecutedAssessor);
        serviceLevelAssessor.registerMetricAssessor(withinScheduleAssessor);
    }

}
