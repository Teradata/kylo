package com.thinkbiganalytics.jobrepo.jpa.model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by sr186054 on 9/1/16.
 */
@Entity
@Table(name = "BATCH_STEP_EXECUTION_CTX_VALS")
public class BatchStepExecutionContextValues extends AbstractBatchExecutionContextValues {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "id", unique = true)
    private String id;

    @ManyToOne
    @JoinColumn(name = "STEP_EXECUTION_ID", referencedColumnName = "STEP_EXECUTION_ID")
    private NifiStepExecution stepExecution;

    @Column(name = "JOB_EXECUTION_ID")
    private Long jobExecutionId;


    @Type(type = "com.thinkbiganalytics.jobrepo.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "250")})
    @Column(name = "KEY_NAME")
    private String keyName;

    public BatchStepExecutionContextValues() {

    }

    public BatchStepExecutionContextValues(NifiStepExecution stepExecution, String keyName) {
        setStepExecution(stepExecution);
        this.keyName = keyName;
        this.jobExecutionId = stepExecution.getJobExecution().getJobExecutionId();
    }

    public NifiStepExecution getStepExecution() {
        return stepExecution;
    }

    public void setStepExecution(NifiStepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }


    public String getKeyName() {
        return keyName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
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

        BatchStepExecutionContextValues that = (BatchStepExecutionContextValues) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (stepExecution != null ? !stepExecution.equals(that.stepExecution) : that.stepExecution != null) {
            return false;
        }
        if (jobExecutionId != null ? !jobExecutionId.equals(that.jobExecutionId) : that.jobExecutionId != null) {
            return false;
        }
        return keyName.equals(that.keyName);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (stepExecution != null ? stepExecution.hashCode() : 0);
        result = 31 * result + (jobExecutionId != null ? jobExecutionId.hashCode() : 0);
        result = 31 * result + keyName.hashCode();
        return result;
    }
}
