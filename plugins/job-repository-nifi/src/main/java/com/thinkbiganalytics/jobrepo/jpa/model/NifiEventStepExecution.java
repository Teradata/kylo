package com.thinkbiganalytics.jobrepo.jpa.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by sr186054 on 9/1/16.
 */
@Entity
@Table(name = "BATCH_NIFI_STEP")
public class NifiEventStepExecution implements Serializable {

    @EmbeddedId
    private NifiEventStepExecutionPK eventStepExecutionPK;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_EXECUTION_ID", nullable = false, insertable = true, updatable = true)
    private NifiJobExecution jobExecution;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "STEP_EXECUTION_ID", nullable = false, insertable = true, updatable = true)
    private NifiStepExecution stepExecution;


    @Column(name = "EVENT_ID", insertable = false, updatable = false)
    private Long eventId;

    @Column(name = "COMPONENT_ID")
    private String componentId;

    @Column(name = "JOB_FLOW_FILE_ID")
    private String jobFlowFileId;

    @Column(name = "FLOW_FILE_ID", insertable = false, updatable = false)
    private String flowFileId;


    public NifiEventStepExecution() {

    }

    public NifiEventStepExecution(Long eventId, String flowFileId) {
        this.eventStepExecutionPK = new NifiEventStepExecutionPK(eventId, flowFileId);
    }

    public NifiEventStepExecution(NifiStepExecution stepExecution, Long eventId, String flowFileId) {
        this.eventStepExecutionPK = new NifiEventStepExecutionPK(eventId, flowFileId);
        this.stepExecution = stepExecution;
        this.jobExecution = stepExecution.getJobExecution();
    }

    public NifiEventStepExecution(NifiJobExecution jobExecution, NifiStepExecution stepExecution, Long eventId, String flowFileId) {
        this.eventStepExecutionPK = new NifiEventStepExecutionPK(eventId, flowFileId);
        this.jobExecution = jobExecution;
        this.stepExecution = stepExecution;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getFlowFileId() {
        return flowFileId;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getJobFlowFileId() {
        return jobFlowFileId;
    }

    public void setJobFlowFileId(String jobFlowFileId) {
        this.jobFlowFileId = jobFlowFileId;
    }

    @Embeddable
    public static class NifiEventStepExecutionPK implements Serializable {

        @Column(name = "EVENT_ID")
        private Long eventId;

        @Column(name = "FLOW_FILE_ID")
        private String flowFileId;

        public NifiEventStepExecutionPK() {

        }

        public NifiEventStepExecutionPK(Long eventId, String flowFileId) {
            this.eventId = eventId;
            this.flowFileId = flowFileId;
        }

        public Long getEventId() {
            return eventId;
        }

        public void setEventId(Long eventId) {
            this.eventId = eventId;
        }

        public String getFlowFileId() {
            return flowFileId;
        }

        public void setFlowFileId(String flowFileId) {
            this.flowFileId = flowFileId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            NifiEventStepExecutionPK that = (NifiEventStepExecutionPK) o;

            if (!eventId.equals(that.eventId)) {
                return false;
            }
            return flowFileId.equals(that.flowFileId);

        }

        @Override
        public int hashCode() {
            int result = eventId.hashCode();
            result = 31 * result + flowFileId.hashCode();
            return result;
        }
    }


}
