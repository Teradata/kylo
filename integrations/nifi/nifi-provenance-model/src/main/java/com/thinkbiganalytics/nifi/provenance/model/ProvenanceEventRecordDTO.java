package com.thinkbiganalytics.nifi.provenance.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thinkbiganalytics.nifi.provenance.model.util.ProvenanceEventUtil;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by sr186054 on 2/24/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProvenanceEventRecordDTO implements Serializable {

    private transient AtomicBoolean processed = new AtomicBoolean(false);

    /**
     * Indicates the start of a Root Flow file
     */
    private boolean isStartOfJob;

    /**
     * Indicates the end of a Root Flow File
     */
    private boolean isEndOfJob;

    /**
     * indicates it is the final event in a root file or if the root file has child root flow files (as a result of a Merge) when this is true it is the final event for all of the root flow files
     */
    private boolean isFinalJobEvent;

    /**
     * inidcates if this is a Batch Root Flow file
     */
    private boolean isBatchJob;

    /**
     * Track all failures related to this jobFlowFileId if this is a finalJobEvent
     */
    private boolean hasFailedEvents;

    /**
     * The previous event that was captured before this one
     */
    private transient ProvenanceEventRecordDTO previousEvent;

    private Long previousEventId;

    /**
     * The previous events flow file id
     */
    private String previousFlowfileId;

    /**
     * the event Time of the previous event to help determine event duration
     */
    private DateTime previousEventTime;

    private String id;
    private Long eventId;
    private DateTime eventTime;
    private Long eventDuration;
    private String eventType;
    private String flowFileUuid;
    private String fileSize;
    private Long fileSizeBytes;
    private String clusterNodeId;
    private String clusterNodeAddress;
    private String groupId;
    private String componentId;
    private String componentType;
    private String componentName;
    private transient Collection<ProvenanceEventAttributeDTO> attributes;
    private List<String> parentUuids;
    private List<String> childUuids;
    private String details;

    private String sourceConnectionIdentifier;

    private Long inputContentClaimFileSizeBytes;
    private String inputContentClaimFileSize;
    private Long outputContentClaimFileSizeBytes;
    private String outputContentClaimFileSize;

    private Set<String>relatedRootFlowFiles;


    /**
     * if this event came from a processor or connection that has the keyword "failure" or is "Auto terminated by Failure event"
     */
    private boolean isFailure;

    /**
     * set when the aggregator is determining if the flow of events indicate a stream (rapid fire)
     */
    private boolean stream;

    /**
     * The flow file id the corresponds to the parent /starting event
     */
    private String jobFlowFileId;

    /**
     * The Id that corresponds to the first event that started the job
     */
    private Long jobEventId;

    //flow information  the following are set via looking at the rest api graph of events

    /**
     * The Feed name  {category}.{feed}
     */
    private String feedName;
    /**
     * The processgroup that is holding the flow for this feed
     */
    private String feedProcessGroupId;

    /**
     * the flow file and all of its relationships to other flow files/events
     */
    private transient ActiveFlowFile flowFile;


    private String batchId;

    public ProvenanceEventRecordDTO() {


    }

    public ActiveFlowFile getFlowFile() {
        return flowFile;
    }

    public void setFlowFile(ActiveFlowFile flowFile) {
        this.flowFile = flowFile;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public boolean isTerminatedByFailureRelationship() {
        return ProvenanceEventUtil.isTerminatedByFailureRelationship(this);
    }



    public boolean isEndingFlowFileEvent() {
        return ProvenanceEventUtil.isEndingFlowFileEvent(this);
    }

    @JsonProperty("updatedAttributes")
    private Map<String, String> updatedAttributes;

    @JsonProperty("previousAttributes")
    private Map<String, String> previousAttributes;


    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("attributes")
    private Map<String, String> attributeMap;


    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @JsonProperty("attributes")
    public Map<String, String> getAttributeMap() {
        return attributeMap;
    }

    @JsonProperty("attributes")
    public void setAttributeMap(Map<String, String> attributeMap) {
        this.attributeMap = attributeMap;
    }


    @JsonProperty("updatedAttributes")
    public Map<String, String> getUpdatedAttributes() {
        return updatedAttributes;
    }

    @JsonProperty("updatedAttributes")
    public void setUpdatedAttributes(Map<String, String> updatedAttributes) {
        this.updatedAttributes = updatedAttributes;
    }

    @JsonProperty("previousAttributes")
    public Map<String, String> getPreviousAttributes() {
        return previousAttributes;
    }

    @JsonProperty("previousAttributes")
    public void setPreviousAttributes(Map<String, String> previousAttributes) {
        this.previousAttributes = previousAttributes;
    }

    public boolean hasUpdatedAttributes() {
        return this.updatedAttributes != null && !this.updatedAttributes.isEmpty();
    }

    public String getSourceConnectionIdentifier() {
        return sourceConnectionIdentifier;
    }

    public void setSourceConnectionIdentifier(String sourceConnectionIdentifier) {
        this.sourceConnectionIdentifier = sourceConnectionIdentifier;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }


    public Long getEventDuration() {
        return eventDuration;
    }

    public void setEventDuration(Long eventDuration) {
        this.eventDuration = eventDuration;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getFlowFileUuid() {
        return flowFileUuid;
    }

    public void setFlowFileUuid(String flowFileUuid) {
        this.flowFileUuid = flowFileUuid;
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public Collection<ProvenanceEventAttributeDTO> getAttributes() {
        if (attributes == null) {
            attributes = new ArrayList<>();
        }
        return attributes;
    }

    public void setAttributes(Collection<ProvenanceEventAttributeDTO> attributes) {
        this.attributes = attributes;
    }


    public List<String> getParentUuids() {
        return parentUuids;
    }

    public void setParentUuids(List<String> parentUuids) {
        this.parentUuids = parentUuids;
    }

    public List<String> getChildUuids() {
        return childUuids;
    }

    public void setChildUuids(List<String> childUuids) {
        this.childUuids = childUuids;
    }

    public void addChildUuid(String childFlowFileId) {
        if (childUuids == null) {
            childUuids = new ArrayList<>();
        }
        if (!childUuids.contains(childFlowFileId)) {
            childUuids.add(childFlowFileId);
        }
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean isStartOfCurrentFlowFile() {
        Integer index = flowFile.getCompletedEvents().indexOf(this);
        return index == 0;
    }

    public ProvenanceEventRecordDTO getPreviousEvent() {
        return previousEvent;
    }

    public void setPreviousEvent(ProvenanceEventRecordDTO previousEvent) {
        this.previousEventId = previousEvent.getEventId();
        this.previousEvent = previousEvent;
        this.previousEventTime = previousEvent.getEventTime();
        this.previousFlowfileId = previousEvent.getFlowFileUuid();
    }

    public Long getPreviousEventId() {
        if (previousEventId == null && previousEvent != null) {
            this.previousEventId = previousEvent.getEventId();
        }
        return previousEventId;
    }

    public DateTime getPreviousEventTime() {
        if (previousEventTime == null && previousEvent != null) {
            this.previousEventTime = previousEvent.getEventTime();
        }
        return previousEventTime;
    }

    public String getPreviousFlowfileId() {
        if (previousFlowfileId == null && previousEvent != null) {
            this.previousFlowfileId = previousEvent.getFlowFileUuid();
        }
        return previousFlowfileId;
    }

    public DateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(DateTime eventTime) {
        this.eventTime = eventTime;
    }

    public Set<String> getParentFlowFileIds() {
        if (getParentUuids() != null) {
            return new HashSet<>(this.getParentUuids());
        }
        return new HashSet<>();
    }

    public void setParentFlowFileIds(Set<String> parentFlowFileIds) {
        if (parentFlowFileIds != null) {
            this.setParentUuids(new ArrayList<>(parentFlowFileIds));
        } else {
            this.setParentUuids(null);
        }
    }


    public Long getInputContentClaimFileSizeBytes() {
        return inputContentClaimFileSizeBytes;
    }

    public void setInputContentClaimFileSizeBytes(Long inputContentClaimFileSizeBytes) {
        this.inputContentClaimFileSizeBytes = inputContentClaimFileSizeBytes;
    }

    public String getInputContentClaimFileSize() {
        return inputContentClaimFileSize;
    }

    public void setInputContentClaimFileSize(String inputContentClaimFileSize) {
        this.inputContentClaimFileSize = inputContentClaimFileSize;
    }

    public Long getOutputContentClaimFileSizeBytes() {
        return outputContentClaimFileSizeBytes;
    }

    public void setOutputContentClaimFileSizeBytes(Long outputContentClaimFileSizeBytes) {
        this.outputContentClaimFileSizeBytes = outputContentClaimFileSizeBytes;
    }

    public String getOutputContentClaimFileSize() {
        return outputContentClaimFileSize;
    }

    public void setOutputContentClaimFileSize(String outputContentClaimFileSize) {
        this.outputContentClaimFileSize = outputContentClaimFileSize;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }


    public AtomicBoolean getProcessed() {
        return processed;
    }

    public String getFeedProcessGroupId() {
        return feedProcessGroupId;
    }

    public void setFeedProcessGroupId(String feedProcessGroupId) {
        this.feedProcessGroupId = feedProcessGroupId;
    }


    public String getJobFlowFileId() {
        return jobFlowFileId;
    }

    public void setJobFlowFileId(String jobFlowFileId) {
        this.jobFlowFileId = jobFlowFileId;
    }


    public Long getJobEventId() {
        return jobEventId;
    }

    public void setJobEventId(Long jobEventId) {
        this.jobEventId = jobEventId;
    }

    public boolean isStartOfJob() {
        return isStartOfJob;
    }

    public void setIsStartOfJob(boolean isStartOfJob) {
        this.isStartOfJob = isStartOfJob;
    }

    public void setIsFailure(boolean isFailure) {
        this.isFailure = isFailure;
    }

    public boolean isFailure() {
        return isFailure;
    }


    public boolean isEndOfJob() {
        return isEndOfJob;
    }

    public void setIsEndOfJob(boolean isEndOfJob) {
        this.isEndOfJob = isEndOfJob;
    }


    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    private void addRelatedRootFlowFile(String rootFlowFileId){

    }

    public Set<String> getRelatedRootFlowFiles() {
        return relatedRootFlowFiles;
    }

    public void setRelatedRootFlowFiles(Set<String> relatedRootFlowFiles) {
        this.relatedRootFlowFiles = relatedRootFlowFiles;
    }


    public boolean isFinalJobEvent() {
        return isFinalJobEvent;
    }

    public void setIsFinalJobEvent(boolean isFinalJobEvent) {
        this.isFinalJobEvent = isFinalJobEvent;
        if (this.isFinalJobEvent) {
            this.hasFailedEvents = getFlowFile().getRootFlowFile().hasFailedEvents();
        }
    }

    public boolean isHasFailedEvents() {
        return hasFailedEvents;
    }

    public void setHasFailedEvents(boolean hasFailedEvents) {
        this.hasFailedEvents = hasFailedEvents;
    }

    public boolean isBatchJob() {
        return isBatchJob;
    }

    public void setIsBatchJob(boolean isBatchJob) {
        this.isBatchJob = isBatchJob;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProvenanceEventRecordDTO that = (ProvenanceEventRecordDTO) o;

        if (eventId != null ? !eventId.equals(that.eventId) : that.eventId != null) {
            return false;
        }
        return !(flowFileUuid != null ? !flowFileUuid.equals(that.flowFileUuid) : that.flowFileUuid != null);

    }

    @Override
    public int hashCode() {
        int result = eventId != null ? eventId.hashCode() : 0;
        result = 31 * result + (flowFileUuid != null ? flowFileUuid.hashCode() : 0);
        return result;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("ProvenanceEventRecordDTO{");
        sb.append("eventId=").append(getEventId());
        sb.append(", processorName=").append(getComponentName());
        sb.append(", componentId=").append(getComponentId());
        sb.append(", flowFile=").append(getFlowFileUuid());
        sb.append(", previous=").append(getPreviousEventId());
        sb.append(", eventType=").append(getEventType());
        sb.append(", eventDetails=").append(getDetails());
        sb.append(", isEndOfJob=").append(isEndOfJob());
        sb.append('}');
        return sb.toString();
    }


}
