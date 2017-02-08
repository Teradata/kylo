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
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceFeed;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
@SuppressWarnings("Duplicates")
public class FeedExecutedSinceFeedAssessor extends MetadataMetricAssessor<FeedExecutedSinceFeed> {

    private static final Logger LOG = LoggerFactory.getLogger(FeedExecutedSinceFeedAssessor.class);


    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof FeedExecutedSinceFeed;
    }

    @Override
    public void assess(FeedExecutedSinceFeed metric, MetricAssessmentBuilder<Serializable> builder) {
        LOG.debug("Assessing metric {}", metric.getDescription());

        FeedProvider feedProvider = getFeedProvider();
        FeedOperationsProvider opsProvider = getFeedOperationsProvider();
        List<Feed> mainFeeds = feedProvider.getFeeds(feedProvider.feedCriteria().name(metric.getFeedName()).category(metric.getCategoryName()));
        LOG.debug("Main feeds {}", mainFeeds);
        List<Feed> triggeredFeeds = feedProvider.getFeeds(feedProvider.feedCriteria().name(metric.getSinceFeedName()).category(metric.getSinceCategoryName()));
        LOG.debug("Triggered feeds {}", triggeredFeeds);

        builder.metric(metric);

        if (!mainFeeds.isEmpty() && !triggeredFeeds.isEmpty()) {
            Feed mainFeed = mainFeeds.get(0);
            Feed triggeredFeed = triggeredFeeds.get(0);
            List<FeedOperation> mainFeedOps = opsProvider.findLatestCompleted(mainFeed.getId());
            List<FeedOperation> triggeredFeedOps = opsProvider.findLatest(triggeredFeed.getId());

            if (mainFeedOps.isEmpty()) {
                // If the feed we are checking has never run then it can't have run before the "since" feed.
                LOG.debug("Main feed ops is empty");
                builder
                    .result(AssessmentResult.FAILURE)
                    .message("Main feed " + mainFeed.getName() + " has never executed.");
            } else {
                // If the "since" feed has never run then the tested feed has run before it.
                if (triggeredFeedOps.isEmpty()) {
                    LOG.debug("Triggered feed ops is empty");
                    builder
                        .result(AssessmentResult.SUCCESS)
                        .message("Triggered feed " + triggeredFeed.getName() + " has never executed");
                } else {

                    DateTime mainFeedStopTime = mainFeedOps.get(0).getStopTime();
                    DateTime triggeredFeedStartTime = triggeredFeedOps.get(0).getStartTime();
                    LOG.debug("Main feed stop time {}", mainFeedStopTime);
                    LOG.debug("Triggered feed start time {}", triggeredFeedStartTime);

                    if (mainFeedStopTime.isBefore(triggeredFeedStartTime)) {
                        LOG.debug("Main feed stop time is before triggered feed start time");
                        builder
                            .result(AssessmentResult.FAILURE)
                            .message("Main feed " + mainFeed.getName() + " has not executed since triggered feed "
                                     + triggeredFeed.getName() + ": " + triggeredFeedStartTime);
                    } else {
                        LOG.debug("Main feed stop time is after triggered feed start time");
                        boolean isMainFeedRunning = opsProvider.isFeedRunning(mainFeed.getId());
                        if (isMainFeedRunning) {
                            //todo whether to trigger the feed while the other one is already running should be a
                            // configuration parameter defined by the user
                            LOG.debug("Main feed is still running");
                            builder
                                .result(AssessmentResult.SUCCESS)
                                .message(
                                    "Triggered feed " + triggeredFeed.getName() + " has executed since feed " + mainFeed.getName() + ", but main feed " + mainFeed.getName() + " is still running");
                        } else {
                            LOG.debug("Main is not running");
                            builder
                                .result(AssessmentResult.SUCCESS)
                                .message("Triggered feed " + triggeredFeed.getName() + " has executed since main feed " + mainFeed.getName() + ".");
                        }
                    }
                }
            }
        } else {
            LOG.debug("Either triggered or main feed does not exist");
            builder
                .result(AssessmentResult.FAILURE)
                .message("Either feed " + metric.getSinceCategoryAndFeedName() + " and/or feed " + metric.getSinceCategoryAndFeedName() + " does not exist.");
        }
    }
}
