package com.thinkbiganalytics.metadata.jpa.jobrepo.job;

import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobInstance;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

/**
 * Created by sr186054 on 8/31/16.
 */
@Entity
@Table(name = "BATCH_JOB_INSTANCE")
public class JpaBatchJobInstance implements BatchJobInstance {


    @TableGenerator(
        name = "JOB_INSTANCE_KEY_GENERATOR",
        table = "GENERATED_KEYS",
        pkColumnName = "PK_COLUMN",
        valueColumnName = "VALUE_COLUMN",
        pkColumnValue = "JOB_INSTANCE_ID",
        allocationSize = 1)
    @Id
    @Column(name = "JOB_INSTANCE_ID")
    @GeneratedValue(generator = "JOB_INSTANCE_KEY_GENERATOR", strategy = GenerationType.TABLE)
    private Long jobInstanceId;
    @Version
    @Column(name = "VERSION")
    private Long version = 0L;
    @Column(name = "JOB_NAME")
    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "255")})
    private String jobName;
    @Column(name = "JOB_KEY")
    private String jobKey;

    @OneToMany(targetEntity = JpaBatchJobExecution.class, mappedBy = "jobInstance", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BatchJobExecution> jobExecutions = new ArrayList<>();


    public JpaBatchJobInstance() {

    }

    @Override
    public Long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(Long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    @Override
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public String getJobKey() {
        return jobKey;
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey;
    }

    @Override
    public List<BatchJobExecution> getJobExecutions() {
        return jobExecutions;
    }

    public void setJobExecutions(List<BatchJobExecution> jobExecutions) {
        this.jobExecutions = jobExecutions;
    }
}
