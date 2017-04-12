/**
 *
 */
package com.thinkbiganalytics.metadata.core.sla.feed;

/*-
 * #%L
 * thinkbig-metadata-core
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

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceSchedule;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class FeedExecutedSinceScheduleAssessor extends MetadataMetricAssessor<FeedExecutedSinceSchedule> {

    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof FeedExecutedSinceSchedule;
    }

    @Override
    public void assess(FeedExecutedSinceSchedule metric, MetricAssessmentBuilder<Serializable> builder) {
        Date prev = CronExpressionUtil.getPreviousFireTime(metric.getCronExpression(), 2);
        DateTime schedTime = new DateTime(prev);
        String feedName = metric.getFeedName();
        FeedCriteria crit = getFeedProvider().feedCriteria().name(feedName);
        List<Feed> feeds = getFeedProvider().getFeeds(crit);

        if (feeds.size() > 0) {
            Feed feed = feeds.get(0);
            List<FeedOperation> list = this.getFeedOperationsProvider().findLatestCompleted(feed.getId());

            if (!list.isEmpty()) {
                FeedOperation latest = list.get(0);

                if (latest.getStopTime().isAfter(schedTime)) {
                    builder
                        .result(AssessmentResult.SUCCESS)
                        .message("Feed " + feed.getName() + " has executed at least 1 operation since " + schedTime);
                } else {
                    builder
                        .result(AssessmentResult.FAILURE)
                        .message("Feed " + feed.getName() + " has not executed any data operations since " + schedTime);
                }
            }
        }
    }

}
