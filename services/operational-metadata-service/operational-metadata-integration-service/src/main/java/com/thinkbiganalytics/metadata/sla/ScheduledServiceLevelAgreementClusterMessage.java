package com.thinkbiganalytics.metadata.sla;

import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.scheduler.JobIdentifier;

import java.io.Serializable;

/**
 * A cluster message for SLA schedules
 */
public class ScheduledServiceLevelAgreementClusterMessage implements Serializable {

    private ServiceLevelAgreement.ID slaId;

    private JobIdentifier jobIdentifier;

    public ScheduledServiceLevelAgreementClusterMessage(){

    }

    public ScheduledServiceLevelAgreementClusterMessage(ServiceLevelAgreement.ID slaId, JobIdentifier jobIdentifier){
        this.slaId = slaId;
        this.jobIdentifier = jobIdentifier;
    }

    public ServiceLevelAgreement.ID getSlaId() {
        return slaId;
    }

    public void setSlaId(ServiceLevelAgreement.ID slaId) {
        this.slaId = slaId;
    }

    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }

    public void setJobIdentifier(JobIdentifier jobIdentifier) {
        this.jobIdentifier = jobIdentifier;
    }
}
