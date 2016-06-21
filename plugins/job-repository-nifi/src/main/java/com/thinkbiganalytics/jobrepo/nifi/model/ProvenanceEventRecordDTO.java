package com.thinkbiganalytics.jobrepo.nifi.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import org.apache.nifi.web.api.dto.provenance.AttributeDTO;
import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by sr186054 on 2/24/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProvenanceEventRecordDTO implements RunStatus, Serializable {

    @JsonProperty("nifiEventId")
    private Long nifiEventId;

    private String id;
    private Long eventId;
    private Date eventTime;
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
    private Collection<ProvenanceEventAttributeDTO> attributes;
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


    public ProvenanceEventRecordDTO() {
        runStatusContext = new RunStatusContext();
    }

    public ProvenanceEventRecordDTO(ProvenanceEventRecordDTO other) {
        this(other.getEventId(), other);
        this.nifiEventId = other.nifiEventId;
        this.runStatusContext = other.runStatusContext;
        this.flowFileComponent = other.flowFileComponent;
        this.flowFile = other.flowFile;
        this.updatedAttributes = other.updatedAttributes;
        this.previousAttributes = other.previousAttributes;
        this.additionalProperties = other.additionalProperties;
        this.attributeMap = other.attributeMap;
    }

    private boolean hasChanged(ProvenanceEventAttributeDTO attributeDTO) {
        boolean changed = false;
        if ((attributeDTO.getPreviousValue() == null && attributeDTO.getValue() != null)
            || (attributeDTO.getValue() == null && attributeDTO.getPreviousValue() != null)
            || (attributeDTO.getPreviousValue() != null && attributeDTO.getValue() != null && !attributeDTO.getPreviousValue().equalsIgnoreCase(attributeDTO.getValue()))) {
            changed = true;
        }
        return changed;
    }

    public ProvenanceEventRecordDTO(Long tbEventId, ProvenanceEventRecordDTO other) {
        this();
        this.setId(other.getId());
        this.setEventId(tbEventId);
        this.setNifiEventId(other.getEventId());
        this.setEventTime(other.getEventTime());
        this.setEventDuration(other.getEventDuration());
        this.setLineageDuration(other.getLineageDuration());
        this.setEventType(other.getEventType());
        this.setFlowFileUuid(other.getFlowFileUuid());
        this.setFileSize(other.getFileSize());
        this.setFileSizeBytes(other.getFileSizeBytes());
        this.setClusterNodeId(other.getClusterNodeId());
        this.setClusterNodeAddress(other.getClusterNodeAddress());
        this.setGroupId(other.getGroupId());
        this.setComponentId(other.getComponentId());
        this.setComponentType(other.getComponentType());
        this.setComponentName(other.getComponentName());
        this.setSourceSystemFlowFileId(other.getSourceSystemFlowFileId());
        this.setAlternateIdentifierUri(other.getAlternateIdentifierUri());
        this.setAttributes(other.getAttributes());
        this.attributeMap = new HashMap<>();
        this.updatedAttributes = new HashMap<>();
        this.previousAttributes = new HashMap<>();
        if (other.getAttributes() != null) {
            for (ProvenanceEventAttributeDTO attributeDTO : other.getAttributes()) {
                attributeMap.put(attributeDTO.getName(), attributeDTO.getValue());
                previousAttributes.put(attributeDTO.getName(), attributeDTO.getPreviousValue());
                if (hasChanged(attributeDTO)) {
                    updatedAttributes.put(attributeDTO.getName(), attributeDTO.getValue());
                }
            }
        }
        this.setParentUuids(other.getParentUuids());
        this.setChildUuids(other.getChildUuids());
        this.setTransitUri(other.getTransitUri());
        this.setRelationship(other.getRelationship());
        this.setDetails(other.getDetails());
        this.setContentEqual(other.getContentEqual());
        this.setInputContentAvailable(other.getInputContentAvailable());
        this.setInputContentClaimSection(other.getInputContentClaimSection());
        this.setInputContentClaimContainer(other.getInputContentClaimContainer());
        this.setInputContentClaimIdentifier(other.getInputContentClaimIdentifier());
        this.setInputContentClaimOffset(other.getInputContentClaimOffset());
        this.setInputContentClaimFileSize(other.getInputContentClaimFileSize());
        this.setInputContentClaimFileSizeBytes(other.getInputContentClaimFileSizeBytes());
        this.setOutputContentAvailable(other.getOutputContentAvailable());
        this.setOutputContentClaimSection(other.getOutputContentClaimSection());
        this.setOutputContentClaimContainer(other.getOutputContentClaimContainer());
        this.setOutputContentClaimIdentifier(other.getOutputContentClaimIdentifier());
        this.setOutputContentClaimOffset(other.getOutputContentClaimOffset());
        this.setOutputContentClaimFileSize(other.getOutputContentClaimFileSize());
        this.setOutputContentClaimFileSizeBytes(other.getOutputContentClaimFileSizeBytes());
        this.setReplayAvailable(other.getReplayAvailable());
        this.setReplayExplanation(other.getReplayExplanation());
        this.setSourceConnectionIdentifier(other.getSourceConnectionIdentifier());
    }

    public ProvenanceEventRecordDTO(Long tbEventId, ProvenanceEventDTO other) {
        this();
        this.setId(other.getId());
        this.setEventId(tbEventId);
        this.setNifiEventId(other.getEventId());
        this.setEventTime(other.getEventTime());
        this.setEventDuration(other.getEventDuration());
        this.setLineageDuration(other.getLineageDuration());
        this.setEventType(other.getEventType());
        this.setFlowFileUuid(other.getFlowFileUuid());
        this.setFileSize(other.getFileSize());
        this.setFileSizeBytes(other.getFileSizeBytes());
        this.setClusterNodeId(other.getClusterNodeId());
        this.setClusterNodeAddress(other.getClusterNodeAddress());
        this.setGroupId(other.getGroupId());
        this.setComponentId(other.getComponentId());
        this.setComponentType(other.getComponentType());
        this.setComponentName(other.getComponentName());
        this.setSourceSystemFlowFileId(other.getSourceSystemFlowFileId());
        this.setAlternateIdentifierUri(other.getAlternateIdentifierUri());
        List<ProvenanceEventAttributeDTO> attrs = getAttributesFromNifi(other.getAttributes());
        this.setAttributes(attrs);
        this.attributeMap = new HashMap<>();
        this.updatedAttributes = new HashMap<>();
        this.previousAttributes = new HashMap<>();
        if (other.getAttributes() != null) {
            for (ProvenanceEventAttributeDTO attributeDTO : attrs) {
                attributeMap.put(attributeDTO.getName(), attributeDTO.getValue());
                previousAttributes.put(attributeDTO.getName(), attributeDTO.getPreviousValue());
                if (hasChanged(attributeDTO)) {
                    updatedAttributes.put(attributeDTO.getName(), attributeDTO.getValue());
                }
            }
        }
        this.setParentUuids(other.getParentUuids());
        this.setChildUuids(other.getChildUuids());
        this.setTransitUri(other.getTransitUri());
        this.setRelationship(other.getRelationship());
        this.setDetails(other.getDetails());
        this.setContentEqual(other.getContentEqual());
        this.setInputContentAvailable(other.getInputContentAvailable());
        this.setInputContentClaimSection(other.getInputContentClaimSection());
        this.setInputContentClaimContainer(other.getInputContentClaimContainer());
        this.setInputContentClaimIdentifier(other.getInputContentClaimIdentifier());
        this.setInputContentClaimOffset(other.getInputContentClaimOffset());
        this.setInputContentClaimFileSize(other.getInputContentClaimFileSize());
        this.setInputContentClaimFileSizeBytes(other.getInputContentClaimFileSizeBytes());
        this.setOutputContentAvailable(other.getOutputContentAvailable());
        this.setOutputContentClaimSection(other.getOutputContentClaimSection());
        this.setOutputContentClaimContainer(other.getOutputContentClaimContainer());
        this.setOutputContentClaimIdentifier(other.getOutputContentClaimIdentifier());
        this.setOutputContentClaimOffset(other.getOutputContentClaimOffset());
        this.setOutputContentClaimFileSize(other.getOutputContentClaimFileSize());
        this.setOutputContentClaimFileSizeBytes(other.getOutputContentClaimFileSizeBytes());
        this.setReplayAvailable(other.getReplayAvailable());
        this.setReplayExplanation(other.getReplayExplanation());
        this.setSourceConnectionIdentifier(other.getSourceConnectionIdentifier());
    }

    private RunStatusContext runStatusContext;

    FlowFileComponent flowFileComponent;


    @JsonIgnore
    private FlowFileEvents flowFile;

    @JsonProperty("updatedAttributes")
    private Map<String, Object> updatedAttributes;

    @JsonProperty("previousAttributes")
    private Map<String, Object> previousAttributes;


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
    public Map<String, Object> getUpdatedAttributes() {
        return updatedAttributes;
    }

    @JsonProperty("updatedAttributes")
    public void setUpdatedAttributes(Map<String, Object> updatedAttributes) {
        this.updatedAttributes = updatedAttributes;
    }

    @JsonProperty("previousAttributes")
    public Map<String, Object> getPreviousAttributes() {
        return previousAttributes;
    }

    @JsonProperty("previousAttributes")
    public void setPreviousAttributes(Map<String, Object> previousAttributes) {
        this.previousAttributes = previousAttributes;
    }

    public boolean hasUpdatedAttributes() {
        return this.updatedAttributes != null && !this.updatedAttributes.isEmpty();
    }

    @Override
    public boolean markRunning() {
        return runStatusContext.markRunning();
    }

    @Override
    public boolean markRunning(DateTime dateTime) {
        return runStatusContext.markRunning(dateTime);
    }

    @Override
    public boolean markCompleted(DateTime dateTime) {
        return runStatusContext.markCompleted(dateTime);
    }

    @Override
    public boolean markFailed(DateTime dateTime) {
        return runStatusContext.markFailed(dateTime);
    }

    @Override
    public boolean markCompleted() {
        return runStatusContext.markCompleted();
    }

    @Override
    public boolean markFailed() {
        return runStatusContext.markFailed();
    }

    @Override
    public boolean isRunning() {
        return runStatusContext.isRunning();
    }

    @Override
    public boolean isComplete() {
        return runStatusContext.isComplete();
    }

    public boolean isInitial() {
        return runStatusContext.isInitial();
    }

    @Override
    public RunStatusContext.RUN_STATUS getRunStatus() {
        return runStatusContext.getRunStatus();
    }

    @Override
    public Date getStartTime() {
        return runStatusContext.getStartTime();
    }

    @Override
    public Date getEndTime() {
        return runStatusContext.getEndTime();
    }

    @Override
    public Date getUTCStartTime() {
        return runStatusContext.getUTCStartTime();
    }

    @Override
    public Date getUTCEndTime() {
        return runStatusContext.getUTCEndTime();
    }

    @JsonIgnore
    public boolean isDropEvent() {
        return "DROP".equalsIgnoreCase(getEventType());
    }

    public Long getNifiEventId() {
        return nifiEventId;
    }

    public void setNifiEventId(Long nifiEventId) {
        this.nifiEventId = nifiEventId;
    }

    @JsonIgnore
    public FlowFileComponent getFlowFileComponent() {
        return flowFileComponent;
    }

    @JsonIgnore
    public void setFlowFileComponent(FlowFileComponent flowFileComponent) {
        this.flowFileComponent = flowFileComponent;
        flowFileComponent.addEvent(this);
    }

    public void setRunStatusContext(RunStatusContext runStatusContext) {
        this.runStatusContext = runStatusContext;
    }

    public RunStatusContext getRunStatusContext() {
        return runStatusContext;
    }

    @JsonIgnore
    public FlowFileEvents getFlowFile() {
        return flowFile;
    }

    @JsonIgnore
    public void setFlowFile(FlowFileEvents flowFile) {
        this.flowFile = flowFile;
    }

    public boolean hasJobExecution() {
        if (this.getFlowFile().getRoot().getFirstEvent() != null) {
            return this.getFlowFile().getRoot().getFirstEvent().getFlowFileComponent().getJobExecution() != null;
        }
        return false;
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

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
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

    public List<ProvenanceEventAttributeDTO> getAttributesFromNifi(Collection<AttributeDTO> attributes) {
        List<ProvenanceEventAttributeDTO> attrs = new ArrayList<>();
        if (attributes != null && !attributes.isEmpty()) {
            for (AttributeDTO attributeDTO : attributes) {
                ProvenanceEventAttributeDTO provenanceEventAttributeDTO = new ProvenanceEventAttributeDTO();
                provenanceEventAttributeDTO.setName(attributeDTO.getName());
                provenanceEventAttributeDTO.setPreviousValue(attributeDTO.getPreviousValue());
                provenanceEventAttributeDTO.setValue(attributeDTO.getValue());
                attrs.add(provenanceEventAttributeDTO);
            }
        }
        return attrs;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("eventId", this.getEventId() == null ? nifiEventId : this.getEventId())
            .add("flowfile", this.getFlowFileUuid())
            .add("eventTime", this.getEventTime())
            .add("eventType", this.getEventType())
            .add("parents", this.getFlowFile() != null ? this.getFlowFile().getParents().size() : null)
            .toString();
    }
}
