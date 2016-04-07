/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

import java.util.List;

/**
 * Groups together a set of obligations under an assessment rule governing how these obligations
 * contribute to the overall assessment of an SLA.
 * 
 * @author Sean Felten
 */
public interface ObligationGroup {

    
    /**
     * Describes how this group will contribute to the overall assessment success of the containing SLA
     */
    enum Condition {
        REQUIRED,       // Indicates successful assessment of this group is required for SLA success
        SUFFICIENT,     
        OPTIONAL
    }
    
    Condition getCondition();
    
    List<Obligation> getObligations();

    ServiceLevelAgreement getAgreement();
}
