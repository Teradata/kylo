package com.thinkbiganalytics.sla.model;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by sr186054 on 7/23/17.
 */
public class DefaultServiceLevelAssessment {


    private String agreementName;
    private String agreementId;

    private String description;
    private DateTime createdTime;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



    private DefaultAssessment assessment;

    private List<ObligationAssessment> obligationAssessments;


    public String getAgreementName() {
        return agreementName;
    }

    public void setAgreementName(String agreementName) {
        this.agreementName = agreementName;
    }

    public String getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(String agreementId) {
        this.agreementId = agreementId;
    }

    public DateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(DateTime createdTime) {
        this.createdTime = createdTime;
    }

    public DefaultAssessment getAssessment() {
        return assessment;
    }

    public void setAssessment(DefaultAssessment assessment) {
        this.assessment = assessment;
    }

    public List<ObligationAssessment> getObligationAssessments() {
        return obligationAssessments;
    }

    public void setObligationAssessments(List<ObligationAssessment> obligationAssessments) {
        this.obligationAssessments = obligationAssessments;
    }
}
