/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

import java.io.Serializable;
import java.util.Set;

import org.joda.time.DateTime;

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
    DateTime getCreationTime();
    
    /**
     * @return a description of this SLA
     */
    String getDescription();
    
    /**
     * @return all obligations that make up this SLA
     */
    Set<Obligation> getObligations();

}
