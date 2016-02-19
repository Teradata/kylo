/**
 * 
 */
package com.thinkbiganalytics.controller.precond;

import java.io.Serializable;
import java.util.Set;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public interface FeedPrecondition {
    
    interface ID extends Serializable { }
    
    ID getId();

    String getName();
    
    Set<Metric> getMetrics();
}
