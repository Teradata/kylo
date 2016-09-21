package com.thinkbiganalytics.metadata.jpa.jobrepo.job;

import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionContext;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Created by sr186054 on 9/1/16.
 */
@Entity
@Table(name = "BATCH_JOB_EXECUTION_CONTEXT")
public class JpaBatchJobExecutionContext implements BatchJobExecutionContext {

    @Id
    @Column(name = "JOB_EXECUTION_ID")
    private Long jobExecutionId;


    @Column(name = "SHORT_CONTEXT")
    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "2500")})
    public String shortContext;

    @Lob
    @Column(name = "SERIALIZED_CONTEXT")
    public String serializedContext;

    @Override
    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    @Override
    public String getShortContext() {
        return shortContext;
    }

    public void setShortContext(String shortContext) {
        this.shortContext = shortContext;
    }

    @Override
    public String getSerializedContext() {
        return serializedContext;
    }

    public void setSerializedContext(String serializedContext) {
        this.serializedContext = serializedContext;
    }
}
