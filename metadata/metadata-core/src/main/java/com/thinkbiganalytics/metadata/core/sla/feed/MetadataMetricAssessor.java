/**
 * 
 */
package com.thinkbiganalytics.metadata.core.sla.feed;

import java.io.Serializable;

import javax.inject.Inject;

import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;

/**
 *
 * @author Sean Felten
 */
public abstract class MetadataMetricAssessor<M extends Metric> 
        implements MetricAssessor<M, Serializable> {

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
}
