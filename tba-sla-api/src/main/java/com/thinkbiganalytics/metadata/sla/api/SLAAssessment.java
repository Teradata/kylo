/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

import java.util.Set;

import org.joda.time.DateTime;

/**
 *
 * @author Sean Felten
 */
public interface SLAAssessment {

    DateTime getTime();
    
    SLA getSLA();
    
    AssessmentResult getResult();
    
    AssessmentSeverity getSeverity();
    
    Set<ObligationAssessment> getObligationAssessments();
}
