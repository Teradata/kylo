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
public interface MetricAssessment<D extends Serializable> extends Comparable<MetricAssessment<D>>, Serializable {
    
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
    
    /**
     * Gets some arbitrary data that was attached to this assessment.  The type of data is
     * undefined and must be known by the consumer of this assessment.
     * @return the data attached to this assessment
     */
    D getData();

}
