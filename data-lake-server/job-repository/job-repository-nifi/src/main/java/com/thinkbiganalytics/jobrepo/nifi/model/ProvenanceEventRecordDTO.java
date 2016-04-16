package com.thinkbiganalytics.jobrepo.nifi.model;

import com.fasterxml.jackson.annotation.*;
import com.google.common.base.MoreObjects;
import org.apache.nifi.web.api.dto.provenance.AttributeDTO;
import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by sr186054 on 2/24/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProvenanceEventRecordDTO extends ProvenanceEventDTO  implements RunStatus{

    @JsonProperty("nifiEventId")
    private Long nifiEventId;

    public ProvenanceEventRecordDTO(){
        runStatusContext = new RunStatusContext();
    }

    public ProvenanceEventRecordDTO(ProvenanceEventRecordDTO other) {
        this((ProvenanceEventDTO)other);
        this.nifiEventId = other.nifiEventId;
        this.runStatusContext = other.runStatusContext;
        this.flowFileComponent = other.flowFileComponent;
        this.flowFile = other.flowFile;
        this.updatedAttributes = other.updatedAttributes;
        this.previousAttributes = other.previousAttributes;
        this.additionalProperties = other.additionalProperties;
        this.attributeMap = other.attributeMap;
    }

    private boolean hasChanged(AttributeDTO attributeDTO){
        boolean changed = false;
        if( (attributeDTO.getPreviousValue() == null && attributeDTO.getValue() != null)
                || (attributeDTO.getValue() == null && attributeDTO.getPreviousValue() != null)
            || (attributeDTO.getPreviousValue() != null && attributeDTO.getValue() != null && !attributeDTO.getPreviousValue().equalsIgnoreCase(attributeDTO.getValue()))) {
            changed = true;
        }
        return changed;
    }
    public ProvenanceEventRecordDTO(ProvenanceEventDTO other){
        this();
    this.setId(other.getId());
    this.setEventId(other.getEventId());
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
        if(other.getAttributes() != null) {
            for(AttributeDTO attributeDTO: other.getAttributes()){
                attributeMap.put(attributeDTO.getName(),attributeDTO.getValue());
                previousAttributes.put(attributeDTO.getName(),attributeDTO.getPreviousValue());
                if(hasChanged(attributeDTO)){
                    updatedAttributes.put(attributeDTO.getName(),attributeDTO.getValue());
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
    private Map<String,Object>updatedAttributes;

    @JsonProperty("previousAttributes")
    private Map<String,Object>previousAttributes;


    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("attributes")
    private Map<String,String> attributeMap;



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

    public boolean hasUpdatedAttributes(){
        return this.updatedAttributes != null && !this.updatedAttributes.isEmpty();
    }

    @Override
    public boolean markRunning() {
       return  runStatusContext.markRunning();
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

    public boolean isInitial(){
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
    public boolean isDropEvent(){
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

    public boolean hasJobExecution(){
        if(this.getFlowFile().getRoot().getFirstEvent() != null) {
            return this.getFlowFile().getRoot().getFirstEvent().getFlowFileComponent().getJobExecution() != null;
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("eventId",this.getEventId())
                .add("nifiEventId", nifiEventId)
        .add("eventTime",this.getEventTime())
                .add("eventType",this.getEventType())
                .toString();
    }
}
