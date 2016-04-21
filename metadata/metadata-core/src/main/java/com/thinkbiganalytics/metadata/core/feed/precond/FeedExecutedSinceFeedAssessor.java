/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed.precond;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperation.State;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceFeed;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
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
                       MetricAssessmentBuilder<ArrayList<Dataset<Datasource, ChangeSet>>> builder) {
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
            ArrayList<Dataset<Datasource, ChangeSet>> result = new ArrayList<>();
        
            // If the feed we are checking has never run then it can't have run before the "since" feed.
            if (testedOps.isEmpty()) {
                builder
                    .result(AssessmentResult.FAILURE)
                    .message("Feed " + testedFeed.getName() + " has executed");
            } else {
                // If the "since" feed has never run then the tested feed has run before it.
                if (sinceOps.isEmpty()) {
                    // Collects all ops that have the tested feed generated.
                    int incompleteness = collectChangeSetsSince(result, testedOps, new DateTime(1));
                    
                    builder
                        .result(incompleteness > 0 ? AssessmentResult.WARNING : AssessmentResult.SUCCESS)
                        .message("There have been a total of " + result.size() + " change sets produced")
                        .data(result);
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
