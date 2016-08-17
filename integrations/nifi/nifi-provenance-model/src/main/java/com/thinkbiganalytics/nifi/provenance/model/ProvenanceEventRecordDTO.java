package com.thinkbiganalytics.nifi.provenance.model;

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

    private ProvenanceEventRecordDTO previousEvent;

    private Long duration;

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
    public String getId() {
        return dto.getId();
    }

    @Override
    public void setId(String id) {
        dto.setId(id);
    }

    @Override
    public Long getEventId() {
        return dto.getEventId();
    }

    @Override
    public void setEventId(Long eventId) {
        dto.setEventId(eventId);
    }

    @Override
    @XmlJavaTypeAdapter(TimestampAdapter.class)
    public Date getEventTime() {
        return dto.getEventTime();
    }

    @Override
    public void setEventTime(Date eventTime) {
        dto.setEventTime(eventTime);
    }

    @Override
    public String getFlowFileUuid() {
        return dto.getFlowFileUuid();
    }

    @Override
    public void setFlowFileUuid(String flowFileUuid) {
        dto.setFlowFileUuid(flowFileUuid);
    }

    @Override
    public String getFileSize() {
        return dto.getFileSize();
    }

    @Override
    public void setFileSize(String fileSize) {
        dto.setFileSize(fileSize);
    }

    @Override
    public Long getFileSizeBytes() {
        return dto.getFileSizeBytes();
    }

    @Override
    public void setFileSizeBytes(Long fileSizeBytes) {
        dto.setFileSizeBytes(fileSizeBytes);
    }

    @Override
    public String getEventType() {
        return dto.getEventType();
    }

    @Override
    public void setEventType(String eventType) {
        dto.setEventType(eventType);
    }

    @Override
    public Collection<AttributeDTO> getAttributes() {
        return dto.getAttributes();
    }

    @Override
    public void setAttributes(Collection<AttributeDTO> attributes) {
        dto.setAttributes(attributes);
    }

    @Override
    public String getGroupId() {
        return dto.getGroupId();
    }

    @Override
    public void setGroupId(String groupId) {
        dto.setGroupId(groupId);
    }

    @Override
    public String getComponentId() {
        return dto.getComponentId();
    }

    @Override
    public void setComponentId(String componentId) {
        dto.setComponentId(componentId);
    }

    @Override
    public String getComponentName() {
        return dto.getComponentName();
    }

    @Override
    public void setComponentName(String componentName) {
        dto.setComponentName(componentName);
    }

    @Override
    public String getComponentType() {
        return dto.getComponentType();
    }

    @Override
    public void setComponentType(String componentType) {
        dto.setComponentType(componentType);
    }

    @Override
    public String getTransitUri() {
        return dto.getTransitUri();
    }

    @Override
    public void setTransitUri(String transitUri) {
        dto.setTransitUri(transitUri);
    }

    @Override
    public String getAlternateIdentifierUri() {
        return dto.getAlternateIdentifierUri();
    }

    @Override
    public void setAlternateIdentifierUri(String alternateIdentifierUri) {
        dto.setAlternateIdentifierUri(alternateIdentifierUri);
    }

    @Override
    public String getClusterNodeId() {
        return dto.getClusterNodeId();
    }

    @Override
    public void setClusterNodeId(String clusterNodeId) {
        dto.setClusterNodeId(clusterNodeId);
    }

    @Override
    public String getClusterNodeAddress() {
        return dto.getClusterNodeAddress();
    }

    @Override
    public void setClusterNodeAddress(String clusterNodeAddress) {
        dto.setClusterNodeAddress(clusterNodeAddress);
    }

    @Override
    public List<String> getParentUuids() {
        return dto.getParentUuids();
    }

    @Override
    public void setParentUuids(List<String> parentUuids) {
        dto.setParentUuids(parentUuids);
    }

    @Override
    public List<String> getChildUuids() {
        return dto.getChildUuids();
    }

    @Override
    public void setChildUuids(List<String> childUuids) {
        dto.setChildUuids(childUuids);
    }

    @Override
    public Long getEventDuration() {
        return dto.getEventDuration();
    }

    @Override
    public void setEventDuration(Long eventDuration) {
        dto.setEventDuration(eventDuration);
    }

    @Override
    public Long getLineageDuration() {
        return dto.getLineageDuration();
    }

    @Override
    public void setLineageDuration(Long lineageDuration) {
        dto.setLineageDuration(lineageDuration);
    }

    @Override
    public String getSourceSystemFlowFileId() {
        return dto.getSourceSystemFlowFileId();
    }

    @Override
    public void setSourceSystemFlowFileId(String sourceSystemFlowFileId) {
        dto.setSourceSystemFlowFileId(sourceSystemFlowFileId);
    }

    @Override
    public String getRelationship() {
        return dto.getRelationship();
    }

    @Override
    public void setRelationship(String relationship) {
        dto.setRelationship(relationship);
    }

    @Override
    public String getDetails() {
        return dto.getDetails();
    }

    @Override
    public void setDetails(String details) {
        dto.setDetails(details);
    }

    @Override
    public Boolean getContentEqual() {
        return dto.getContentEqual();
    }

    @Override
    public void setContentEqual(Boolean contentEqual) {
        dto.setContentEqual(contentEqual);
    }

    @Override
    public Boolean getOutputContentAvailable() {
        return dto.getOutputContentAvailable();
    }

    @Override
    public void setOutputContentAvailable(Boolean outputContentAvailable) {
        dto.setOutputContentAvailable(outputContentAvailable);
    }

    @Override
    public String getOutputContentClaimSection() {
        return dto.getOutputContentClaimSection();
    }

    @Override
    public void setOutputContentClaimSection(String contentClaimSection) {
        dto.setOutputContentClaimSection(contentClaimSection);
    }

    @Override
    public String getOutputContentClaimContainer() {
        return dto.getOutputContentClaimContainer();
    }

    @Override
    public void setOutputContentClaimContainer(String outputContentClaimContainer) {
        dto.setOutputContentClaimContainer(outputContentClaimContainer);
    }

    @Override
    public String getOutputContentClaimIdentifier() {
        return dto.getOutputContentClaimIdentifier();
    }

    @Override
    public void setOutputContentClaimIdentifier(String outputContentClaimIdentifier) {
        dto.setOutputContentClaimIdentifier(outputContentClaimIdentifier);
    }

    @Override
    public Long getOutputContentClaimOffset() {
        return dto.getOutputContentClaimOffset();
    }

    @Override
    public void setOutputContentClaimOffset(Long outputContentClaimOffset) {
        dto.setOutputContentClaimOffset(outputContentClaimOffset);
    }

    @Override
    public String getOutputContentClaimFileSize() {
        return dto.getOutputContentClaimFileSize();
    }

    @Override
    public void setOutputContentClaimFileSize(String outputContentClaimFileSize) {
        dto.setOutputContentClaimFileSize(outputContentClaimFileSize);
    }

    @Override
    public Long getOutputContentClaimFileSizeBytes() {
        return dto.getOutputContentClaimFileSizeBytes();
    }

    @Override
    public void setOutputContentClaimFileSizeBytes(Long outputContentClaimFileSizeBytes) {
        dto.setOutputContentClaimFileSizeBytes(outputContentClaimFileSizeBytes);
    }

    @Override
    public Boolean getInputContentAvailable() {
        return dto.getInputContentAvailable();
    }

    @Override
    public void setInputContentAvailable(Boolean inputContentAvailable) {
        dto.setInputContentAvailable(inputContentAvailable);
    }

    @Override
    public String getInputContentClaimSection() {
        return dto.getInputContentClaimSection();
    }

    @Override
    public void setInputContentClaimSection(String inputContentClaimSection) {
        dto.setInputContentClaimSection(inputContentClaimSection);
    }

    @Override
    public String getInputContentClaimContainer() {
        return dto.getInputContentClaimContainer();
    }

    @Override
    public void setInputContentClaimContainer(String inputContentClaimContainer) {
        dto.setInputContentClaimContainer(inputContentClaimContainer);
    }

    @Override
    public String getInputContentClaimIdentifier() {
        return dto.getInputContentClaimIdentifier();
    }

    @Override
    public void setInputContentClaimIdentifier(String inputContentClaimIdentifier) {
        dto.setInputContentClaimIdentifier(inputContentClaimIdentifier);
    }

    @Override
    public Long getInputContentClaimOffset() {
        return dto.getInputContentClaimOffset();
    }

    @Override
    public void setInputContentClaimOffset(Long inputContentClaimOffset) {
        dto.setInputContentClaimOffset(inputContentClaimOffset);
    }

    @Override
    public String getInputContentClaimFileSize() {
        return dto.getInputContentClaimFileSize();
    }

    @Override
    public void setInputContentClaimFileSize(String inputContentClaimFileSize) {
        dto.setInputContentClaimFileSize(inputContentClaimFileSize);
    }

    @Override
    public Long getInputContentClaimFileSizeBytes() {
        return dto.getInputContentClaimFileSizeBytes();
    }

    @Override
    public void setInputContentClaimFileSizeBytes(Long inputContentClaimFileSizeBytes) {
        dto.setInputContentClaimFileSizeBytes(inputContentClaimFileSizeBytes);
    }

    @Override
    public Boolean getReplayAvailable() {
        return dto.getReplayAvailable();
    }

    @Override
    public void setReplayAvailable(Boolean replayAvailable) {
        dto.setReplayAvailable(replayAvailable);
    }

    @Override
    public String getReplayExplanation() {
        return dto.getReplayExplanation();
    }

    @Override
    public void setReplayExplanation(String replayExplanation) {
        dto.setReplayExplanation(replayExplanation);
    }

    @Override
    public String getSourceConnectionIdentifier() {
        return dto.getSourceConnectionIdentifier();
    }

    @Override
    public void setSourceConnectionIdentifier(String sourceConnectionIdentifier) {
        dto.setSourceConnectionIdentifier(sourceConnectionIdentifier);
    }


    public boolean isStartOfCurrentFlowFile() {
        Integer index = flowFile.getCompletedEvents().indexOf(this);
        return index == 0;
    }

    public ProvenanceEventRecordDTO getPreviousEvent() {
        return previousEvent;
    }

    public void setPreviousEvent(ProvenanceEventRecordDTO previousEvent) {
        this.previousEvent = previousEvent;
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

    public String toString() {
        final StringBuilder sb = new StringBuilder("ProvenanceEventRecordDTO{");
        sb.append("eventId=").append(getEventId());
        sb.append(", flowFile=").append(getFlowFileUuid()).append("(").append(flowFile).append(")");
        sb.append(", eventComponentId=").append(getComponentId());
        sb.append(", eventRelationship=").append(getRelationship());
        sb.append(", eventType=").append(getEventType());
        sb.append(", eventDetails=").append(getDetails());
        sb.append(", eventDuration=").append(getEventDuration());
        sb.append(", eventTime=").append(getEventTime());
        sb.append('}');
        return sb.toString();
    }
}
