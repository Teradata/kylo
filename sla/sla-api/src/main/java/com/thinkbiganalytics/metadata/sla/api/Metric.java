package com.thinkbiganalytics.metadata.sla.api;

/**
 * Defines a base type of any metric that must be satisfied as part of an obligation in an SLA. 
 * 
 * @author Sean Felten
 */
public interface Metric {
    
    /**
     * @return a description of the metric
     */
    String getDescription();

}
