/**
 *
 */
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

import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;

import javax.inject.Inject;

/**
 * Metric assessor to assess the {@link FeedOnTimeArrivalMetric}
 */
public class FeedOnTimeArrivalMetricAssessor implements MetricAssessor<FeedOnTimeArrivalMetric, Serializable> {

    private static final Logger LOG = LoggerFactory.getLogger(FeedOnTimeArrivalMetricAssessor.class);

    @Inject
    private OpsManagerFeedProvider feedProvider;

    @Inject
    private MetadataAccess metadataAccess;


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.spi.MetricAssessor#accepts(com.thinkbiganalytics.metadata.sla.api.Metric)
     */
    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof FeedOnTimeArrivalMetric;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.spi.MetricAssessor#assess(com.thinkbiganalytics.metadata.sla.api.Metric, com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void assess(FeedOnTimeArrivalMetric metric, MetricAssessmentBuilder builder) {
        LOG.debug("Assessing metric: ", metric);

        builder.metric(metric);

        String feedName = metric.getFeedName();
        DateTime lastFeedTime = feedProvider.getLastActiveTimeStamp(feedName);

        Long nowDiff = 0L;
        Period nowDiffPeriod = new Period(nowDiff.longValue());

        if(lastFeedTime != null) {
            nowDiff =   DateTime.now().getMillis() - lastFeedTime.getMillis();
            nowDiffPeriod = new Period(nowDiff.longValue());
        }
        Long latePeriodMillis = metric.getLatePeriod().toStandardDuration().getMillis();
        Long duration = CronExpressionUtil.getCronInterval(metric.getExpectedExpression());
        Period acceptedPeriod = new Period(duration + latePeriodMillis);
        Date expectedDate = CronExpressionUtil.getPreviousFireTime(metric.getExpectedExpression());
        DateTime expectedTime = new DateTime(expectedDate);
        LOG.debug("Calculated the Expected Date to be {}  ", expectedTime);
        DateTime lateTime = expectedTime.plus(metric.getLatePeriod());
        LOG.debug("CurrentTime is: {}.  Comparing {} against the lateTime of {} ", DateTime.now(), lastFeedTime, lateTime);
        builder.compareWith(expectedDate, feedName);

        if (lastFeedTime == null) {
            LOG.debug("Feed with the specified name {} not found", feedName);
            builder.message("Feed with the specified name " + feedName + " not found ")
                .result(AssessmentResult.WARNING);
        } else if (lastFeedTime.isAfter(expectedTime) && lastFeedTime.isBefore(lateTime)) {
            LOG.debug("Data for feed {} arrived on {}, which was before late time: {}", feedName, lastFeedTime, lateTime);

            builder.message("Data for feed " + feedName + " arrived on " + lastFeedTime + ", which was before late time:  " + lateTime)
                .result(AssessmentResult.SUCCESS);
        }
        else if(lastFeedTime.isAfter(lateTime)){
            LOG.debug("Data for feed {} has not arrived before the late time: {} ", feedName, lateTime);
            builder.message("Data for feed " + feedName + " has not arrived before the late time: " + lateTime + "\n The last successful feed was on " + lastFeedTime)
                .result(AssessmentResult.FAILURE);
        }
        else if (nowDiff <= (duration + latePeriodMillis)) {
            LOG.debug("Data for feed {} has arrived before the late time: {}. The last successful feed was on {}.  It has been {} since data has arrived.  The allowed duration is {} ", feedName,
                      lateTime, lastFeedTime, DateTimeUtil.formatPeriod(nowDiffPeriod), DateTimeUtil.formatPeriod(acceptedPeriod));
            builder.message("Data for feed " + feedName + " has arrived on time.  \n The last successful feed was on " + lastFeedTime + ". It has been " + DateTimeUtil
                .formatPeriod(nowDiffPeriod) + " since data has arrived.  The allowed duration is " + DateTimeUtil.formatPeriod(acceptedPeriod))
                .result(AssessmentResult.SUCCESS);
        } else if (nowDiff > (duration + latePeriodMillis)) {
            //error its been greater that the duration of the cron + lateTime
            LOG.debug("Data for feed {} has not arrived before the late time: {}. The last successful feed was on {}.  It has been {} since data has arrived.  The allowed duration is {} ", feedName,
                      lateTime, lastFeedTime,
                      DateTimeUtil.formatPeriod(nowDiffPeriod), DateTimeUtil.formatPeriod(acceptedPeriod));
            builder.message("Data for feed " + feedName + " has not arrived on time. \n The last successful feed was on " + lastFeedTime + ".  It has been " + DateTimeUtil
                .formatPeriod(nowDiffPeriod) + " since data has arrived. The allowed duration is " + DateTimeUtil.formatPeriod(acceptedPeriod))
                .result(AssessmentResult.FAILURE);
        } else if (DateTime.now().isBefore(lateTime)) { //&& lastFeedTime.isBefore(expectedTime)
            LOG.debug("CurrentTime {} is before the lateTime of {}.  Not Assessing", DateTime.now(), lateTime);
            return;
        } else {
            LOG.debug("Data for feed {} has not arrived before the late time: {} ", feedName, lateTime);

            builder.message("Data for feed " + feedName + " has not arrived before the late time: " + lateTime + "\n The last successful feed was on " + lastFeedTime)
                .result(AssessmentResult.FAILURE);
        }
    }


    public MetadataAccess getMetadataAccess() {
        return metadataAccess;
    }

    public void setMetadataAccess(MetadataAccess metadataAccess) {
        this.metadataAccess = metadataAccess;
    }
}
