/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

/**
 * A builder for creating new SLAs.
 * @author Sean Felten
 */
public interface ServiceLevelAgreementBuilder {

    /**
     * @param name sets the SLA name
     * @return this builder
     */
    ServiceLevelAgreementBuilder name(String name);
    
    /**
     * @param description sets the description
     * @return this builder
     */
    ServiceLevelAgreementBuilder description(String description);
    
    /**
     * Adds an obligation to the SLA.
     * @param obligation the obligation to add
     * @return this builder
     */
    ServiceLevelAgreementBuilder obligation(Obligation obligation);
    
    /**
     * Produces a builder for adding a new obligation to the SLA.
     * @return the obligation builder
     */
    ObligationBuilder obligationBuilder();
    
    /**
     * Generates the SLA and adds it to the provider that produce this builder
     * @return the SLA that was created and added to the provider
     */
    ServiceLevelAgreement build();
}
