/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed.precond;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.precond.DatasourceUpdatedSinceFeedExecutedMetric;
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
public class DatasourceUpdatedSinceFeedExecutedAssessor extends MetadataMetricAssessor<DatasourceUpdatedSinceFeedExecutedMetric> {

    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof DatasourceUpdatedSinceFeedExecutedMetric;
    }

    @Override
    public void assess(DatasourceUpdatedSinceFeedExecutedMetric metric,
                       MetricAssessmentBuilder<ArrayList<ChangeSet<Dataset, ChangedContent>>> builder) {
        FeedProvider fPvdr = getFeedProvider();
        DatasetProvider dsPvdr = getDatasetProvider();
        DataOperationsProvider opPvdr = getDataOperationsProvider();
        Collection<Feed> feeds = fPvdr.getFeeds(fPvdr.feedCriteria().name(metric.getFeedName()));
        Set<Dataset> datasources = dsPvdr.getDatasets(dsPvdr.datasetCriteria().name(metric.getDatasetName()));
        
        builder.metric(metric);
        
        if (! feeds.isEmpty() && ! datasources.isEmpty()) {
            Feed feed = feeds.iterator().next();
            Dataset datasource = datasources.iterator().next();
            List<DataOperation> feedOps = opPvdr.getDataOperations(opPvdr.dataOperationCriteria()
                    .feed(feed.getId())
                    .state(State.SUCCESS));
            List<DataOperation> datasourceOps = opPvdr.getDataOperations(opPvdr.dataOperationCriteria()
                    .dataset(datasource.getId())
                    .state(State.SUCCESS));
            ArrayList<ChangeSet<Dataset, ChangedContent>> result = new ArrayList<>();
        
            // If the feed we are checking has never run then it can't have run before the "since" feed.
            if (datasourceOps.isEmpty()) {
                builder
                    .result(AssessmentResult.FAILURE)
                    .message("The dependent datasource has never been updated: " + datasource.getName());
            } else {
                if (feedOps.isEmpty()) {
                    builder
                        .result(AssessmentResult.FAILURE)
                        .message("The feed has never been executed");
                } else {
                    DateTime feedTime = feedOps.iterator().next().getStopTime();
                    DateTime datasourceTime = datasourceOps.iterator().next().getStopTime();
                    
                    if (datasourceTime.isBefore(feedTime)) {
                        builder
                            .result(AssessmentResult.FAILURE)
                            .message("The datasource has not been updated since " + feedTime);
                    } else {
                        // Collects any datasource changes that have occurred since the feed last ran.
                        // Returns the highest found incompleteness factor.
                        int incompleteness = collectChangeSetsSince(result, datasourceOps, feedTime);
                        
                        builder
                            .result(incompleteness > 0 ? AssessmentResult.WARNING : AssessmentResult.SUCCESS)
                            .message("There have been " + result.size() + " change sets produced since " + feedTime)
                            .data(result);
                    }
                }
            }
        }
    }
}
