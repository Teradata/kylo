/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceLevelAssessment {
    
    public enum Result { SUCCESS, WARNING, FAILURE }
    
    @JsonSerialize(using=DateTimeSerializer.class)
    private DateTime time;
    
    private ServiceLevelAgreement agreement;
    private String message;
    private Result result;
    List<ObligationAssessment> obligationAssessments;
    
    public ServiceLevelAssessment() {
        this.obligationAssessments = new ArrayList<>();
    }
    
    public ServiceLevelAssessment(ServiceLevelAgreement agreement, DateTime time, String message, Result result) {
        this(agreement, time, message, result, new ArrayList<ObligationAssessment>());
    }

    public ServiceLevelAssessment(ServiceLevelAgreement agreement, DateTime time, String message, Result result,
            List<ObligationAssessment> obligationAssessments) {
        super();
        this.agreement = agreement;
        this.time = time;
        this.message = message;
        this.result = result;
        this.obligationAssessments = obligationAssessments;
    }

    public ServiceLevelAgreement getAgreement() {
        return agreement;
    }

    public void setAgreement(ServiceLevelAgreement agreement) {
        this.agreement = agreement;
    }

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public List<ObligationAssessment> getObligationAssessments() {
        return obligationAssessments;
    }

    public void setObligationAssessments(List<ObligationAssessment> obligationAssessments) {
        this.obligationAssessments = obligationAssessments;
    }
    
    public void addObligationAssessment(ObligationAssessment oa) {
        this.obligationAssessments.add(oa);
    }
}
