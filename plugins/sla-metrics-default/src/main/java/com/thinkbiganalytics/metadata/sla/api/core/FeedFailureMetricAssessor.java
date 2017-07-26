package com.thinkbiganalytics.metadata.sla.api.core;

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

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import javax.inject.Inject;

/**
 * SLA assessor used to asses the {@link FeedFailedMetric} and violate the SLA if the feed fails
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
        FeedFailureService.LastFeedFailure lastFeedFailure = feedFailureService.getLastFeedFailure(feedName);
        DateTime lastTime =feedFailureService.initializeTime;
        if(lastFeedFailure != null){
            lastTime = lastFeedFailure.getDateTime();
        }
        //compare with the latest feed time
        builder.compareWith(feedName, lastTime.getMillis());
        if (feedFailureService.hasFailure(feedName)) {
            builder.message("Feed " + feedName + " has failed ")
                .result(AssessmentResult.FAILURE);
        } else {
            builder.message("Feed " + feedName + " has succeeded ")
                .result(AssessmentResult.SUCCESS);
        }

    }
}
