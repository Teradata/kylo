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

    private boolean isStartOfJob;

    private ProvenanceEventRecordDTO previousEvent;
    private Long previousEventId;

    private String id;
    private Long eventId;
    private DateTime eventTime;
    private Long eventDuration;
    private Long lineageDuration;
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
    private String sourceSystemFlowFileId;
    private String alternateIdentifierUri;
    private transient Collection<ProvenanceEventAttributeDTO> attributes;
    private List<String> parentUuids;
    private List<String> childUuids;
    private String transitUri;
    private String relationship;
    private String details;
    private Boolean contentEqual;
    private Boolean inputContentAvailable;
    private String inputContentClaimSection;
    private String inputContentClaimContainer;
    private String inputContentClaimIdentifier;
    private Long inputContentClaimOffset;
    private String inputContentClaimFileSize;
    private Long inputContentClaimFileSizeBytes;
    private Boolean outputContentAvailable;
    private String outputContentClaimSection;
    private String outputContentClaimContainer;
    private String outputContentClaimIdentifier;
    private Long outputContentClaimOffset;
    private String outputContentClaimFileSize;
    private Long outputContentClaimFileSizeBytes;
    private Boolean replayAvailable;
    private String replayExplanation;
    private String sourceConnectionIdentifier;


    private boolean stream;

    /**
     * The flow file id the corresponds to the parent /starting event
     */
    private String jobFlowFileId;

    /**
     * The Id that corresponds to the first event that started the job
     */
    private Long jobEventId;

    //flow information

    private String feedName;
    private String feedProcessGroupId;

    private String processorName;

    private ProvenanceEventRecordDTO realFailureEvent;


    private transient ActiveFlowFile flowFile;

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

    public boolean isFailure() {
        return ProvenanceEventUtil.isFailure(this);
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

    public Long getLineageDuration() {
        return lineageDuration;
    }

    public void setLineageDuration(Long lineageDuration) {
        this.lineageDuration = lineageDuration;
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

    public String getSourceSystemFlowFileId() {
        return sourceSystemFlowFileId;
    }

    public void setSourceSystemFlowFileId(String sourceSystemFlowFileId) {
        this.sourceSystemFlowFileId = sourceSystemFlowFileId;
    }

    public String getAlternateIdentifierUri() {
        return alternateIdentifierUri;
    }

    public void setAlternateIdentifierUri(String alternateIdentifierUri) {
        this.alternateIdentifierUri = alternateIdentifierUri;
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

    public String getTransitUri() {
        return transitUri;
    }

    public void setTransitUri(String transitUri) {
        this.transitUri = transitUri;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Boolean getContentEqual() {
        return contentEqual;
    }

    public void setContentEqual(Boolean contentEqual) {
        this.contentEqual = contentEqual;
    }

    public Boolean getInputContentAvailable() {
        return inputContentAvailable;
    }

    public void setInputContentAvailable(Boolean inputContentAvailable) {
        this.inputContentAvailable = inputContentAvailable;
    }

    public String getInputContentClaimSection() {
        return inputContentClaimSection;
    }

    public void setInputContentClaimSection(String inputContentClaimSection) {
        this.inputContentClaimSection = inputContentClaimSection;
    }

    public String getInputContentClaimContainer() {
        return inputContentClaimContainer;
    }

    public void setInputContentClaimContainer(String inputContentClaimContainer) {
        this.inputContentClaimContainer = inputContentClaimContainer;
    }

    public String getInputContentClaimIdentifier() {
        return inputContentClaimIdentifier;
    }

    public void setInputContentClaimIdentifier(String inputContentClaimIdentifier) {
        this.inputContentClaimIdentifier = inputContentClaimIdentifier;
    }

    public Long getInputContentClaimOffset() {
        return inputContentClaimOffset;
    }

    public void setInputContentClaimOffset(Long inputContentClaimOffset) {
        this.inputContentClaimOffset = inputContentClaimOffset;
    }

    public String getInputContentClaimFileSize() {
        return inputContentClaimFileSize;
    }

    public void setInputContentClaimFileSize(String inputContentClaimFileSize) {
        this.inputContentClaimFileSize = inputContentClaimFileSize;
    }

    public Long getInputContentClaimFileSizeBytes() {
        return inputContentClaimFileSizeBytes;
    }

    public void setInputContentClaimFileSizeBytes(Long inputContentClaimFileSizeBytes) {
        this.inputContentClaimFileSizeBytes = inputContentClaimFileSizeBytes;
    }

    public Boolean getOutputContentAvailable() {
        return outputContentAvailable;
    }

    public void setOutputContentAvailable(Boolean outputContentAvailable) {
        this.outputContentAvailable = outputContentAvailable;
    }

    public String getOutputContentClaimSection() {
        return outputContentClaimSection;
    }

    public void setOutputContentClaimSection(String outputContentClaimSection) {
        this.outputContentClaimSection = outputContentClaimSection;
    }

    public String getOutputContentClaimContainer() {
        return outputContentClaimContainer;
    }

    public void setOutputContentClaimContainer(String outputContentClaimContainer) {
        this.outputContentClaimContainer = outputContentClaimContainer;
    }

    public String getOutputContentClaimIdentifier() {
        return outputContentClaimIdentifier;
    }

    public void setOutputContentClaimIdentifier(String outputContentClaimIdentifier) {
        this.outputContentClaimIdentifier = outputContentClaimIdentifier;
    }

    public Long getOutputContentClaimOffset() {
        return outputContentClaimOffset;
    }

    public void setOutputContentClaimOffset(Long outputContentClaimOffset) {
        this.outputContentClaimOffset = outputContentClaimOffset;
    }

    public String getOutputContentClaimFileSize() {
        return outputContentClaimFileSize;
    }

    public void setOutputContentClaimFileSize(String outputContentClaimFileSize) {
        this.outputContentClaimFileSize = outputContentClaimFileSize;
    }

    public Long getOutputContentClaimFileSizeBytes() {
        return outputContentClaimFileSizeBytes;
    }

    public void setOutputContentClaimFileSizeBytes(Long outputContentClaimFileSizeBytes) {
        this.outputContentClaimFileSizeBytes = outputContentClaimFileSizeBytes;
    }

    public Boolean getReplayAvailable() {
        return replayAvailable;
    }

    public void setReplayAvailable(Boolean replayAvailable) {
        this.replayAvailable = replayAvailable;
    }

    public String getReplayExplanation() {
        return replayExplanation;
    }

    public void setReplayExplanation(String replayExplanation) {
        this.replayExplanation = replayExplanation;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
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
    }

    public Long getPreviousEventId() {
        if (previousEventId == null && previousEvent != null) {
            return previousEvent.getEventId();
        }
        return previousEventId;
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

    public ProvenanceEventRecordDTO getRealFailureEvent() {
        return realFailureEvent;
    }

    public void setRealFailureEvent(ProvenanceEventRecordDTO realFailureEvent) {
        this.realFailureEvent = realFailureEvent;
    }

    public boolean isStartOfJob() {
        return isStartOfJob;
    }

    public void setIsStartOfJob(boolean isStartOfJob) {
        this.isStartOfJob = isStartOfJob;
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
        sb.append("processorName=").append(getProcessorName());
        sb.append("componentId=").append(getComponentId());
        sb.append(", flowFile=").append(getFlowFileUuid()).append("(").append(flowFile != null).append(")");
        sb.append(", parentUUIDs=").append(getParentUuids() != null ? getParentUuids().size() : "NULL");
        sb.append(",previous=").append(getPreviousEventId());
        sb.append(", eventComponentId=").append(getComponentId());
        sb.append(", eventType=").append(getEventType());
        sb.append(", eventDetails=").append(getDetails());
        sb.append(", eventDuration=").append(getEventDuration());
        sb.append(", eventTime=").append(getEventTime());
        sb.append(", eventRelationship=").append(getRelationship());
        sb.append(", sourceQueueId=").append(getSourceConnectionIdentifier());
        sb.append(", sourceFlowFileId=").append(getSourceSystemFlowFileId());
        sb.append('}');
        return sb.toString();
    }



}
