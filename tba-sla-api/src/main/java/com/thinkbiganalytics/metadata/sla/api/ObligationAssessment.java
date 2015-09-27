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
    
    AssessmentResult getResult();
    
    AssessmentSeverity getSeverity();
    
    Set<MetricAssessment> getMetricAssessments();

}
