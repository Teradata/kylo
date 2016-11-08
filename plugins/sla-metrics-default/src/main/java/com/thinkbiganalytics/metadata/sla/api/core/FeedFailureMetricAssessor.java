package com.thinkbiganalytics.metadata.sla.api.core;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import javax.inject.Inject;

/**
 * Created by sr186054 on 11/8/16.
 */
public class FeedFailureMetricAssessor implements MetricAssessor<FeedFailedMetric, Serializable> {

    private static final Logger log = LoggerFactory.getLogger(FeedFailureMetricAssessor.class);


    @Inject
    private FeedFailureService feedFailureService;


    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof FeedFailedMetric;
    }

    @Override
    public void assess(FeedFailedMetric metric, MetricAssessmentBuilder<Serializable> builder) {
        builder.metric(metric);

        String feedName = metric.getFeedName();
        if (feedFailureService.hasFailure(feedName)) {
            builder.message("Feed " + feedName + " has failed ")
                .result(AssessmentResult.FAILURE);
        } else {
            builder.message("Feed " + feedName + " has succeeded ")
                .result(AssessmentResult.SUCCESS);
        }

    }
}
