package com.thinkbiganalytics.metadata.jpa.jobrepo.nifi;

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

import com.thinkbiganalytics.jpa.AbstractAuditedEntity;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEvent;

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
 * Entity to store the NiFi Provenance Events
 */
@Deprecated
@Entity
@Table(name = "NIFI_EVENT")
public class JpaNifiEvent extends AbstractAuditedEntity implements NifiEvent {

    @Column(name = "CLUSTER_NODE_ID")
    protected String clusterNodeId;
    @Column(name = "CLUSTER_NODE_ADDRESS")
    protected String clusterNodeAddress;
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
    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "255")})
    private String parentFlowFileIds;
    @Column(name = "CHILD_FLOW_FILE_IDS")
    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "255")})
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


    public JpaNifiEvent() {

    }

    public JpaNifiEvent(Long eventId, String flowfileId) {
        this.eventPK = new NiFiEventPK(eventId, flowfileId);
    }

    public JpaNifiEvent(NiFiEventPK eventPK) {
        this.eventPK = eventPK;
    }

    @Override
    public Long getEventId() {
        return eventPK.getEventId();
    }

    public String getFlowFileId() {
        return eventPK.getFlowFileId();
    }

    @Override
    public String getFeedName() {
        return feedName;
    }

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

    @Override
    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    @Override
    public String getParentFlowFileIds() {
        return parentFlowFileIds;
    }

    public void setParentFlowFileIds(String parentFlowFileIds) {
        this.parentFlowFileIds = parentFlowFileIds;
    }

    @Override
    public String getChildFlowFileIds() {
        return childFlowFileIds;
    }

    public void setChildFlowFileIds(String childFlowFileIds) {
        this.childFlowFileIds = childFlowFileIds;
    }

    @Override
    public String getAttributesJson() {
        return attributesJson;
    }

    public void setAttributesJson(String attributesJson) {
        this.attributesJson = attributesJson;
    }

    @Override
    public String getSourceConnectionId() {
        return sourceConnectionId;
    }

    public void setSourceConnectionId(String sourceConnectionId) {
        this.sourceConnectionId = sourceConnectionId;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @Override
    public Long getEventDuration() {
        return eventDuration;
    }

    public void setEventDuration(Long eventDuration) {
        this.eventDuration = eventDuration;
    }

    @Override
    public String getJobFlowFileId() {
        return jobFlowFileId;
    }

    public void setJobFlowFileId(String jobFlowFileId) {
        this.jobFlowFileId = jobFlowFileId;
    }

    @Override
    public boolean isStartOfJob() {
        return isStartOfJob;
    }

    public void setIsStartOfJob(boolean isStartOfJob) {
        this.isStartOfJob = isStartOfJob;
    }

    @Override
    public boolean isEndOfJob() {
        return isEndOfJob;
    }

    public void setIsEndOfJob(boolean isEndOfJob) {
        this.isEndOfJob = isEndOfJob;
    }

    @Override
    public boolean isFailure() {
        return isFailure;
    }

    public void setIsFailure(boolean isFailure) {
        this.isFailure = isFailure;
    }

    @Override
    public boolean isBatchJob() {
        return isBatchJob;
    }

    public void setIsBatchJob(boolean isBatchJob) {
        this.isBatchJob = isBatchJob;
    }

    @Override
    public boolean isFinalJobEvent() {
        return isFinalJobEvent;
    }

    public void setIsFinalJobEvent(boolean isFinalJobEvent) {
        this.isFinalJobEvent = isFinalJobEvent;
    }

    @Override
    public boolean isHasFailureEvents() {
        return hasFailureEvents;
    }

    public void setHasFailureEvents(boolean hasFailureEvents) {
        this.hasFailureEvents = hasFailureEvents;
    }

    public String getClusterNodeId() {
        return clusterNodeId;
    }

    public void setClusterNodeId(String clusterNodeId) {
        this.clusterNodeId = clusterNodeId;
    }

    public String getClusterNodeAddress() {
        return clusterNodeAddress;
    }

    public void setClusterNodeAddress(String clusterNodeAddress) {
        this.clusterNodeAddress = clusterNodeAddress;
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
}
