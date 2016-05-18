/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import java.util.Set;

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
public interface FeedPrecondition {
    
    String getName();
    
    String getDescription();
    
    Set<Metric> getMetrics();

    ServiceLevelAgreement getAgreement();
}
