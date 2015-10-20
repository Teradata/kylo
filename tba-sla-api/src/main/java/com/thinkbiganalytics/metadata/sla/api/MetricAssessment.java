/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

import java.io.Serializable;

/**
 * Reports an assessment of a metric.
 * 
 * @author Sean Felten
 */
public interface MetricAssessment extends Comparable<MetricAssessment>, Serializable {
    
    /**
     * @return the metric that was assessed
     */
    Metric getMetric();
    
    /**
     * @return a message describing the assessment result
     */
    String getMessage();
    
    /**
     * @return the result status of the assessment
     */
    AssessmentResult getResult();

}
