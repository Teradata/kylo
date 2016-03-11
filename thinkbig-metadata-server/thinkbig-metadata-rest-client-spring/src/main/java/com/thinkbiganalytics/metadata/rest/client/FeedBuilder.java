/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.client;

import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.sla.Metric;

/**
 *
 * @author Sean Felten
 */
public interface FeedBuilder {

    FeedBuilder displayName(String name);
    FeedBuilder description(String descr);
    FeedBuilder owner(String owner);
    FeedBuilder preconditionMetric(Metric... metrics);
    
    Feed build();
    Feed post();
}
