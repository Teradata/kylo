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
 * @author Sean Felten
 */
public class FeedExecutedSinceFeedAssessor extends MetadataMetricAssessor<FeedExecutedSinceFeed> {

    private static final Logger LOG = LoggerFactory.getLogger(FeedExecutedSinceFeedAssessor.class);


    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof FeedExecutedSinceFeed;
    }

    @Override
    public void assess(FeedExecutedSinceFeed metric, MetricAssessmentBuilder<Serializable> builder) {
        LOG.debug("Assessing metric {}", metric.getDescription());

        FeedProvider fPvdr = getFeedProvider();
        FeedOperationsProvider opPvdr = getFeedOperationsProvider();
        List<Feed> tested = fPvdr.getFeeds(fPvdr.feedCriteria().name(metric.getFeedName()).category(metric.getCategoryName()));
        LOG.debug("Tested feeds {}", tested);
        List<Feed> since = fPvdr.getFeeds(fPvdr.feedCriteria().name(metric.getSinceFeedName()).category(metric.getSinceCategoryName()));
        LOG.debug("Since feeds {}", since);

        builder.metric(metric);
        
        if (! tested.isEmpty() && ! since.isEmpty()) {
            Feed testedFeed = tested.get(0);
            Feed sinceFeed = since.get(0);
            List<FeedOperation> testedOps = opPvdr.find(testedFeed.getId());
            List<FeedOperation> sinceOps = opPvdr.find(sinceFeed.getId());

            boolean isSinceFeedRunning = opPvdr.isFeedRunning(sinceFeed.getId());
            if (isSinceFeedRunning) {
                LOG.debug("SinceFeed is still running");
                builder
                        .result(AssessmentResult.FAILURE)
                        .message("Feed " + sinceFeed.getName() + " is still running.");
            } else if (testedOps.isEmpty()) {
                // If the feed we are checking has never run then it can't have run before the "since" feed.
                LOG.debug("TestedOps is empty");
                builder
                    .result(AssessmentResult.FAILURE)
                    .message("Feed " + testedFeed.getName() + " has never executed.");
            } else {
                // If the "since" feed has never run then the tested feed has run before it.
                if (sinceOps.isEmpty()) {
                    LOG.debug("SinceOps is empty");
                    builder
                        .result(AssessmentResult.SUCCESS)
                        .message("Feed " + sinceFeed.getName() + " has never executed since feed " + testedFeed.getName() + ".");
                } else {

                    boolean isTestedOpsRunning = opPvdr.isFeedRunning(testedFeed.getId());

                    DateTime testedTime = testedOps.get(0).getStopTime();
                    DateTime sinceTime = sinceOps.get(0).getStopTime();
                    LOG.debug("TestedTime {}", testedTime);
                    LOG.debug("SinceTime {}", sinceTime);

                    if (testedTime.isBefore(sinceTime)) {
                        LOG.debug("testedTime is before sinceTime");
                        builder
                            .result(AssessmentResult.FAILURE)
                            .message("Feed " + testedFeed.getName() + " has not executed since feed "
                                     + sinceFeed.getName() + ": " + sinceTime);
                    } else {
                        LOG.debug("testedTime is after sinceTime");
                        if (isTestedOpsRunning) {
                            LOG.debug("testedFeed is still running");
                            builder
                                .result(AssessmentResult.FAILURE)
                                .message("Feed " + sinceFeed.getName() + " has executed since feed " + testedFeed.getName() + ", but " + testedFeed.getName() + " is still running");
                        } else {
                            LOG.debug("testedFeed has finished running");
                            builder
                                .result(AssessmentResult.SUCCESS)
                                .message("Feed " + sinceFeed.getName() + " has executed since feed " + testedFeed.getName() + ".");
                        }
                    }
                }
            }
        } else {
            LOG.debug("Either tested or since feed does not exist");
            builder
                .result(AssessmentResult.FAILURE)
                .message("Either feed " + metric.getSinceCategoryAndFeedName() + " and/or feed " + metric.getSinceCategoryAndFeedName() + " does not exist.");
        }
    }
}
