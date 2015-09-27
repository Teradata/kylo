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
public interface ServiceLevelAssessment {

    DateTime getTime();
    
    ServiceLevelAgreement getSLA();
    
    AssessmentResult getResult();
    
    AssessmentSeverity getSeverity();
    
    Set<ObligationAssessment> getObligationAssessments();
}
