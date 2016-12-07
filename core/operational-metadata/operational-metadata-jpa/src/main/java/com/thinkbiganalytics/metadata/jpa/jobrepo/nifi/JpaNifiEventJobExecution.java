package com.thinkbiganalytics.metadata.jpa.jobrepo.nifi;

import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventJobExecution;
import com.thinkbiganalytics.metadata.jpa.jobrepo.job.JpaBatchJobExecution;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Maps the NiFi Provenance Event to the BATCH_JOB_EXECUTION (BatchJobExecution)
 *
 *
 * Created by sr186054 on 9/1/16.
 */
@Entity
@Table(name = "BATCH_NIFI_JOB")
public class JpaNifiEventJobExecution implements Serializable, NifiEventJobExecution {

    //@EmbeddedId
    //private NifiEventJobExecutionPK eventJobExecutionPK;

    @OneToOne(targetEntity = JpaBatchJobExecution.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "JOB_EXECUTION_ID", nullable = false, insertable = true, updatable = true)
    private BatchJobExecution jobExecution;


    @Column(name = "EVENT_ID")
    private Long eventId;

    @Id
    @Column(name = "FLOW_FILE_ID")
    private String flowFileId;


    public JpaNifiEventJobExecution() {

    }

    public JpaNifiEventJobExecution(Long eventId, String flowFileId) {
        this.flowFileId = flowFileId;
       // this.eventJobExecutionPK = new NifiEventJobExecutionPK( flowFileId);
        this.eventId = eventId;
    }

    public JpaNifiEventJobExecution(BatchJobExecution jobExecution,Long eventId, String flowFileId) {
        this.flowFileId = flowFileId;
        //this.eventJobExecutionPK = new NifiEventJobExecutionPK(flowFileId);
        this.eventId = eventId;
        this.jobExecution = jobExecution;
    }

    //@Embeddable
    public static class NifiEventJobExecutionPK implements Serializable {


    //    @Column(name = "FLOW_FILE_ID")
        private String flowFileId;

        public NifiEventJobExecutionPK() {

        }

        public NifiEventJobExecutionPK(String flowFileId) {
             this.flowFileId = flowFileId;
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

            return !(flowFileId != null ? !flowFileId.equals(that.flowFileId) : that.flowFileId != null);

        }

        @Override
        public int hashCode() {
            return flowFileId != null ? flowFileId.hashCode() : 0;
        }
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

/*    public NifiEventJobExecutionPK getEventJobExecutionPK() {
        return eventJobExecutionPK;
    }

    public void setEventJobExecutionPK(NifiEventJobExecutionPK eventJobExecutionPK) {
        this.eventJobExecutionPK = eventJobExecutionPK;
    }
*/
    @Override
    public BatchJobExecution getJobExecution() {
        return jobExecution;
    }

    public void setJobExecution(BatchJobExecution jobExecution) {
        this.jobExecution = jobExecution;
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

        JpaNifiEventJobExecution that = (JpaNifiEventJobExecution) o;

        return !(flowFileId != null ? !flowFileId.equals(that.flowFileId) : that.flowFileId != null);

    }

    @Override
    public int hashCode() {
        return flowFileId != null ? flowFileId.hashCode() : 0;
    }
}
