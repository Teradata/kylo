package com.thinkbiganalytics.jobrepo.jpa;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

/**
 * Created by sr186054 on 9/1/16.
 */
@Entity
@Table(name = "BATCH_EXECUTION_CONTEXT_VALUES")
public class NifiJobExecutionContext extends NifiExecutionContextValues {

    @Column(name = "EXECUTION_CONTEXT_TYPE")
    private String executionContextType = "JOB";

    @EmbeddedId
    private NifiJobExecutionContextPK jobExecutionContextPK;


    @MapsId("jobExecutionId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "JOB_EXECUTION_ID", referencedColumnName = "JOB_EXECUTION_ID")
    private NifiJobExecution jobExecution;


    public NifiJobExecutionContext() {

    }

    @Embeddable
    public static class NifiJobExecutionContextPK implements Serializable {

        @Column(name = "JOB_EXECUTION_ID")
        private Long jobExecutionId;

        @Type(type = "com.thinkbiganalytics.jobrepo.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "250")})
        @Column(name = "KEY_NAME")
        private String keyName;

        public NifiJobExecutionContextPK() {

        }

        public NifiJobExecutionContextPK(Long jobExecutionId, String keyName) {
            this.jobExecutionId = jobExecutionId;
            this.keyName = keyName;
        }

        public Long getJobExecutionId() {
            return jobExecutionId;
        }

        public void setJobExecutionId(Long jobExecutionId) {
            this.jobExecutionId = jobExecutionId;
        }

        public String getKeyName() {
            return keyName;
        }

        public void setKeyName(String keyName) {
            this.keyName = keyName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            NifiJobExecutionContextPK that = (NifiJobExecutionContextPK) o;

            if (!jobExecutionId.equals(that.jobExecutionId)) {
                return false;
            }
            return keyName.equals(that.keyName);

        }

        @Override
        public int hashCode() {
            int result = jobExecutionId.hashCode();
            result = 31 * result + keyName.hashCode();
            return result;
        }
    }

    public NifiJobExecutionContextPK getJobExecutionContextPK() {
        return jobExecutionContextPK;
    }

    public void setJobExecutionContextPK(NifiJobExecutionContextPK jobExecutionContextPK) {
        this.jobExecutionContextPK = jobExecutionContextPK;
    }

    public String getExecutionContextType() {
        return executionContextType;
    }

    public void setExecutionContextType(String executionContextType) {
        this.executionContextType = executionContextType;
    }

    public NifiJobExecution getJobExecution() {
        return jobExecution;
    }

    public void setJobExecution(NifiJobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }
}
