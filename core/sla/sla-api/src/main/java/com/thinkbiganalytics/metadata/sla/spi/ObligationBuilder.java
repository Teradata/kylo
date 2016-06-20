/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.spi;

import java.util.Collection;

import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.Obligation;

/**
 * A builder for producing obligations for an SLA.
 * @author Sean Felten
 */
public interface ObligationBuilder<B> {

    /**
     * @param description sets the description
     * @return this builder
     */
    ObligationBuilder<B> description(String descr);
    
    /**
     * @param metric a metric to add to this obligation
     * @param more optional additional metrics to add
     * @return this builder
     */
    ObligationBuilder<B> metric(Metric metric, Metric... more);

    /**
     * @param metrics metrics to add to this obligation
     * @return this builder
     */
    ObligationBuilder<B> metric(Collection<Metric> metrics);
    
    /**
     * Builds the obligation and adds it to the SLA that is being built
     * @return the SLA builder that produced this builder
     */
    B build();
    
}
