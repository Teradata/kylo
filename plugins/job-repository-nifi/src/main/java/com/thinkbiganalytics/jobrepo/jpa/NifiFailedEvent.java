package com.thinkbiganalytics.jobrepo.jpa;

import com.thinkbiganalytics.jobrepo.model.FailedProvenanceEvent;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by sr186054 on 8/30/16.
 */
@Entity
@Table(name = "NIFI_EVENT_FAILURE")
public class NifiFailedEvent implements FailedProvenanceEvent {

    @EmbeddedId
    private NiFiFailedEventPK failedEventPK;

    @Column(name = "FM_FEED_NAME")
    private String feedName;

    @Column(name = "NIFI_PROCESSOR_ID")
    private String processorId;


    @Column(name = "PROCESSOR_NAME")
    private String processorName;

    @Column(name = "NIFI_FEED_PROCESS_GROUP_ID")
    private String feedProcessGroupId;

    @Column(name = "EVENT_DETAILS")
    private String eventDetails;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "EVENT_TIME")
    private DateTime eventTime;

    @Column(name = "JOB_FLOW_FILE_ID")
    private String jobFlowFileId;


    @Column(name = "EVENT_ID", insertable = false, updatable = false)
    private Long eventId;

    @Column(name = "FLOW_FILE_ID", insertable = false, updatable = false)
    private String flowFileId;


    public NifiFailedEvent() {

    }

    public NifiFailedEvent(NiFiFailedEventPK pk) {
        this.failedEventPK = pk;
    }

    public NifiFailedEvent(Long eventId, String flowFileId) {
        this.failedEventPK = new NiFiFailedEventPK(eventId, flowFileId);
    }


    @Embeddable
    public static class NiFiFailedEventPK implements Serializable {

        @Column(name = "EVENT_ID")
        private Long eventId;

        @Column(name = "FLOW_FILE_ID")
        private String flowFileId;

        public NiFiFailedEventPK() {

        }

        public NiFiFailedEventPK(Long eventId, String flowFileId) {
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
    }


    public Long getEventId() {
        return getFailedEventPK().getEventId();
    }

    public void setEventId(Long eventId) {
        getFailedEventPK().setEventId(eventId);
    }

    @Override
    public String getFeedName() {
        return feedName;
    }

    @Override
    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    @Override
    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    @Override
    public String getFlowFileId() {
        return getFailedEventPK().getFlowFileId();
    }

    public void setFlowFileId(String flowFileId) {
        this.getFailedEventPK().setFlowFileId(flowFileId);
    }

    @Override
    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    @Override
    public String getFeedProcessGroupId() {
        return feedProcessGroupId;
    }

    public void setFeedProcessGroupId(String feedProcessGroupId) {
        this.feedProcessGroupId = feedProcessGroupId;
    }

    @Override
    public String getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }

    @Override
    public DateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(DateTime eventTime) {
        this.eventTime = eventTime;
    }

    public NiFiFailedEventPK getFailedEventPK() {
        return failedEventPK;
    }

    public void setFailedEventPK(NiFiFailedEventPK failedEventPK) {
        this.failedEventPK = failedEventPK;
    }

    public String getJobFlowFileId() {
        return jobFlowFileId;
    }

    public void setJobFlowFileId(String jobFlowFileId) {
        this.jobFlowFileId = jobFlowFileId;
    }



}
