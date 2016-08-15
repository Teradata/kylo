package com.thinkbiganalytics.nifi.provenance.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

import org.apache.nifi.web.api.dto.provenance.AttributeDTO;
import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;
import org.apache.nifi.web.api.dto.util.TimestampAdapter;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by sr186054 on 8/13/16.
 */
public class ProvenanceEventRecordDTO extends ProvenanceEventDTO {

    private ProvenanceEventDTO dto;

    private ActiveFlowFile flowFile;

    public ProvenanceEventRecordDTO(ProvenanceEventDTO dto, ActiveFlowFile flowFile) {
        this.dto = dto;
        this.flowFile = flowFile;
    }

    public ActiveFlowFile getFlowFile() {
        return flowFile;
    }

    public void setFlowFile(ActiveFlowFile flowFile) {
        this.flowFile = flowFile;
    }

    @Override
    @ApiModelProperty("The event uuid.")
    public String getId() {
        return dto.getId();
    }

    @Override
    public void setId(String id) {
        dto.setId(id);
    }

    @Override
    @ApiModelProperty("The event id. This is a one up number thats unique per node.")
    public Long getEventId() {
        return dto.getEventId();
    }

    @Override
    public void setEventId(Long eventId) {
        dto.setEventId(eventId);
    }

    @Override
    @XmlJavaTypeAdapter(TimestampAdapter.class)
    @ApiModelProperty("The timestamp of the event.")
    public Date getEventTime() {
        return dto.getEventTime();
    }

    @Override
    public void setEventTime(Date eventTime) {
        dto.setEventTime(eventTime);
    }

    @Override
    @ApiModelProperty("The uuid of the flowfile for the event.")
    public String getFlowFileUuid() {
        return dto.getFlowFileUuid();
    }

    @Override
    public void setFlowFileUuid(String flowFileUuid) {
        dto.setFlowFileUuid(flowFileUuid);
    }

    @Override
    @ApiModelProperty("The size of the flowfile for the event.")
    public String getFileSize() {
        return dto.getFileSize();
    }

    @Override
    public void setFileSize(String fileSize) {
        dto.setFileSize(fileSize);
    }

    @Override
    @ApiModelProperty("The size of the flowfile in bytes for the event.")
    public Long getFileSizeBytes() {
        return dto.getFileSizeBytes();
    }

    @Override
    public void setFileSizeBytes(Long fileSizeBytes) {
        dto.setFileSizeBytes(fileSizeBytes);
    }

    @Override
    @ApiModelProperty("The type of the event.")
    public String getEventType() {
        return dto.getEventType();
    }

    @Override
    public void setEventType(String eventType) {
        dto.setEventType(eventType);
    }

    @Override
    @ApiModelProperty("The attributes of the flowfile for the event.")
    public Collection<AttributeDTO> getAttributes() {
        return dto.getAttributes();
    }

    @Override
    public void setAttributes(Collection<AttributeDTO> attributes) {
        dto.setAttributes(attributes);
    }

    @Override
    @ApiModelProperty("The id of the group that the component resides in. If the component is no longer in the flow, the group id will not be set.")
    public String getGroupId() {
        return dto.getGroupId();
    }

    @Override
    public void setGroupId(String groupId) {
        dto.setGroupId(groupId);
    }

    @Override
    @ApiModelProperty("The id of the component that generated the event.")
    public String getComponentId() {
        return dto.getComponentId();
    }

    @Override
    public void setComponentId(String componentId) {
        dto.setComponentId(componentId);
    }

    @Override
    @ApiModelProperty("The name of the component that generated the event.")
    public String getComponentName() {
        return dto.getComponentName();
    }

    @Override
    public void setComponentName(String componentName) {
        dto.setComponentName(componentName);
    }

    @Override
    @ApiModelProperty("The type of the component that generated the event.")
    public String getComponentType() {
        return dto.getComponentType();
    }

    @Override
    public void setComponentType(String componentType) {
        dto.setComponentType(componentType);
    }

    @Override
    @ApiModelProperty("The source/destination system uri if the event was a RECEIVE/SEND.")
    public String getTransitUri() {
        return dto.getTransitUri();
    }

    @Override
    public void setTransitUri(String transitUri) {
        dto.setTransitUri(transitUri);
    }

    @Override
    @ApiModelProperty("The alternate identifier uri for the fileflow for the event.")
    public String getAlternateIdentifierUri() {
        return dto.getAlternateIdentifierUri();
    }

    @Override
    public void setAlternateIdentifierUri(String alternateIdentifierUri) {
        dto.setAlternateIdentifierUri(alternateIdentifierUri);
    }

    @Override
    @ApiModelProperty("The identifier for the node where the event originated.")
    public String getClusterNodeId() {
        return dto.getClusterNodeId();
    }

    @Override
    public void setClusterNodeId(String clusterNodeId) {
        dto.setClusterNodeId(clusterNodeId);
    }

    @Override
    @ApiModelProperty("The label for the node where the event originated.")
    public String getClusterNodeAddress() {
        return dto.getClusterNodeAddress();
    }

    @Override
    public void setClusterNodeAddress(String clusterNodeAddress) {
        dto.setClusterNodeAddress(clusterNodeAddress);
    }

    @Override
    @ApiModelProperty("The parent uuids for the event.")
    public List<String> getParentUuids() {
        return dto.getParentUuids();
    }

    @Override
    public void setParentUuids(List<String> parentUuids) {
        dto.setParentUuids(parentUuids);
    }

    @Override
    @ApiModelProperty("The child uuids for the event.")
    public List<String> getChildUuids() {
        return dto.getChildUuids();
    }

    @Override
    public void setChildUuids(List<String> childUuids) {
        dto.setChildUuids(childUuids);
    }

    @Override
    @ApiModelProperty("The event duration in milliseconds.")
    public Long getEventDuration() {
        return dto.getEventDuration();
    }

    @Override
    public void setEventDuration(Long eventDuration) {
        dto.setEventDuration(eventDuration);
    }

    @Override
    @ApiModelProperty("The duration since the lineage began, in milliseconds.")
    public Long getLineageDuration() {
        return dto.getLineageDuration();
    }

    @Override
    public void setLineageDuration(Long lineageDuration) {
        dto.setLineageDuration(lineageDuration);
    }

    @Override
    @ApiModelProperty("The source system flowfile id.")
    public String getSourceSystemFlowFileId() {
        return dto.getSourceSystemFlowFileId();
    }

    @Override
    public void setSourceSystemFlowFileId(String sourceSystemFlowFileId) {
        dto.setSourceSystemFlowFileId(sourceSystemFlowFileId);
    }

    @Override
    @ApiModelProperty("The relationship to which the flowfile was routed if the event is of type ROUTE.")
    public String getRelationship() {
        return dto.getRelationship();
    }

    @Override
    public void setRelationship(String relationship) {
        dto.setRelationship(relationship);
    }

    @Override
    @ApiModelProperty("The event details.")
    public String getDetails() {
        return dto.getDetails();
    }

    @Override
    public void setDetails(String details) {
        dto.setDetails(details);
    }

    @Override
    @ApiModelProperty("Whether the input and output content claim is the same.")
    public Boolean getContentEqual() {
        return dto.getContentEqual();
    }

    @Override
    public void setContentEqual(Boolean contentEqual) {
        dto.setContentEqual(contentEqual);
    }

    @Override
    @ApiModelProperty("Whether the output content is still available.")
    public Boolean getOutputContentAvailable() {
        return dto.getOutputContentAvailable();
    }

    @Override
    public void setOutputContentAvailable(Boolean outputContentAvailable) {
        dto.setOutputContentAvailable(outputContentAvailable);
    }

    @Override
    @ApiModelProperty("The section in which the output content claim lives.")
    public String getOutputContentClaimSection() {
        return dto.getOutputContentClaimSection();
    }

    @Override
    public void setOutputContentClaimSection(String contentClaimSection) {
        dto.setOutputContentClaimSection(contentClaimSection);
    }

    @Override
    @ApiModelProperty("The container in which the output content claim lives.")
    public String getOutputContentClaimContainer() {
        return dto.getOutputContentClaimContainer();
    }

    @Override
    public void setOutputContentClaimContainer(String outputContentClaimContainer) {
        dto.setOutputContentClaimContainer(outputContentClaimContainer);
    }

    @Override
    @ApiModelProperty("The identifier of the output content claim.")
    public String getOutputContentClaimIdentifier() {
        return dto.getOutputContentClaimIdentifier();
    }

    @Override
    public void setOutputContentClaimIdentifier(String outputContentClaimIdentifier) {
        dto.setOutputContentClaimIdentifier(outputContentClaimIdentifier);
    }

    @Override
    @ApiModelProperty("The offset into the output content claim where the flowfiles content begins.")
    public Long getOutputContentClaimOffset() {
        return dto.getOutputContentClaimOffset();
    }

    @Override
    public void setOutputContentClaimOffset(Long outputContentClaimOffset) {
        dto.setOutputContentClaimOffset(outputContentClaimOffset);
    }

    @Override
    @ApiModelProperty("The file size of the output content claim formatted.")
    public String getOutputContentClaimFileSize() {
        return dto.getOutputContentClaimFileSize();
    }

    @Override
    public void setOutputContentClaimFileSize(String outputContentClaimFileSize) {
        dto.setOutputContentClaimFileSize(outputContentClaimFileSize);
    }

    @Override
    @ApiModelProperty("The file size of the output content claim in bytes.")
    public Long getOutputContentClaimFileSizeBytes() {
        return dto.getOutputContentClaimFileSizeBytes();
    }

    @Override
    public void setOutputContentClaimFileSizeBytes(Long outputContentClaimFileSizeBytes) {
        dto.setOutputContentClaimFileSizeBytes(outputContentClaimFileSizeBytes);
    }

    @Override
    @ApiModelProperty("Whether the input content is still available.")
    public Boolean getInputContentAvailable() {
        return dto.getInputContentAvailable();
    }

    @Override
    public void setInputContentAvailable(Boolean inputContentAvailable) {
        dto.setInputContentAvailable(inputContentAvailable);
    }

    @Override
    @ApiModelProperty("The section in which the input content claim lives.")
    public String getInputContentClaimSection() {
        return dto.getInputContentClaimSection();
    }

    @Override
    public void setInputContentClaimSection(String inputContentClaimSection) {
        dto.setInputContentClaimSection(inputContentClaimSection);
    }

    @Override
    @ApiModelProperty("The container in which the input content claim lives.")
    public String getInputContentClaimContainer() {
        return dto.getInputContentClaimContainer();
    }

    @Override
    public void setInputContentClaimContainer(String inputContentClaimContainer) {
        dto.setInputContentClaimContainer(inputContentClaimContainer);
    }

    @Override
    @ApiModelProperty("The identifier of the input content claim.")
    public String getInputContentClaimIdentifier() {
        return dto.getInputContentClaimIdentifier();
    }

    @Override
    public void setInputContentClaimIdentifier(String inputContentClaimIdentifier) {
        dto.setInputContentClaimIdentifier(inputContentClaimIdentifier);
    }

    @Override
    @ApiModelProperty("The offset into the input content claim where the flowfiles content begins.")
    public Long getInputContentClaimOffset() {
        return dto.getInputContentClaimOffset();
    }

    @Override
    public void setInputContentClaimOffset(Long inputContentClaimOffset) {
        dto.setInputContentClaimOffset(inputContentClaimOffset);
    }

    @Override
    @ApiModelProperty("The file size of the input content claim formatted.")
    public String getInputContentClaimFileSize() {
        return dto.getInputContentClaimFileSize();
    }

    @Override
    public void setInputContentClaimFileSize(String inputContentClaimFileSize) {
        dto.setInputContentClaimFileSize(inputContentClaimFileSize);
    }

    @Override
    @ApiModelProperty("The file size of the intput content claim in bytes.")
    public Long getInputContentClaimFileSizeBytes() {
        return dto.getInputContentClaimFileSizeBytes();
    }

    @Override
    public void setInputContentClaimFileSizeBytes(Long inputContentClaimFileSizeBytes) {
        dto.setInputContentClaimFileSizeBytes(inputContentClaimFileSizeBytes);
    }

    @Override
    @ApiModelProperty("Whether or not replay is available.")
    public Boolean getReplayAvailable() {
        return dto.getReplayAvailable();
    }

    @Override
    public void setReplayAvailable(Boolean replayAvailable) {
        dto.setReplayAvailable(replayAvailable);
    }

    @Override
    @ApiModelProperty("Explanation as to why replay is unavailable.")
    public String getReplayExplanation() {
        return dto.getReplayExplanation();
    }

    @Override
    public void setReplayExplanation(String replayExplanation) {
        dto.setReplayExplanation(replayExplanation);
    }

    @Override
    @ApiModelProperty(
        "The identifier of the queue/connection from which the flowfile was pulled to genereate this event. May be null if the queue/connection is unknown or the flowfile was generated from this event.")
    public String getSourceConnectionIdentifier() {
        return dto.getSourceConnectionIdentifier();
    }

    @Override
    public void setSourceConnectionIdentifier(String sourceConnectionIdentifier) {
        dto.setSourceConnectionIdentifier(sourceConnectionIdentifier);
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
        if (that.dto == null) {
            return false;
        }
        return Objects.equals(getEventId(), that.getEventId()) &&
               Objects.equals(getFlowFileUuid(), that.getFlowFileUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(dto.getEventId(), dto.getFlowFileUuid());
    }
}
