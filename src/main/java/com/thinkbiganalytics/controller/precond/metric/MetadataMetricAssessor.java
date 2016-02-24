/**
 * 
 */
package com.thinkbiganalytics.controller.precond.metric;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.thinkbiganalytics.controller.metadata.MetadataProviderService;
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

    private MetadataProviderService providerService;
    
    public MetadataMetricAssessor(MetadataProviderService service) {
        this.providerService = service;
    }
    
    public FeedProvider getFeedProvider() {
        return this.providerService.getFeedProvider();
    }
    
    public DatasetProvider getDatasetProvider() {
        return this.providerService.getDatasetProvider();
    }
    
    public DataOperationsProvider getDataOperationsProvider() {
        return this.providerService.getDataOperationsProvider();
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
