package com.thinkbiganalytics.metadata.server;

import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by sr186054 on 7/25/16.
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
