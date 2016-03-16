/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.Obligation;

/**
 *
 * @author Sean Felten
 */
public interface ObligationGroupBuilder {
    
    /**
     * Adds an obligation to the SLA.
     * @param obligation the obligation to add
     * @return this builder
     */
    ObligationGroupBuilder obligation(Obligation obligation);
    
    /**
     * Produces a builder for adding a new obligation to the SLA.
     * @return the obligation builder
     */
    ObligationBuilder<ObligationGroupBuilder> obligationBuilder();
    
    /**
     * Builds and adds the obligation group to the SLA.
     * @return the SLA builder to which this group was added
     */
    ServiceLevelAgreementBuilder build();
}
