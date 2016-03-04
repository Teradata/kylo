/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed.precond;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;

/**
 *
 * @author Sean Felten
 */
public abstract class MetadataMetricAssessor<M extends Metric> 
        implements MetricAssessor<M, ArrayList<ChangeSet<Dataset, ChangedContent>>> {

    @Inject
    private FeedProvider feedProvider;
    
    @Inject
    private DatasetProvider datasetProvider;
    
    @Inject
    private DataOperationsProvider dataOperationsProvider;
    
    protected FeedProvider getFeedProvider() {
        return this.feedProvider;
    }
    
    protected DatasetProvider getDatasetProvider() {
        return this.datasetProvider;
    }
    
    protected DataOperationsProvider getDataOperationsProvider() {
        return this.dataOperationsProvider;
    }
   
    protected int collectChangeSetsSince(ArrayList<ChangeSet<Dataset, ChangedContent>> result,
                                         List<DataOperation> testedOps,
                                         DateTime sinceTime) {
        int maxCompleteness = 0;
        
        for (DataOperation op : testedOps) {
            ChangeSet<Dataset, ChangedContent> cs = op.getChangeSet();
            
            if (cs.getTime().isBefore(sinceTime)) {
                break;
            }
            
            for (ChangedContent content : cs.getChanges()) {
                maxCompleteness = Math.max(maxCompleteness, content.getCompletenessFactor());
            }
            
            result.add(cs);
        }
        
        return maxCompleteness;
    }
}
