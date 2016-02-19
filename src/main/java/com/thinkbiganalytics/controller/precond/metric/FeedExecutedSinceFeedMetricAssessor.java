/**
 * 
 */
package com.thinkbiganalytics.controller.precond.metric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;

import com.thinkbiganalytics.controller.metadata.MetadataProviderService;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
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

    public FeedExecutedSinceFeedMetricAssessor(MetadataProviderService service) {
        super(service);
    }

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
                    .source(testedFeed.getId())
                    .state(State.SUCCESS));
            List<DataOperation> sinceOps = opPvdr.getDataOperations(opPvdr.dataOperationCriteria()
                    .source(sinceFeed.getId())
                    .state(State.SUCCESS));
            ArrayList<ChangeSet<Dataset, ChangedContent>> result = new ArrayList<>();
        
            
            if (sinceOps.isEmpty()) {
                builder
                    .result(AssessmentResult.FAILURE)
                    .message("The dependent feed has never executed: " + sinceFeed.getName());
            } else {
                DateTime sinceTime;
                
                if (testedOps.isEmpty()) {
                    sinceTime = new DateTime(1);
                } else {
                    DataOperation op = testedOps.iterator().next();
                    sinceTime = op.getChangeSet().getTime();
                }
                
                int incompleteness = collectChangeSetsSince(result, sinceOps, sinceTime);
                
                builder
                    .result(incompleteness > 0 ? AssessmentResult.WARNING : AssessmentResult.SUCCESS)
                    .message("There have been " + result.size() + " change sets produced since " + sinceTime)
                    .data(result);
            }
        }
    }
}
