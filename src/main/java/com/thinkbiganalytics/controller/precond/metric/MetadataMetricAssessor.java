/**
 * 
 */
package com.thinkbiganalytics.controller.precond.metric;

import java.util.ArrayList;

import com.thinkbiganalytics.controller.metadata.MetadataProviderService;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;
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
   
}
