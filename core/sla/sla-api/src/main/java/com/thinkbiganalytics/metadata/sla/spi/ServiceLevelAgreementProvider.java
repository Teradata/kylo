/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.io.Serializable;
import java.util.List;

/**
 * A provider for creating and managing SLAs.
 * 
 * @author Sean Felten
 */
public interface ServiceLevelAgreementProvider {
    
    // TODO Add criteria-based SLA search methods
    
    /**
     * Resolves an ID from a serialized form, for instance the string result from the toSting() method of an ID.
     * @param ser some serializable form
     * @return a reconstituted SLA ID
     */
    ServiceLevelAgreement.ID resolve(Serializable ser);
    
    /**
     * @return a set of all SLAs
     */
    List<ServiceLevelAgreement> getAgreements();
    
    /**
     * @param id the ID of an SLA
     * @return the SLA corresponding to the given ID, or null if the SLA no longer exists
     */
    ServiceLevelAgreement getAgreement(ServiceLevelAgreement.ID id);
    
    /**
     * Search for an SLA by name.
     * @param slaName to name to match
     * @return the SLA or null if not found
     */
    // TODO: remove/deprecate this method when criteria-based search methods are added
    ServiceLevelAgreement findAgreementByName(String slaName);
    
    /**
     * Removes an SLA with the given ID.
     * @param id the ID of an SLA
     * @return true of the SLA existed and was SLA, otherwise false
     */
    boolean removeAgreement(ServiceLevelAgreement.ID id);

    /**
     * Produces a new builder for creating a new SLA.
     * @return the builder
     */
    ServiceLevelAgreementBuilder builder();
    
    /**
     * Produces a new builder for creating a new SLA that replaces another one having the given ID.
     * @param id the ID of the SLA that will be replaced
     * @return the builder
     */
    ServiceLevelAgreementBuilder builder(ServiceLevelAgreement.ID id);

    SLACheckBuilder slaCheckBuilder(ServiceLevelAgreement.ID slaId);
    
}
