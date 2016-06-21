package com.thinkbiganalytics.metadata.sla.api;

import java.io.Serializable;

/**
 * Defines a base type of any metric that must be satisfied as part of an obligation in an SLA. 
 * 
 * @author Sean Felten
 */
public interface Metric extends Serializable {
    
    /**
     * @return a description of the metric
     */
    String getDescription();

}
