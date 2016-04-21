/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import java.util.Set;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public interface FeedPrecondition {
    
    String getName();
    
    String getDescription();
    
    Set<Metric> getMetrics();

}
