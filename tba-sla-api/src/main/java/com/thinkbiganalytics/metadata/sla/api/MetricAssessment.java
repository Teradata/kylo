/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

/**
 *
 * @author Sean Felten
 */
public interface MetricAssessment {
    
    Metric getMetric();
    
    String getMessage();
    
    AssessmentResult getResult();

}
