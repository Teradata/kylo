package com.thinkbiganalytics.metadata.jpa.jobrepo.job;

import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.jobrepo.common.constants.FeedConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionContextValue;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionParameter;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobInstance;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiEventJobExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.step.JpaBatchStepExecution;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.HashSet;
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
public class JpaBatchJobExecution implements BatchJobExecution {


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


    @ManyToOne(targetEntity = JpaBatchJobInstance.class)
    @JoinColumn(name = "JOB_INSTANCE_ID", nullable = false, insertable = true, updatable = true)
    private BatchJobInstance jobInstance;

    @OneToMany(targetEntity = JpaBatchJobExecutionParameter.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "JOB_EXECUTION_ID", referencedColumnName = "JOB_EXECUTION_ID")
    private Set<BatchJobExecutionParameter> jobParameters;


    @OneToMany(targetEntity = JpaBatchStepExecution.class, mappedBy = "jobExecution", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.JOIN)
    private Set<BatchStepExecution> stepExecutions = new HashSet<>();

    @OneToMany(targetEntity = JpaBatchJobExecutionContextValue.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "JOB_EXECUTION_ID", referencedColumnName = "JOB_EXECUTION_ID")
    private Set<BatchJobExecutionContextValue> jobExecutionContext = new HashSet<>();


    @OneToOne(targetEntity = JpaNifiEventJobExecution.class, mappedBy = "jobExecution", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    private NifiEventJobExecution nifiEventJobExecution;


    public JpaBatchJobExecution() {

    }


    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    @Override
    public BatchJobInstance getJobInstance() {
        return jobInstance;
    }

    public void setJobInstance(BatchJobInstance jobInstance) {
        this.jobInstance = jobInstance;
    }

    @Override
    public Long getJobExecutionId() {
        return jobExecutionId;
    }


    @Override
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }


    @Override
    public DateTime getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(DateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public DateTime getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public DateTime getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    @Override
    public ExecutionConstants.ExitCode getExitCode() {
        return exitCode;
    }

    public void setExitCode(ExecutionConstants.ExitCode exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public String getExitMessage() {
        return exitMessage;
    }

    @Override
    public void setExitMessage(String exitMessage) {
        this.exitMessage = exitMessage;
    }

    @Override
    public DateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(DateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }


    @Override
    public Set<BatchJobExecutionParameter> getJobParameters() {
        return jobParameters;
    }

    public void setJobParameters(Set<BatchJobExecutionParameter> jobParameters) {
        this.jobParameters = jobParameters;
    }


    @Override
    public Set<BatchStepExecution> getStepExecutions() {
        return stepExecutions;
    }

    public void setStepExecutions(Set<BatchStepExecution> stepExecutions) {
        this.stepExecutions = stepExecutions;
    }

    @Override
    public Set<BatchJobExecutionContextValue> getJobExecutionContext() {
        return jobExecutionContext;
    }

    public void setJobExecutionContext(Set<BatchJobExecutionContextValue> jobExecutionContext) {
        this.jobExecutionContext.clear();
        if (this.jobExecutionContext != null) {
            this.jobExecutionContext.addAll(jobExecutionContext);
        }
    }

    public void addJobExecutionContext(BatchJobExecutionContextValue context) {
        if (getJobExecutionContext().contains(context)) {
            getJobExecutionContext().remove(context);
        }
        getJobExecutionContext().add(context);
    }

    @Override
    public Map<String, String> getJobExecutionContextAsMap() {
        if (!getJobExecutionContext().isEmpty()) {
            Map<String, String> map = new HashMap<>();
            getJobExecutionContext().forEach(ctx -> {
                map.put(ctx.getKeyName(), ctx.getStringVal());
            });
            return map;
        }
        return null;
    }

    @Override
    public NifiEventJobExecution getNifiEventJobExecution() {
        return nifiEventJobExecution;
    }

    public void setNifiEventJobExecution(NifiEventJobExecution nifiEventJobExecution) {
        this.nifiEventJobExecution = nifiEventJobExecution;
    }


    public void completeOrFailJob() {
        StringBuffer stringBuffer = null;
        boolean failedJob = false;
        for (BatchStepExecution se : getStepExecutions()) {
            if (BatchStepExecution.StepStatus.FAILED.equals(se.getStatus())) {
                failedJob = true;
                if (stringBuffer == null) {
                    stringBuffer = new StringBuffer();
                } else {
                    stringBuffer.append(",");
                }
                stringBuffer.append("Failed Step " + se.getStepName());
            }
            if(se.getEndTime() == null)
            {
                ((JpaBatchStepExecution)se).setEndTime(DateTimeUtil.getNowUTCTime());
            }
        }
        if (failedJob) {
            setExitMessage(stringBuffer != null ? stringBuffer.toString() : "");
            setStatus(JpaBatchJobExecution.JobStatus.FAILED);
            setExitCode(ExecutionConstants.ExitCode.FAILED);

        } else {
            setStatus(JpaBatchJobExecution.JobStatus.COMPLETED);
            setExitCode(ExecutionConstants.ExitCode.COMPLETED);
        }
        if(endTime == null){
            endTime = DateTimeUtil.getNowUTCTime();
        }
    }

    @Override
    public boolean isFailed() {
        return JobStatus.FAILED.equals(getStatus());
    }

    @Override
    public boolean isSuccess() {
        return JobStatus.COMPLETED.equals(getStatus());
    }

    public JpaBatchJobExecutionParameter addParameter(String keyName, Object value) {
        JpaBatchJobExecutionParameter jobExecutionParameters = new JpaBatchJobExecutionParameter();
        jobExecutionParameters.setJobExecutionParametersPK(new JpaBatchJobExecutionParameter.BatchJobExecutionParametersPK(getJobExecutionId(), keyName));
        jobExecutionParameters.setJobExecution(this);
        jobExecutionParameters.setStringVal(value != null ? value.toString() : null);
        return jobExecutionParameters;
    }

    @Override
    public boolean isFinished() {
        return getEndTime() != null;
    }


    public void setAsCheckDataJob(String feedNameReference) {

        BatchJobExecutionParameter feedNameRefParam = null;
        BatchJobExecutionParameter jobTypeParam = null;
        for (BatchJobExecutionParameter p : getJobParameters()) {
            if (FeedConstants.PARAM__FEED_NAME.equalsIgnoreCase(p.getKeyName())) {
                feedNameRefParam = p;
            } else if (FeedConstants.PARAM__JOB_TYPE.equalsIgnoreCase(p.getKeyName())) {
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

        JpaBatchJobExecution that = (JpaBatchJobExecution) o;

        return jobExecutionId.equals(that.jobExecutionId);

    }

    @Override
    public int hashCode() {
        return jobExecutionId.hashCode();
    }
}
