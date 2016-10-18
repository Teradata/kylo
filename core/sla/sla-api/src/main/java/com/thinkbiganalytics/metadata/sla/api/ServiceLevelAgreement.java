/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementCheck;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

/**
 * Describes an SLA, which is a collection of obligations that must be met when assessed.
 * 
 * @author Sean Felten
 */
public interface ServiceLevelAgreement {
    interface ID extends Serializable {};
    
    /**
     * @return the unique ID of this SLA
     */
    ID getId();
    
    /**
     * @return the name of this SLA
     */
    String getName();
    
    /**
     * @return the time when this SLA was created
     */
    DateTime getCreatedTime();
    
    /**
     * @return a description of this SLA
     */
    String getDescription();
    
    /**
     * Gets all of the obligation groups of this SLA in assessment order.  The list
     * returned will always have at least 1 group (the default group) of none of the
     * obligations of this SLA have been explicitly grouped.
     * @return all the obligation groups
     */
    List<ObligationGroup> getObligationGroups();
    
    /**
     * Gets all obligations from that exist in this SLA, in assessment order, regardless 
     * of how they are grouped.
     * @return all obligations that make up this SLA
     */
    List<Obligation> getObligations();

    List<ServiceLevelAgreementCheck> getSlaChecks();

    boolean isEnabled();

}
