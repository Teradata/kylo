/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.Obligation;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
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
     * Adds an obligation to the default group of this SLA.
     * @param obligation the obligation to add
     * @return this builder
     */
    ServiceLevelAgreementBuilder obligation(Obligation obligation);
    
    /**
     * Produces a builder for adding a new obligation to the default group of this SLA.
     * @return the obligation builder
     */
    ObligationBuilder<ServiceLevelAgreementBuilder> obligationBuilder();
    
    /**
     * Convenience method that produces a builder for adding a new obligation within its own
     * group governed by the specified condition.
     * @return the obligation builder
     */
    ObligationBuilder<ServiceLevelAgreementBuilder> obligationBuilder(ObligationGroup.Condition condition);
    
    
    /**
     * Produces a builder for an obligation group to be added to this SLA under the given condition.
     * @param condition the condition controlling how this group will contribute to this SLA's assessment.
     * @return the obligation builder
     */
    ObligationGroupBuilder obligationGroupBuilder(ObligationGroup.Condition condition);
    
    /**
     * Generates the SLA and adds it to the provider that produce this builder
     * @return the SLA that was created and added to the provider
     */
    ServiceLevelAgreement build();
}
