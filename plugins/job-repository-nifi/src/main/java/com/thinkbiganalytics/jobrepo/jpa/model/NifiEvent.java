package com.thinkbiganalytics.jobrepo.jpa.model;

import com.thinkbiganalytics.jpa.AbstractAuditedEntity;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by sr186054 on 8/31/16.
 */
@Entity
@Table(name = "NIFI_EVENT")
public class NifiEvent extends AbstractAuditedEntity {

    @EmbeddedId
    private NiFiEventPK eventPK;

    @Column(name = "FM_FEED_NAME")
    private String feedName;

    @Column(name = "PROCESSOR_ID")
    private String processorId;

    @Column(name = "PROCESSOR_NAME")
    private String processorName;

    @Column(name = "FEED_PROCESS_GROUP_ID")
    private String feedProcessGroupId;

    @Column(name = "EVENT_DETAILS")
    private String eventDetails;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "EVENT_TIME")
    private DateTime eventTime;

    @Column(name = "FILE_SIZE")
    private String fileSize;

    @Column(name = "FILE_SIZE_BYTES")
    private Long fileSizeBytes;

    @Column(name = "PARENT_FLOW_FILE_IDS")
    @Type(type = "com.thinkbiganalytics.jobrepo.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "255")})
    private String parentFlowFileIds;

    @Column(name = "CHILD_FLOW_FILE_IDS")
    @Type(type = "com.thinkbiganalytics.jobrepo.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "255")})
    private String childFlowFileIds;

    @Column(name = "ATTRIBUTES_JSON")
    private String attributesJson;

    @Column(name = "SOURCE_CONNECTION_ID")
    private String sourceConnectionId;

    @Column(name = "EVENT_TYPE")
    private String eventType;

    @Column(name = "EVENT_DURATION_MILLIS")
    private Long eventDuration;

    @Column(name = "JOB_FLOW_FILE_ID")
    private String jobFlowFileId;

    @Column(name = "IS_START_OF_JOB", length = 1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean isStartOfJob;

    @Column(name = "IS_END_OF_JOB", length = 1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean isEndOfJob;

    @Column(name = "IS_FAILURE", length = 1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean isFailure;

    @Column(name = "IS_BATCH_JOB", length = 1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean isBatchJob;

    @Column(name = "IS_FINAL_JOB_EVENT", length = 1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean isFinalJobEvent;

    @Column(name = "HAS_FAILURE_EVENTS", length = 1)
    @org.hibernate.annotations.Type(type = "yes_no")
    private boolean hasFailureEvents;

    @Column(name = "EVENT_ID", insertable = false, updatable = false)
    private Long eventId;

    @Column(name = "FLOW_FILE_ID", insertable = false, updatable = false)
    private String flowFileId;


    public NifiEvent() {

    }

    public NifiEvent(Long eventId, String flowfileId) {
        this.eventPK = new NiFiEventPK(eventId, flowfileId);
    }

    public NifiEvent(NiFiEventPK eventPK) {
        this.eventPK = eventPK;
    }

    public Long getEventId() {
        return eventPK.getEventId();
    }

    public String getFlowFileId() {
        return eventPK.getFlowFileId();
    }


    @Embeddable
    public static class NiFiEventPK implements Serializable {

        @Column(name = "EVENT_ID")
        private Long eventId;

        @Column(name = "FLOW_FILE_ID")
        private String flowFileId;

        public NiFiEventPK() {

        }

        public NiFiEventPK(Long eventId, String flowFileId) {
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

            NiFiEventPK that = (NiFiEventPK) o;

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

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    public String getFeedProcessGroupId() {
        return feedProcessGroupId;
    }

    public void setFeedProcessGroupId(String feedProcessGroupId) {
        this.feedProcessGroupId = feedProcessGroupId;
    }

    public String getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }

    public DateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(DateTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }


    public String getParentFlowFileIds() {
        return parentFlowFileIds;
    }

    public void setParentFlowFileIds(String parentFlowFileIds) {
        this.parentFlowFileIds = parentFlowFileIds;
    }

    public String getChildFlowFileIds() {
        return childFlowFileIds;
    }

    public void setChildFlowFileIds(String childFlowFileIds) {
        this.childFlowFileIds = childFlowFileIds;
    }

    public String getAttributesJson() {
        return attributesJson;
    }

    public void setAttributesJson(String attributesJson) {
        this.attributesJson = attributesJson;
    }

    public String getSourceConnectionId() {
        return sourceConnectionId;
    }

    public void setSourceConnectionId(String sourceConnectionId) {
        this.sourceConnectionId = sourceConnectionId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getEventDuration() {
        return eventDuration;
    }

    public void setEventDuration(Long eventDuration) {
        this.eventDuration = eventDuration;
    }


    public String getJobFlowFileId() {
        return jobFlowFileId;
    }

    public void setJobFlowFileId(String jobFlowFileId) {
        this.jobFlowFileId = jobFlowFileId;
    }

    public boolean isStartOfJob() {
        return isStartOfJob;
    }

    public void setIsStartOfJob(boolean isStartOfJob) {
        this.isStartOfJob = isStartOfJob;
    }

    public boolean isEndOfJob() {
        return isEndOfJob;
    }

    public void setIsEndOfJob(boolean isEndOfJob) {
        this.isEndOfJob = isEndOfJob;
    }


    public boolean isFailure() {
        return isFailure;
    }

    public void setIsFailure(boolean isFailure) {
        this.isFailure = isFailure;
    }

    public boolean isBatchJob() {
        return isBatchJob;
    }

    public void setIsBatchJob(boolean isBatchJob) {
        this.isBatchJob = isBatchJob;
    }

    public boolean isFinalJobEvent() {
        return isFinalJobEvent;
    }

    public void setIsFinalJobEvent(boolean isFinalJobEvent) {
        this.isFinalJobEvent = isFinalJobEvent;
    }

    public boolean isHasFailureEvents() {
        return hasFailureEvents;
    }

    public void setHasFailureEvents(boolean hasFailureEvents) {
        this.hasFailureEvents = hasFailureEvents;
    }
}
