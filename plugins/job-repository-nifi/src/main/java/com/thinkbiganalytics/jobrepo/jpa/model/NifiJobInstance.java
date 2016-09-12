package com.thinkbiganalytics.jobrepo.jpa.model;

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
public class NifiJobInstance {


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
    private String jobName;
    @Column(name = "JOB_KEY")
    private String jobKey;

    @OneToMany(targetEntity = NifiJobExecution.class, mappedBy = "jobInstance", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NifiJobExecution> jobExecutions = new ArrayList<>();


    public NifiJobInstance() {

    }

    public Long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(Long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobKey() {
        return jobKey;
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey;
    }

    public List<NifiJobExecution> getJobExecutions() {
        return jobExecutions;
    }

    public void setJobExecutions(List<NifiJobExecution> jobExecutions) {
        this.jobExecutions = jobExecutions;
    }
}
