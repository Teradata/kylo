/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed.precond;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.DataOperation;
import com.thinkbiganalytics.metadata.api.op.Dataset;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;

/**
 *
 * @author Sean Felten
 */
public abstract class MetadataMetricAssessor<M extends Metric> 
        implements MetricAssessor<M, ArrayList<Dataset<Datasource, ChangeSet>>> {

    @Inject
    private FeedProvider feedProvider;
    
    @Inject
    private FeedOperationsProvider operationsProvider;
    
    protected FeedProvider getFeedProvider() {
        return this.feedProvider;
    }
    
    protected FeedOperationsProvider getFeedOperationsProvider() {
        return this.operationsProvider;
    }
   
    protected int collectChangeSetsSince(ArrayList<Dataset<Datasource, ChangeSet>> result,
                                         List<DataOperation> testedOps,
                                         DateTime sinceTime) {
        int maxCompleteness = 0;
        
        for (DataOperation op : testedOps) {
            Dataset<Datasource, ChangeSet> cs = op.getDataset();
            
            if (cs.getCreatedTime().isBefore(sinceTime)) {
                break;
            }
            
            for (ChangeSet content : cs.getChanges()) {
                maxCompleteness = Math.max(maxCompleteness, content.getCompletenessFactor());
            }
            
            result.add(cs);
        }
        
        return maxCompleteness;
    }
}
