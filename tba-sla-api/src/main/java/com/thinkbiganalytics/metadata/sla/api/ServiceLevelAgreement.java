/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author Sean Felten
 */
public interface ServiceLevelAgreement {
    interface ID extends Serializable {};
    
    ID getId();
    
    String getName();
    
    String getDescription();
    
    Set<Obligation> getObligations();

}
