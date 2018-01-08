package com.thinkbiganalytics.metadata.jpa.jobrepo.job;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
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

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import javax.persistence.NamedNativeQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

/**
 * Entity to store batch job executions
 */
@Entity
@NamedNativeQuery(name = "BatchJobExecution.findLatestByFeed", query = "SELECT e.* "
                                                                       + "FROM   BATCH_JOB_EXECUTION e "
                                                                       + "INNER JOIN BATCH_JOB_INSTANCE i on i.JOB_INSTANCE_ID = e.JOB_INSTANCE_ID "
                                                                       + "INNER JOIN FEED f on f.ID = i.FEED_ID "
                                                                       + "inner JOIN (SELECT f2.ID as FEED_ID,MAX(END_TIME) END_TIME "
                                                                       + "                             FROM BATCH_JOB_EXECUTION e2 "
                                                                       + "                             INNER JOIN BATCH_JOB_INSTANCE i2 on i2.JOB_INSTANCE_ID = e2.JOB_INSTANCE_ID "
                                                                       + " INNER JOIN FEED f2 on f2.ID = i2.FEED_ID "
                                                                       + "  group by f2.ID) maxJobs "
                                                                       + "                             on maxJobs.FEED_ID = f.ID "
                                                                       + "                             and maxJobs.END_TIME =e.END_TIME ")
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


    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "CREATE_TIME")
    @QueryType(PropertyType.COMPARABLE)
    private DateTime createTime;
    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "START_TIME")
    @QueryType(PropertyType.COMPARABLE)
    private DateTime startTime;

    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "END_TIME")
    @QueryType(PropertyType.COMPARABLE)
    private DateTime endTime;


    @Column(name = "START_TIME", insertable = false, updatable = false)
    private Long startTimeMillis;

    @Column(name = "END_TIME", insertable = false, updatable = false)
    private Long endTimeMillis;

    @Column(name = "START_YEAR")
    private Integer startYear;

    @Column(name = "START_MONTH")
    private Integer startMonth;

    @Column(name = "START_DAY")
    private Integer startDay;

    @Column(name = "END_YEAR")
    private Integer endYear;

    @Column(name = "END_MONTH")
    private Integer endMonth;

    @Column(name = "END_DAY")
    private Integer endDay;


    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 10, nullable = false)
    private JobStatus status = JobStatus.STARTED;


    @Enumerated(EnumType.STRING)
    @Column(name = "EXIT_CODE")
    private ExecutionConstants.ExitCode exitCode = ExecutionConstants.ExitCode.EXECUTING;

    @Column(name = "EXIT_MESSAGE")
    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "2500")})
    private String exitMessage;

    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "LAST_UPDATED")
    private DateTime lastUpdated;

    @Column(name = "IS_STREAM", length = 1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean isStream;

    @ManyToOne(targetEntity = JpaBatchJobInstance.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_INSTANCE_ID", nullable = false, insertable = true, updatable = true)
    private BatchJobInstance jobInstance;

    @OneToMany(targetEntity = JpaBatchJobExecutionParameter.class, mappedBy = "jobExecution", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<BatchJobExecutionParameter> jobParameters = null;


    @OneToMany(targetEntity = JpaBatchStepExecution.class, mappedBy = "jobExecution", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.DETACH}, orphanRemoval = true)
    private Set<BatchStepExecution> stepExecutions = null;

    @OneToMany(targetEntity = JpaBatchJobExecutionContextValue.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "JOB_EXECUTION_ID", referencedColumnName = "JOB_EXECUTION_ID")
    private Set<BatchJobExecutionContextValue> jobExecutionContext = null;


    @OneToOne(targetEntity = JpaNifiEventJobExecution.class, mappedBy = "jobExecution", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private NifiEventJobExecution nifiEventJobExecution;


    public JpaBatchJobExecution() {

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

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
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
        if (startTime != null) {
            this.startYear = startTime.getYear();
            this.startMonth = startTime.getMonthOfYear();
            this.startDay = startTime.getDayOfMonth();
        }
    }

    @Override
    public DateTime getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
        if (endTime != null) {
            this.endYear = endTime.getYear();
            this.endMonth = endTime.getMonthOfYear();
            this.endDay = endTime.getDayOfMonth();
        }
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
        if (this.jobExecutionContext == null) {
            this.jobExecutionContext = jobExecutionContext != null ? jobExecutionContext : new HashSet<>();
        }
        this.jobExecutionContext.clear();
        if (jobExecutionContext != null && !jobExecutionContext.isEmpty()) {
            this.jobExecutionContext.addAll(jobExecutionContext);
        }
    }

    public void addJobExecutionContext(BatchJobExecutionContextValue context) {
        if (getJobExecutionContext() == null) {
            setJobExecutionContext(new HashSet<>());
        }
        if (!getJobExecutionContext().contains(context)) {
            getJobExecutionContext().add(context);
        }

    }

    @Override
    public boolean isStream() {
        return isStream;
    }

    public void setStream(boolean stream) {
        isStream = stream;
    }

    @Override
    public Map<String, String> getJobExecutionContextAsMap() {
        Map<String,String> map = new HashMap<>();
        if (getJobExecutionContext() != null && !getJobExecutionContext().isEmpty()) {
             getJobExecutionContext().forEach(ctx -> map.put(ctx.getKeyName(),ctx.getStringVal()));
        }
        return map;
    }

    public Map<String, BatchJobExecutionContextValue> getJobExecutionContextKeyMap() {
        if (getJobExecutionContext() != null && !getJobExecutionContext().isEmpty()) {
            return getJobExecutionContext().stream().collect(Collectors.toMap(ctx -> ctx.getKeyName(), ctx -> ctx));
        }
        return null;
    }


    public Map<String, String> getJobParametersAsMap() {
        if (getJobParameters() != null && !getJobParameters().isEmpty()) {
            return getJobParameters().stream().collect(Collectors.toMap(param -> param.getKeyName(), param -> param.getStringVal()));
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

    public Integer getStartYear() {
        return startYear;
    }

    public Integer getStartMonth() {
        return startMonth;
    }

    public Integer getStartDay() {
        return startDay;
    }

    public Integer getEndYear() {
        return endYear;
    }

    public Integer getEndMonth() {
        return endMonth;
    }

    public Integer getEndDay() {
        return endDay;
    }


    public Long getStartTimeMillis() {
        return startTimeMillis;
    }

    public Long getEndTimeMillis() {
        return endTimeMillis;
    }

    /**
     * Complete a job and mark it as failed setting its status to {@link com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution.JobStatus#FAILED}
     */
    public void failJob() {
        StringBuffer stringBuffer = null;
        setStatus(JpaBatchJobExecution.JobStatus.FAILED);
        setExitCode(ExecutionConstants.ExitCode.FAILED);
        if (endTime == null) {
            endTime = DateTimeUtil.getNowUTCTime();
        }
        Set<BatchStepExecution> steps = getStepExecutions();
        if (steps != null) {
            for (BatchStepExecution se : steps) {
                if (BatchStepExecution.StepStatus.FAILED.equals(se.getStatus())) {
                    if (stringBuffer == null) {
                        stringBuffer = new StringBuffer();
                    } else {
                        stringBuffer.append("\n");
                    }
                    stringBuffer.append("Failed Step " + se.getStepName());
                }
                if (se.getEndTime() == null) {
                    ((JpaBatchStepExecution) se).setEndTime(DateTimeUtil.getNowUTCTime());
                }
            }
            if (stringBuffer != null) {
                //append the exit message
                setExitMessage(getExitMessage() != null ? getExitMessage() + "\n" + stringBuffer.toString() : stringBuffer.toString());
            }
        }
    }

    /**
     * Complete a job and mark it as successful setting its status to {@link com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution.JobStatus#COMPLETED}
     **/
    public void completeJob() {
        setStatus(JpaBatchJobExecution.JobStatus.COMPLETED);
        if (this.exitCode == null || this.exitCode.equals(ExecutionConstants.ExitCode.EXECUTING)) {
            setExitCode(ExecutionConstants.ExitCode.COMPLETED);
        }
        if (endTime == null) {
            endTime = DateTimeUtil.getNowUTCTime();
        }
    }

    public void finishStreamingJob(){
        if (endTime == null) {
            endTime = DateTimeUtil.getNowUTCTime();
        }
    }

    /**
     * Finish the job and update teh status and end time as being completed, or failed, based upon the status of the {@link BatchStepExecution}'s
     */
    public void completeOrFailJob() {
        boolean failedJob = false;
        Set<BatchStepExecution> steps = getStepExecutions();
        if (steps != null) {
            for (BatchStepExecution se : steps) {
                if (BatchStepExecution.StepStatus.FAILED.equals(se.getStatus())) {
                    failedJob = true;
                    break;
                }
            }
        }
        if (failedJob) {
            failJob();

        } else {
            completeJob();
        }

    }

    /**
     * Check to see if the {@link #status} indicates a failed job completion
     *
     * @return {@code true} if this is a failed job, {@code false} if it has not failed
     */
    @Override
    public boolean isFailed() {
        return JobStatus.FAILED.equals(getStatus());
    }

    /**
     * Check to see if the {@link #status} indicates a successful job completion
     *
     * @return {@code true} if this is a successful job, {@code false} if it's not successful
     */
    @Override
    public boolean isSuccess() {
        return JobStatus.COMPLETED.equals(getStatus());
    }

    /**
     * Add a job parameter to this job execution
     *
     * @param keyName the parameter key
     * @param value   the parameter value
     * @return the resulting job parameter object with the passed in {@code keyName} and {@code value}
     */
    public JpaBatchJobExecutionParameter addParameter(String keyName, Object value) {
        JpaBatchJobExecutionParameter jobExecutionParameters = new JpaBatchJobExecutionParameter();
        jobExecutionParameters.setJobExecutionParametersPK(new JpaBatchJobExecutionParameter.BatchJobExecutionParametersPK(getJobExecutionId(), keyName));
        jobExecutionParameters.setJobExecution(this);
        jobExecutionParameters.setStringVal(value != null ? value.toString() : null);
        return jobExecutionParameters;
    }

    /**
     * Check to see if this job is finished
     *
     * @return {@code true} if this job has finished, {@code false} if its still running
     */
    @Override
    public boolean isFinished() {
        return getEndTime() != null;
    }


    /**
     * Mark this job execution as a data confidence check that references/checks the supplied feedNameReference
     *
     * @param feedNameReference the feed this job is checking
     * @return the job parameter with the key of {@link FeedConstants#PARAM__JOB_TYPE} and the value of {@link FeedConstants#PARAM_VALUE__JOB_TYPE_CHECK}
     */
    public Set<JpaBatchJobExecutionParameter> setAsCheckDataJob(String feedNameReference) {
        Set<JpaBatchJobExecutionParameter> updatedParams = new HashSet<>();

        JpaBatchJobExecutionParameter feedNameRefParam = null;
        JpaBatchJobExecutionParameter jobTypeParam = null;
        Set<BatchJobExecutionParameter> jobParams = getJobParameters();
        if (jobParams != null) {
            for (BatchJobExecutionParameter p : jobParams) {
                if (FeedConstants.PARAM__FEED_NAME.equalsIgnoreCase(p.getKeyName())) {
                    feedNameRefParam = (JpaBatchJobExecutionParameter) p;
                } else if (FeedConstants.PARAM__JOB_TYPE.equalsIgnoreCase(p.getKeyName())) {
                    jobTypeParam = (JpaBatchJobExecutionParameter) p;
                }

                if (feedNameRefParam != null && jobTypeParam != null) {
                    break;
                }
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

        updatedParams.add(feedNameRefParam);

        updatedParams.add(jobTypeParam);
        return updatedParams;

    }

    /**
     * A job execution equals another job execution based upon its primary key of the {@link #jobExecutionId}
     *
     * @param o a job execution to check equality
     * @return {@code true} if this job equals the incoming object, {@code false} if its not equal
     */
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JpaBatchJobExecution{");
        sb.append("jobExecutionId=").append(jobExecutionId);
        sb.append(", status=").append(status);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append('}');
        return sb.toString();
    }
}
