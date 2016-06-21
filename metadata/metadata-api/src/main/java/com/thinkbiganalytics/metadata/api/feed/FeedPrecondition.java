/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 *
 * @author Sean Felten
 */
public interface FeedPrecondition {
    
    Feed<?> getFeed();

    ServiceLevelAgreement getAgreement();
    
    // TODO Add support for defining precondition check conditions
}
