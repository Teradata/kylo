/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;

/**
 *
 * @author Sean Felten
 */
public interface FeedPrecondition {
    
    Feed<?> getFeed();

    ServiceLevelAgreement getAgreement();
    
    ServiceLevelAssessment getLastAssessment();
    
    void setLastAssessment(ServiceLevelAssessment assmnt);
    
    // TODO Add support for defining precondition check conditions
}
