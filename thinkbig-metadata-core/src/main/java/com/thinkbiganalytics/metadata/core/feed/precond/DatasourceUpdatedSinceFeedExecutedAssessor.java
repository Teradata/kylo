/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed.precond;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperation.State;
import com.thinkbiganalytics.metadata.api.sla.DatasourceUpdatedSinceFeedExecuted;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;

/**
 *
 * @author Sean Felten
 */
public class DatasourceUpdatedSinceFeedExecutedAssessor extends MetadataMetricAssessor<DatasourceUpdatedSinceFeedExecuted> {

    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof DatasourceUpdatedSinceFeedExecuted;
    }

    @Override
    public void assess(DatasourceUpdatedSinceFeedExecuted metric,
                       MetricAssessmentBuilder<ArrayList<Dataset<Datasource, ChangeSet>>> builder) {
        FeedProvider fPvdr = getFeedProvider();
        DatasourceProvider dsPvdr = getDatasetProvider();
        DataOperationsProvider opPvdr = getDataOperationsProvider();
        Collection<Feed> feeds = fPvdr.getFeeds(fPvdr.feedCriteria().name(metric.getFeedName()));
        List<Datasource> datasources = dsPvdr.getDatasources(dsPvdr.datasetCriteria().name(metric.getDatasourceName()).limit(1));
        
        builder.metric(metric);
        
        if (! feeds.isEmpty() && ! datasources.isEmpty()) {
            Feed feed = feeds.iterator().next();
            Datasource datasource = datasources.get(0);
            List<DataOperation> feedOps = opPvdr.getDataOperations(opPvdr.dataOperationCriteria()
                    .feed(feed.getId())
                    .state(State.SUCCESS));
            List<DataOperation> datasourceOps = opPvdr.getDataOperations(opPvdr.dataOperationCriteria()
                    .dataset(datasource.getId())
                    .state(State.SUCCESS));
            ArrayList<Dataset<Datasource, ChangeSet>> result = new ArrayList<>();
        
            // If the feed we are checking has never run then it can't have run before the "since" feed.
            if (datasourceOps.isEmpty()) {
                builder
                    .result(AssessmentResult.FAILURE)
                    .message("The dependent datasource has never been updated: " + datasource.getName());
            } else {
                DateTime datasourceTime = datasourceOps.iterator().next().getStopTime();

                if (feedOps.isEmpty()) {
                    // If the datasource has been updated at least once and feed has never executed then this condition is true.
                    // Collects any datasource changes that have occurred since the feed last ran.
                    // Returns the highest found incompleteness factor.
                    int incompleteness = collectChangeSetsSince(result, datasourceOps, new DateTime(1));
                    
                    builder
                        .result(incompleteness > 0 ? AssessmentResult.WARNING : AssessmentResult.SUCCESS)
                        .message("The datasource has updated yet the feed has never been executed")
                        .data(result);
                } else {
                    DateTime feedTime = feedOps.iterator().next().getStopTime();
                    
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
