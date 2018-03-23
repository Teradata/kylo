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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

import javax.inject.Inject;

/**
 * SLA assessor used to asses the {@link FeedFailedMetric} and violate the SLA if the feed fails
 */
public class FeedFailureMetricAssessor implements MetricAssessor<FeedFailedMetric, Serializable> {


    private static final Logger LOG = LoggerFactory.getLogger(FeedFailureMetricAssessor.class);


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

        FeedFailureService.LastFeedJob lastFeedJob = feedFailureService.findLatestJob(feedName);
        LOG.debug("Assessing FeedFailureMetric for '{}'.  The Last Feed Job was: {} ",feedName,lastFeedJob);
        if (!feedFailureService.isEmptyJob(lastFeedJob)) {
            DateTime lastTime = lastFeedJob.getDateTime();

            //compare with the latest feed time, alerts with same timestamps will not be raised
            builder.compareWith(feedName, lastTime.getMillis());

            if (lastFeedJob.isFailure()) {
                String message = "Feed " + feedName + " has failed on " + lastFeedJob.getDateTime();
                if(lastFeedJob.getBatchJobExecutionId() != null){
                    message +=". Batch Job ExecutionId: "+lastFeedJob.getBatchJobExecutionId();
                }
                LOG.debug(message);

                builder.message(message).result(AssessmentResult.FAILURE);
            } else {
                String message ="Feed " + feedName + " has succeeded on " + lastFeedJob.getDateTime();
                if(lastFeedJob.getBatchJobExecutionId() != null){
                    message +=". Batch Job ExecutionId: "+lastFeedJob.getBatchJobExecutionId();
                }
                LOG.debug(message);

                builder.message(message).result(AssessmentResult.SUCCESS);
            }
        } else {
            LOG.debug("FeedFailureMetric found an no recent jobs for '{}'. Returning SUCCESS ",feedName);
            builder.message("No Jobs found for feed " + feedName + " since " + lastFeedJob.getDateTime()).result(AssessmentResult.SUCCESS);
        }
    }

    public FeedFailureService getFeedFailureService() {
        return feedFailureService;
    }

    public void setFeedFailureService(FeedFailureService feedFailureService) {
        this.feedFailureService = feedFailureService;
    }
}
