package com.thinkbiganalytics.metadata.sla.api;

import java.io.Serializable;
import java.util.Set;

/**
 * Describes an obligation that must be met as part of an SLA.
 * 
 * @author Sean Felten
 */
public interface Obligation {

    interface ID extends Serializable {

    }

    ;

    ID getId();

    /**
     * @return a description of the obligation
     */
    String getDescription();
    
    /**
     * @return the SLA of which this obligation is a part
     */
    ServiceLevelAgreement getAgreement();
    
    /**
     * @return the group of which this obligation is a part
     */
    ObligationGroup getGroup();
    
    /**
     * @return the metrics of this obligation that are measured when this obligation is assessed
     */
    Set<Metric> getMetrics();

}
