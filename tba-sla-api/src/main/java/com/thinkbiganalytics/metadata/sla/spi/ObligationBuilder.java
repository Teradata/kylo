/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;

/**
 * A builder for producing obligations for an SLA.
 * @author Sean Felten
 */
public interface ObligationBuilder {

    /**
     * @param description sets the description
     * @return this builder
     */
    ObligationBuilder description(String descr);
    
    /**
     * @param metric a metric to add to this obligation
     * @return this builder
     */
    ObligationBuilder metric(Metric metric);
    
    /**
     * Builds the obligation but does not add it yet to the SLA
     * @return the built obligation 
     */
    Obligation build();
    
    /**
     * Builds the obligation and adds it to the SLA that is being built
     * @return the SLA builder that produced this builder
     */
    ServiceLevelAgreementBuilder add();
}
