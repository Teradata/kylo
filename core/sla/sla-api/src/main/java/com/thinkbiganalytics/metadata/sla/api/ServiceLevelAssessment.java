/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Set;

/**
 * The overall assessment of this SLA.
 * 
 * @author Sean Felten
 */
public interface ServiceLevelAssessment extends Comparable<ServiceLevelAssessment>, Serializable {

    interface ID extends Serializable {};

    /**
     * @return the unique ID of this assessment
     */
    ID getId();

    /**
     * @return the time when this assessment was generated
     */
    DateTime getTime();
    
    /**
     * @return the SLA that was assessed
     */
    ServiceLevelAgreement getAgreement();
    
    /**
     * @return a message describing the result of this assessment
     */
    String getMessage();
    
    /**
     * @return the result status of this assessment
     */
    AssessmentResult getResult();
    
    /**
     * @return the assessments of each of the obligations of the SLA
     */
    Set<ObligationAssessment> getObligationAssessments();


}
