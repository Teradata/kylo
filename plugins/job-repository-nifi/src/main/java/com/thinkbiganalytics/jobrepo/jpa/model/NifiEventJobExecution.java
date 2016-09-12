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
 *
 * Maps the NiFi Provenance Event to the BATCH_JOB_EXECUTION (BatchJobExecution)
 *
 *
 * Created by sr186054 on 9/1/16.
 */
@Entity
@Table(name = "BATCH_NIFI_JOB")
public class NifiEventJobExecution implements Serializable {

    @EmbeddedId
    private NifiEventJobExecutionPK eventJobExecutionPK;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_EXECUTION_ID", nullable = false, insertable = true, updatable = true)
    private NifiJobExecution jobExecution;


    @Column(name = "EVENT_ID", insertable = false, updatable = false)
    private Long eventId;

    @Column(name = "FLOW_FILE_UUID", insertable = false, updatable = false)
    private String flowFileId;


    public NifiEventJobExecution() {

    }

    public NifiEventJobExecution(Long eventId, String flowFileId) {
        this.eventJobExecutionPK = new NifiEventJobExecutionPK(eventId, flowFileId);
    }

    public NifiEventJobExecution(NifiJobExecution jobExecution, Long eventId, String flowFileId) {
        this.eventJobExecutionPK = new NifiEventJobExecutionPK(eventId, flowFileId);
        this.jobExecution = jobExecution;
    }

    @Embeddable
    public static class NifiEventJobExecutionPK implements Serializable {

        @Column(name = "EVENT_ID")
        private Long eventId;

        @Column(name = "FLOW_FILE_UUID")
        private String flowFileId;

        public NifiEventJobExecutionPK() {

        }

        public NifiEventJobExecutionPK(Long eventId, String flowFileId) {
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

            NifiEventJobExecutionPK that = (NifiEventJobExecutionPK) o;

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

    public NifiEventJobExecutionPK getEventJobExecutionPK() {
        return eventJobExecutionPK;
    }

    public void setEventJobExecutionPK(NifiEventJobExecutionPK eventJobExecutionPK) {
        this.eventJobExecutionPK = eventJobExecutionPK;
    }

    public NifiJobExecution getJobExecution() {
        return jobExecution;
    }

    public void setJobExecution(NifiJobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }


}
