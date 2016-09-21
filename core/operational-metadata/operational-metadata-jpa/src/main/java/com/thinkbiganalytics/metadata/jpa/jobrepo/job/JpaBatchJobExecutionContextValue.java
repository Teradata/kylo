package com.thinkbiganalytics.metadata.jpa.jobrepo.job;

import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionContextValue;
import com.thinkbiganalytics.metadata.jpa.jobrepo.AbstractBatchExecutionContextValue;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by sr186054 on 9/1/16.
 */
@Entity
@Table(name = "BATCH_JOB_EXECUTION_CTX_VALS")
public class JpaBatchJobExecutionContextValue extends AbstractBatchExecutionContextValue implements BatchJobExecutionContextValue {



    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", unique = true)
    private String id;


    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "250")})
    @Column(name = "KEY_NAME")
    private String keyName;

    @Column(name = "JOB_EXECUTION_ID")
    private Long jobExecutionId;

    //  @ManyToOne
    //  @JoinColumn(name = "JOB_EXECUTION_ID", referencedColumnName = "JOB_EXECUTION_ID")
    // private NifiJobExecution jobExecution;


    public JpaBatchJobExecutionContextValue() {

    }

    public JpaBatchJobExecutionContextValue(BatchJobExecution jobExecution, String keyName) {
        setJobExecutionId(jobExecution.getJobExecutionId());
        this.keyName = keyName;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    @Override
    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JpaBatchJobExecutionContextValue that = (JpaBatchJobExecutionContextValue) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (keyName != null ? !keyName.equals(that.keyName) : that.keyName != null) {
            return false;
        }
        return !(jobExecutionId != null ? !jobExecutionId.equals(that.jobExecutionId) : that.jobExecutionId != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (keyName != null ? keyName.hashCode() : 0);
        result = 31 * result + (jobExecutionId != null ? jobExecutionId.hashCode() : 0);
        return result;
    }
}
