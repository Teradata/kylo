/**
 * 
 */
package com.thinkbiganalytics.metadata.core.sla.feed;

import java.io.Serializable;
import java.util.List;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceFeed;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;

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
            List<FeedOperation> testedOps = opPvdr.find(testedFeed.getId(), 1);
            List<FeedOperation> sinceOps = opPvdr.find(sinceFeed.getId(), 1);
        
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
                        .message("Feed " + sinceFeed.getName() + " has never exectued since feed " + testedFeed.getName() + ".");
                } else {
                    DateTime testedTime = testedOps.get(0).getStopTime();
                    DateTime sinceTime = sinceOps.get(0).getStopTime();
                   
                    if (testedTime.isBefore(sinceTime)) {
                        builder
                            .result(AssessmentResult.FAILURE)
                            .message("Feed " + testedFeed.getName() + " has not executed since feed " 
                                    + sinceFeed.getName() + ": " + sinceTime);
                    } else {
                        builder
                            .result(AssessmentResult.SUCCESS)
                            .message("Feed " + sinceFeed.getName() + " has exectued since feed " + testedFeed.getName() + ".");
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
