package com.thinkbiganalytics.jobrepo.jpa;

import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

/**
 * Created by sr186054 on 8/31/16.
 */
@Entity
@Table(name = "BATCH_JOB_EXECUTION")
public class NifiJobExecution {

    public static enum JobStatus {
        COMPLETED,
        STARTING,
        STARTED,
        STOPPING,
        STOPPED,
        FAILED,
        ABANDONED,
        UNKNOWN;
    }




    @TableGenerator(
        name = "JOB_EXECUTION_KEY_GENERATOR",
        table = "GENERATED_KEYS",
        pkColumnName = "PK_COLUMN",
        valueColumnName = "VALUE_COLUMN",
        pkColumnValue = "JOB_EXECUTION_ID",
        allocationSize = 1)
    @Id
    @Column(name = "JOB_EXECUTION_ID")
    @GeneratedValue(generator = "JOB_EXECUTION_KEY_GENERATOR", strategy = GenerationType.TABLE)
    private Long jobExecutionId;

    @Version
    @Column(name = "VERSION")
    private Long version = 0L;


    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "CREATE_TIME")
    private DateTime createTime;
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "START_TIME")
    private DateTime startTime;
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "END_TIME")
    private DateTime endTime;


    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 10, nullable = false)
    private JobStatus status = JobStatus.STARTED;


    @Enumerated(EnumType.STRING)
    @Column(name = "EXIT_CODE")
    private ExecutionConstants.ExitCode exitCode = ExecutionConstants.ExitCode.EXECUTING;

    @Column(name = "EXIT_MESSAGE")
    private String exitMessage;
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "LAST_UPDATED")
    private DateTime lastUpdated;


    @ManyToOne(targetEntity = NifiJobInstance.class)
    @JoinColumn(name = "JOB_INSTANCE_ID", nullable = false, insertable = true, updatable = true)
    private NifiJobInstance jobInstance;

    @OneToMany(targetEntity = NifiJobExecutionParameters.class, mappedBy = "jobExecution", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NifiJobExecutionParameters> jobParameters;


    @OneToMany(targetEntity = NifiStepExecution.class, mappedBy = "jobExecution", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NifiStepExecution> stepExecutions = new HashSet<>();

    @OneToMany(targetEntity = NifiJobExecutionContext.class, mappedBy = "jobExecution", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<NifiJobExecutionContext> jobExecutionContext = new ArrayList<>();


    @OneToOne(targetEntity = NifiEventJobExecution.class, mappedBy = "jobExecution", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private NifiEventJobExecution nifiEventJobExecution;


    public NifiJobExecution() {

    }


    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    public NifiJobInstance getJobInstance() {
        return jobInstance;
    }

    public void setJobInstance(NifiJobInstance jobInstance) {
        this.jobInstance = jobInstance;
    }

    public Long getJobExecutionId() {
        return jobExecutionId;
    }


    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }


    public DateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(DateTime createTime) {
        this.createTime = createTime;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public ExecutionConstants.ExitCode getExitCode() {
        return exitCode;
    }

    public void setExitCode(ExecutionConstants.ExitCode exitCode) {
        this.exitCode = exitCode;
    }

    public String getExitMessage() {
        return exitMessage;
    }

    public void setExitMessage(String exitMessage) {
        this.exitMessage = exitMessage;
    }

    public DateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(DateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }


    public List<NifiJobExecutionParameters> getJobParameters() {
        return jobParameters;
    }

    public void setJobParameters(List<NifiJobExecutionParameters> jobParameters) {
        this.jobParameters = jobParameters;
    }


    public Set<NifiStepExecution> getStepExecutions() {
        return stepExecutions;
    }

    public void setStepExecutions(Set<NifiStepExecution> stepExecutions) {
        this.stepExecutions = stepExecutions;
    }

    public List<NifiJobExecutionContext> getJobExecutionContext() {
        return jobExecutionContext;
    }

    public void setJobExecutionContext(List<NifiJobExecutionContext> jobExecutionContext) {
        if (this.jobExecutionContext != null) {
            this.jobExecutionContext.clear();
            this.jobExecutionContext.addAll(jobExecutionContext);
        } else {
            this.jobExecutionContext = jobExecutionContext;
        }
    }

    public void addJobExecutionContext(NifiJobExecutionContext context) {
        if (getJobExecutionContext().contains(context)) {
            getJobExecutionContext().remove(context);
        }
        getJobExecutionContext().add(context);
    }

    public Map<String, String> getJobExecutionContextAsMap() {
        if (!getJobExecutionContext().isEmpty()) {
            Map<String, String> map = new HashMap<>();
            getJobExecutionContext().forEach(ctx -> {
                map.put(ctx.getJobExecutionContextPK().getKeyName(), ctx.getStringVal());
            });
            return map;
        }
        return null;
    }

    public NifiEventJobExecution getNifiEventJobExecution() {
        return nifiEventJobExecution;
    }

    public void setNifiEventJobExecution(NifiEventJobExecution nifiEventJobExecution) {
        this.nifiEventJobExecution = nifiEventJobExecution;
    }


    public void completeOrFailJob() {
        StringBuffer stringBuffer = null;
        boolean failedJob = false;
        for (NifiStepExecution se : getStepExecutions()) {
            if (NifiStepExecution.StepStatus.FAILED.equals(se.getStatus())) {
                failedJob = true;
                if (stringBuffer == null) {
                    stringBuffer = new StringBuffer();
                } else {
                    stringBuffer.append(",");
                }
                stringBuffer.append("Failed Step " + se.getStepName());
            }
        }
        if (failedJob) {
            setExitMessage(stringBuffer != null ? stringBuffer.toString() : "");
            setStatus(NifiJobExecution.JobStatus.FAILED);
            setExitCode(ExecutionConstants.ExitCode.FAILED);
        } else {
            setStatus(NifiJobExecution.JobStatus.COMPLETED);
            setExitCode(ExecutionConstants.ExitCode.COMPLETED);
        }
    }

    public boolean isFailed() {
        return JobStatus.FAILED.equals(getStatus());
    }

    public boolean isSuccess() {
        return JobStatus.COMPLETED.equals(getStatus());
    }

    public NifiJobExecutionParameters addParameter(String keyName, Object value) {
        NifiJobExecutionParameters jobExecutionParameters = new NifiJobExecutionParameters();
        jobExecutionParameters.setJobExecutionParametersPK(new NifiJobExecutionParameters.NifiJobExecutionParametersPK(getJobExecutionId(), keyName));
        jobExecutionParameters.setJobExecution(this);
        jobExecutionParameters.setStringVal(value != null ? value.toString() : null);
        return jobExecutionParameters;
    }


    public void setAsCheckDataJob(String feedNameReference) {

        NifiJobExecutionParameters feedNameRefParam = null;
        NifiJobExecutionParameters jobTypeParam = null;
        for (NifiJobExecutionParameters p : getJobParameters()) {
            if (FeedConstants.PARAM__FEED_NAME.equalsIgnoreCase(p.getJobExecutionParametersPK().getKeyName())) {
                feedNameRefParam = p;
            } else if (FeedConstants.PARAM__JOB_TYPE.equalsIgnoreCase(p.getJobExecutionParametersPK().getKeyName())) {
                jobTypeParam = p;
            }

            if (feedNameRefParam != null && jobTypeParam != null) {
                break;
            }
        }

        if (feedNameRefParam == null) {
            feedNameRefParam = addParameter(FeedConstants.PARAM__FEED_NAME, feedNameReference);
        } else {
            feedNameRefParam.setStringVal(feedNameReference);
        }

        if (jobTypeParam == null) {
            jobTypeParam = addParameter(FeedConstants.PARAM__JOB_TYPE, FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK);
        } else {
            jobTypeParam.setStringVal(FeedConstants.PARAM_VALUE__JOB_TYPE_CHECK);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NifiJobExecution that = (NifiJobExecution) o;

        return jobExecutionId.equals(that.jobExecutionId);

    }

    @Override
    public int hashCode() {
        return jobExecutionId.hashCode();
    }
}
