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
public class NifiStepExecutionContext extends NifiExecutionContextValues {


    @Column(name = "EXECUTION_CONTEXT_TYPE")
    private String executionContextType = "STEP";


    @EmbeddedId
    private NifiStepExecutionContextPK stepExecutionContextPK;


    @MapsId("stepExecutionId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "STEP_EXECUTION_ID", referencedColumnName = "STEP_EXECUTION_ID")
    private NifiStepExecution stepExecution;


    @Column(name = "KEY_NAME", insertable = false, updatable = false)
    private String keyName;

    public NifiStepExecutionContext() {

    }

    public NifiStepExecutionContext(NifiStepExecution stepExecution, String keyName) {
        setStepExecution(stepExecution);
        setStepExecutionContextPK(new NifiStepExecutionContextPK(stepExecution.getJobExecution().getJobExecutionId(), stepExecution.getStepExecutionId(), keyName));
    }

    @Embeddable
    public static class NifiStepExecutionContextPK implements Serializable {

        @Column(name = "JOB_EXECUTION_ID")
        private Long jobExecutionId;

        @Column(name = "STEP_EXECUTION_ID")
        private Long stepExecutionId;

        @Type(type = "com.thinkbiganalytics.jobrepo.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "250")})
        @Column(name = "KEY_NAME")
        private String keyName;

        public NifiStepExecutionContextPK() {

        }

        public NifiStepExecutionContextPK(Long jobExecutionId, Long stepExecutionId, String keyName) {
            this.jobExecutionId = jobExecutionId;
            this.stepExecutionId = stepExecutionId;
            this.keyName = keyName;
        }

        public String getKeyName() {
            return keyName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            NifiStepExecutionContextPK that = (NifiStepExecutionContextPK) o;

            if (!jobExecutionId.equals(that.jobExecutionId)) {
                return false;
            }
            if (!stepExecutionId.equals(that.stepExecutionId)) {
                return false;
            }
            return keyName.equals(that.keyName);

        }

        @Override
        public int hashCode() {
            int result = jobExecutionId.hashCode();
            result = 31 * result + stepExecutionId.hashCode();
            result = 31 * result + keyName.hashCode();
            return result;
        }
    }

    public NifiStepExecution getStepExecution() {
        return stepExecution;
    }

    public void setStepExecution(NifiStepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    public NifiStepExecutionContextPK getStepExecutionContextPK() {
        return stepExecutionContextPK;
    }

    public void setStepExecutionContextPK(NifiStepExecutionContextPK stepExecutionContextPK) {
        this.stepExecutionContextPK = stepExecutionContextPK;
    }

    public String getExecutionContextType() {
        return executionContextType;
    }

    public void setExecutionContextType(String executionContextType) {
        this.executionContextType = executionContextType;
    }

    public String getKeyName() {
        return keyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NifiStepExecutionContext that = (NifiStepExecutionContext) o;

        if (!executionContextType.equals(that.executionContextType)) {
            return false;
        }
        return stepExecutionContextPK.equals(that.stepExecutionContextPK);

    }

    @Override
    public int hashCode() {
        int result = executionContextType.hashCode();
        result = 31 * result + stepExecutionContextPK.hashCode();
        return result;
    }
}
