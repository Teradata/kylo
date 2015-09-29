/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

import java.util.Set;

/**
 *
 * @author Sean Felten
 */
public interface ObligationAssessment {
    
    Obligation getObligation();
    
    String getMessage();
    
    AssessmentResult getResult();
    
    Set<MetricAssessment> getMetricAssessments();

}
