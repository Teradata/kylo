/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed.precond;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.precond.FeedExecutedSinceFeedMetric;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperation.State;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;

/**
 *
 * @author Sean Felten
 */
public class FeedExecutedSinceFeedMetricAssessor extends MetadataMetricAssessor<FeedExecutedSinceFeedMetric> {

    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof FeedExecutedSinceFeedMetric;
    }

    @Override
    public void assess(FeedExecutedSinceFeedMetric metric,
                       MetricAssessmentBuilder<ArrayList<ChangeSet<Dataset, ChangedContent>>> builder) {
        FeedProvider fPvdr = getFeedProvider();
        DataOperationsProvider opPvdr = getDataOperationsProvider();
        Collection<Feed> tested = fPvdr.getFeeds(fPvdr.feedCriteria().name(metric.getFeedName()));
        Collection<Feed> since = fPvdr.getFeeds(fPvdr.feedCriteria().name(metric.getSinceName()));
        
        builder.metric(metric);
        
        if (! tested.isEmpty() && ! since.isEmpty()) {
            Feed testedFeed = tested.iterator().next();
            Feed sinceFeed = since.iterator().next();
            List<DataOperation> testedOps = opPvdr.getDataOperations(opPvdr.dataOperationCriteria()
                    .feed(testedFeed.getId())
                    .state(State.SUCCESS));
            List<DataOperation> sinceOps = opPvdr.getDataOperations(opPvdr.dataOperationCriteria()
                    .feed(sinceFeed.getId())
                    .state(State.SUCCESS));
            ArrayList<ChangeSet<Dataset, ChangedContent>> result = new ArrayList<>();
        
            // If the feed we are checking has never run then it can't have run before the "since" feed.
            if (testedOps.isEmpty()) {
                builder
                    .result(AssessmentResult.FAILURE)
                    .message("Feed " + testedFeed.getName() + " has executed");
            } else {
                if (sinceOps.isEmpty()) {
                    builder
                        .result(AssessmentResult.FAILURE)
                        .message("Feed " + sinceFeed.getName() + " has never executed");
                } else {
                    DateTime testedTime = testedOps.iterator().next().getStopTime();
                    DateTime sinceTime = sinceOps.iterator().next().getStopTime();
                   
                    if (testedTime.isBefore(sinceTime)) {
                        builder
                            .result(AssessmentResult.FAILURE)
                            .message("Feed " + testedFeed.getName() + " has not executed since feed " 
                                    + sinceFeed.getName() + ": " + sinceTime);
                    } else {
                        // Collects any feed ops that have run since the "since" time (may be none)
                        int incompleteness = collectChangeSetsSince(result, testedOps, sinceTime);
                        
                        builder
                            .result(incompleteness > 0 ? AssessmentResult.WARNING : AssessmentResult.SUCCESS)
                            .message("There have been " + result.size() + " change sets produced since " + sinceTime)
                            .data(result);
                    }
                }
            }
        }
    }
}
