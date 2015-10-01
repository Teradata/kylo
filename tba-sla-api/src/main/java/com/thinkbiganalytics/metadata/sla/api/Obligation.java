package com.thinkbiganalytics.metadata.sla.api;

import java.util.Set;

/**
 * Describes an obligation that must be met as part of an SLA.
 * 
 * @author Sean Felten
 */
public interface Obligation {

    /**
     * @return a description of the obligation
     */
    String getDescription();
    
    /**
     * @return the SLA that this obligation is a part of
     */
    ServiceLevelAgreement getSLA();
    
    /**
     * @return the metrics of this obligation that are measured when this obligation is assessed
     */
    Set<Metric> getMetrics();

}
