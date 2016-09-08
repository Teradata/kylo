package com.thinkbiganalytics.jobrepo.jpa;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@Table(name = "BATCH_STEP_EXECUTION")
public class NifiStepExecution implements Serializable {

    public enum StepStatus {
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

    //TODO ensure length
    @Column(name = "STEP_NAME")
    private String stepName;


    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "START_TIME")
    private DateTime startTime;
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
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

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "LAST_UPDATED")
    private DateTime lastUpdated;



    @ManyToOne(targetEntity = NifiJobExecution.class)
    @JoinColumn(name = "JOB_EXECUTION_ID", nullable = false, insertable = true, updatable = true)
    private NifiJobExecution jobExecution;


    @OneToMany(targetEntity = NifiStepExecutionContext.class, mappedBy = "stepExecution", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NifiStepExecutionContext> stepExecutionContext = new ArrayList<>();

    @OneToOne(targetEntity = NifiEventStepExecution.class, mappedBy = "stepExecution", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private NifiEventStepExecution nifiEventStepExecution;


    public NifiStepExecution() {

    }

    public Long getStepExecutionId() {
        return stepExecutionId;
    }

    public void setStepExecutionId(Long stepExecutionId) {
        this.stepExecutionId = stepExecutionId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
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

    public StepStatus getStatus() {
        return status;
    }

    public void setStatus(StepStatus status) {
        this.status = status;
    }

    public NifiJobExecution getJobExecution() {
        return jobExecution;
    }

    public void setJobExecution(NifiJobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }

    public List<NifiStepExecutionContext> getStepExecutionContext() {
        return stepExecutionContext;
    }

    public void setStepExecutionContext(List<NifiStepExecutionContext> stepExecutionContext) {
        this.stepExecutionContext = stepExecutionContext;
    }

    public void addStepExecutionContext(NifiStepExecutionContext context) {
        if (getStepExecutionContext().contains(context)) {
            getStepExecutionContext().remove(context);
        }
        getStepExecutionContext().add(context);
    }

    public Map<String, String> getStepExecutionContextAsMap() {
        if (!getStepExecutionContext().isEmpty()) {
            Map<String, String> map = new HashMap<>();
            getStepExecutionContext().forEach(ctx -> {
                map.put(ctx.getStepExecutionContextPK().getKeyName(), ctx.getStringVal());
            });
            return map;
        }
        return null;
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

    public void failStep() {
        setStatus(StepStatus.FAILED);
        setExitCode(ExecutionConstants.ExitCode.FAILED);
    }

    public void completeStep() {
        setStatus(StepStatus.COMPLETED);
        setExitCode(ExecutionConstants.ExitCode.COMPLETED);
    }

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

        NifiStepExecution that = (NifiStepExecution) o;

        return stepExecutionId.equals(that.stepExecutionId);

    }

    @Override
    public int hashCode() {
        return stepExecutionId.hashCode();
    }
}
