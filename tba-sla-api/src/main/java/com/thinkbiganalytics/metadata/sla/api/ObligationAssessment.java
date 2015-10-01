/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

import java.util.Set;

/**
 * Described a assessment of an obligation.
 * 
 * @author Sean Felten
 */
public interface ObligationAssessment {
    
    /**
     * @return the obligation that was assesssed
     */
    Obligation getObligation();
    
    /**
     * @return a message describing the result of the assessment
     */
    String getMessage();
    
    /**
     * @return the result status of the assessment
     */
    AssessmentResult getResult();
    
    /**
     * @return the assessments of all metrics of this obligation
     */
    Set<MetricAssessment> getMetricAssessments();

}
