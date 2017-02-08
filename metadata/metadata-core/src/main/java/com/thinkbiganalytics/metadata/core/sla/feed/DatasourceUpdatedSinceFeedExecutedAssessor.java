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

import com.thinkbiganalytics.metadata.api.sla.DatasourceUpdatedSinceFeedExecuted;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;

import java.io.Serializable;

/**
 *
 */
public class DatasourceUpdatedSinceFeedExecutedAssessor extends MetadataMetricAssessor<DatasourceUpdatedSinceFeedExecuted> {

    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof DatasourceUpdatedSinceFeedExecuted;
    }

    @Override
    public void assess(DatasourceUpdatedSinceFeedExecuted metric,
                       MetricAssessmentBuilder<Serializable> builder) {
        builder
            .metric(metric)
            .message("This metric is no longer supported")
            .result(AssessmentResult.FAILURE);

//        FeedProvider fPvdr = getFeedProvider();
//        DatasourceProvider dsPvdr = getDatasetProvider();
//        DataOperationsProvider opPvdr = getDataOperationsProvider();
//        Collection<Feed> feeds = fPvdr.getFeeds(fPvdr.feedCriteria().name(metric.getFeedName()));
//        List<Datasource> datasources = dsPvdr.getDatasources(dsPvdr.datasetCriteria().name(metric.getDatasourceName()).limit(1));
//        
//        builder.metric(metric);
//        
//        if (! feeds.isEmpty() && ! datasources.isEmpty()) {
//            Feed feed = feeds.iterator().next();
//            Datasource datasource = datasources.get(0);
//            List<DataOperation> feedOps = opPvdr.getDataOperations(opPvdr.dataOperationCriteria()
//                    .feed(feed.getId())
//                    .state(State.SUCCESS));
//            List<DataOperation> datasourceOps = opPvdr.getDataOperations(opPvdr.dataOperationCriteria()
//                    .dataset(datasource.getId())
//                    .state(State.SUCCESS));
//            ArrayList<Dataset<Datasource, ChangeSet>> result = new ArrayList<>();
//        
//            // If the feed we are checking has never run then it can't have run before the "since" feed.
//            if (datasourceOps.isEmpty()) {
//                builder
//                    .result(AssessmentResult.FAILURE)
//                    .message("The dependent datasource has never been updated: " + datasource.getName());
//            } else {
//                DateTime datasourceTime = datasourceOps.iterator().next().getStopTime();
//
//                if (feedOps.isEmpty()) {
//                    // If the datasource has been updated at least once and feed has never executed then this condition is true.
//                    // Collects any datasource changes that have occurred since the feed last ran.
//                    // Returns the highest found incompleteness factor.
//                    int incompleteness = collectChangeSetsSince(result, datasourceOps, new DateTime(1));
//                    
//                    builder
//                        .result(incompleteness > 0 ? AssessmentResult.WARNING : AssessmentResult.SUCCESS)
//                        .message("The datasource has updated yet the feed has never been executed")
//                        .data(result);
//                } else {
//                    DateTime feedTime = feedOps.iterator().next().getStopTime();
//                    
//                    if (datasourceTime.isBefore(feedTime)) {
//                        builder
//                            .result(AssessmentResult.FAILURE)
//                            .message("The datasource has not been updated since " + feedTime);
//                    } else {
//                        // Collects any datasource changes that have occurred since the feed last ran.
//                        // Returns the highest found incompleteness factor.
//                        int incompleteness = collectChangeSetsSince(result, datasourceOps, feedTime);
//                        
//                        builder
//                            .result(incompleteness > 0 ? AssessmentResult.WARNING : AssessmentResult.SUCCESS)
//                            .message("There have been " + result.size() + " change sets produced since " + feedTime)
//                            .data(result);
//                    }
//                }
//            }
//        }
    }
}
