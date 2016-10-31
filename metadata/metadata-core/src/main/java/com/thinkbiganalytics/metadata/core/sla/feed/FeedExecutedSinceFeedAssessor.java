/**
 * 
 */
package com.thinkbiganalytics.metadata.core.sla.feed;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceFeed;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Sean Felten
 */
public class FeedExecutedSinceFeedAssessor extends MetadataMetricAssessor<FeedExecutedSinceFeed> {

    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof FeedExecutedSinceFeed;
    }

    @Override
    public void assess(FeedExecutedSinceFeed metric,
                       MetricAssessmentBuilder<Serializable> builder) {
        FeedProvider fPvdr = getFeedProvider();
        FeedOperationsProvider opPvdr = getFeedOperationsProvider();
        List<Feed> tested = fPvdr.getFeeds(fPvdr.feedCriteria().name(metric.getFeedName()).category(metric.getCategoryName()));
        List<Feed> since = fPvdr.getFeeds(fPvdr.feedCriteria().name(metric.getSinceFeedName()).category(metric.getSinceCategoryName()));
        
        builder.metric(metric);
        
        if (! tested.isEmpty() && ! since.isEmpty()) {
            Feed testedFeed = tested.get(0);
            Feed sinceFeed = since.get(0);
            List<FeedOperation> testedOps = opPvdr.find(testedFeed.getId());
            List<FeedOperation> sinceOps = opPvdr.find(sinceFeed.getId());


            // If the feed we are checking has never run then it can't have run before the "since" feed.
            if (testedOps.isEmpty()) {
                builder
                    .result(AssessmentResult.FAILURE)
                    .message("Feed " + testedFeed.getName() + " has never executed.");
            } else {
                // If the "since" feed has never run then the tested feed has run before it.
                if (sinceOps.isEmpty()) {
                    builder
                        .result(AssessmentResult.SUCCESS)
                        .message("Feed " + sinceFeed.getName() + " has never executed since feed " + testedFeed.getName() + ".");
                } else {

                    boolean isTestedOpsRunning = opPvdr.isFeedRunning(testedFeed.getId());

                    DateTime testedTime = testedOps.get(0).getStopTime();
                    DateTime sinceTime = sinceOps.get(0).getStopTime();

                    if (testedTime.isBefore(sinceTime)) {
                        builder
                            .result(AssessmentResult.FAILURE)
                            .message("Feed " + testedFeed.getName() + " has not executed since feed "
                                     + sinceFeed.getName() + ": " + sinceTime);
                    } else {
                        if (isTestedOpsRunning) {
                            builder
                                .result(AssessmentResult.FAILURE)
                                .message("Feed " + sinceFeed.getName() + " has executed since feed " + testedFeed.getName() + ", but " + testedFeed.getName() + " is still running");
                        } else {
                            builder
                                .result(AssessmentResult.SUCCESS)
                                .message("Feed " + sinceFeed.getName() + " has executed since feed " + testedFeed.getName() + ".");
                        }
                    }
                }
            }
        } else {
            builder
                .result(AssessmentResult.FAILURE)
                .message("Either feed " + metric.getSinceCategoryAndFeedName() + " and/or feed " + metric.getSinceCategoryAndFeedName() + " does not exist.");
        }
    }
}
