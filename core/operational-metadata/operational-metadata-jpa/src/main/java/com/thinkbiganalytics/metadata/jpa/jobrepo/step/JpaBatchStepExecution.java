package com.thinkbiganalytics.metadata.jpa.jobrepo.step;

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

import com.thinkbiganalytics.DateTimeUtil;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventStepExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecutionContextValue;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.JpaBatchJobExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiEventStepExecution;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.io.Serializable;
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
 * Entity to store the step executions for a job
 */
@Entity
@Table(name = "BATCH_STEP_EXECUTION")
public class JpaBatchStepExecution implements Serializable, BatchStepExecution {


    @TableGenerator(
        name = "STEP_EXECUTION_KEY_GENERATOR",
        table = "GENERATED_KEYS",
        pkColumnName = "PK_COLUMN",
        valueColumnName = "VALUE_COLUMN",
        pkColumnValue = "STEP_EXECUTION_ID",
        allocationSize = 1)
    @Id
    @Column(name = "STEP_EXECUTION_ID")
    @GeneratedValue(generator = "STEP_EXECUTION_KEY_GENERATOR", strategy = GenerationType.TABLE)
    private Long stepExecutionId;

    @Version
    @Column(name = "VERSION")
    private Long version = 0L;

    /**
     * ensure its length is valid before insert/update
     **/
    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "100")})
    @Column(name = "STEP_NAME")
    private String stepName;


    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "START_TIME")
    private DateTime startTime;
    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "END_TIME")
    private DateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 10, nullable = false)
    private StepStatus status = StepStatus.COMPLETED;


    @Enumerated(EnumType.STRING)
    @Column(name = "EXIT_CODE")
    private ExecutionConstants.ExitCode exitCode = ExecutionConstants.ExitCode.EXECUTING;

    @Column(name = "EXIT_MESSAGE")
    private String exitMessage;

    @Type(type = "com.thinkbiganalytics.jpa.PersistentDateTimeAsMillisLong")
    @Column(name = "LAST_UPDATED")
    private DateTime lastUpdated;


    @ManyToOne(targetEntity = JpaBatchJobExecution.class)
    @JoinColumn(name = "JOB_EXECUTION_ID", nullable = false, insertable = true, updatable = true)
    private BatchJobExecution jobExecution;


    @OneToMany(targetEntity = JpaBatchStepExecutionContextValue.class, mappedBy = "stepExecution", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BatchStepExecutionContextValue> stepExecutionContext;

    @OneToOne(targetEntity = JpaNifiEventStepExecution.class, mappedBy = "stepExecution", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private NifiEventStepExecution nifiEventStepExecution;


    public JpaBatchStepExecution() {

    }

    @Override
    public Long getStepExecutionId() {
        return stepExecutionId;
    }

    public void setStepExecutionId(Long stepExecutionId) {
        this.stepExecutionId = stepExecutionId;
    }

    @Override
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    @Override
    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public StepStatus getStatus() {
        return status;
    }

    public void setStatus(StepStatus status) {
        this.status = status;
    }

    @Override
    public BatchJobExecution getJobExecution() {
        return jobExecution;
    }

    public void setJobExecution(BatchJobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }

    @Override
    public Set<BatchStepExecutionContextValue> getStepExecutionContext() {
        return stepExecutionContext;
    }

    public void setStepExecutionContext(Set<BatchStepExecutionContextValue> stepExecutionContext) {
        if (this.stepExecutionContext == null) {
            this.stepExecutionContext = stepExecutionContext != null ? stepExecutionContext : new HashSet<>();
        }
        this.stepExecutionContext.clear();
        if (stepExecutionContext != null && !stepExecutionContext.isEmpty()) {
            this.stepExecutionContext.addAll(stepExecutionContext);
        }
    }

    public void addStepExecutionContext(BatchStepExecutionContextValue context) {
        if (getStepExecutionContext() == null) {
            setStepExecutionContext(new HashSet<>());
        }
        if (getStepExecutionContext().contains(context)) {
            getStepExecutionContext().remove(context);
        }
        getStepExecutionContext().add(context);
    }

    @Override
    public Map<String, String> getStepExecutionContextAsMap() {
        if (getStepExecutionContext() != null && !getStepExecutionContext().isEmpty()) {
            Map<String, String> map = new HashMap<>();
            getStepExecutionContext().forEach(ctx -> {
                map.put(ctx.getKeyName(), ctx.getStringVal());
            });
            return map;
        }
        return null;
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

    public void failStep() {
        setStatus(StepStatus.FAILED);
        setExitCode(ExecutionConstants.ExitCode.FAILED);
        if (endTime == null) {
            endTime = DateTimeUtil.getNowUTCTime();
        }
    }

    public void completeStep() {
        setStatus(StepStatus.COMPLETED);
        setExitCode(ExecutionConstants.ExitCode.COMPLETED);
        if (endTime == null) {
            endTime = DateTimeUtil.getNowUTCTime();
        }
    }

    @Override
    public boolean isFinished() {
        return getEndTime() != null;
    }

    @Override
    public NifiEventStepExecution getNifiEventStepExecution() {
        return nifiEventStepExecution;
    }

    public void setNifiEventStepExecution(NifiEventStepExecution nifiEventStepExecution) {
        this.nifiEventStepExecution = nifiEventStepExecution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JpaBatchStepExecution that = (JpaBatchStepExecution) o;

        if (stepExecutionId != null ? !stepExecutionId.equals(that.stepExecutionId) : that.stepExecutionId != null) {
            return false;
        }
        return !(jobExecution != null ? !jobExecution.equals(that.jobExecution) : that.jobExecution != null);

    }

    @Override
    public int hashCode() {
        int result = stepExecutionId != null ? stepExecutionId.hashCode() : 0;
        result = 31 * result + (jobExecution != null ? jobExecution.hashCode() : 0);
        return result;
    }
}
