package com.thinkbiganalytics.jobrepo.nifi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Created by sr186054 on 2/26/16.
 */
public class FlowFileComponent extends RunStatusContext implements Serializable {

    private Long id;
    private String componentId;
    private String componetName;

    private Long stepExecutionId;
    private boolean executionContextSet;
    private Integer version;

    private boolean stepFinished;

    private Set<ProvenanceEventRecordDTO> events;

    private ProvenanceEventRecordDTO firstEvent;

    private NifiJobExecution jobExecution;


    public FlowFileComponent(String componentId) {
        this.componentId = componentId;
    }
    Map<String, Object> executionContextMap = new HashMap<>();

    public FlowFileComponent(RUN_STATUS runStatus, Date startTime, Date endTime, String componentId) {
        super(runStatus, startTime, endTime);
        this.componentId = componentId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponetName() {
        return componetName;
    }

    public void setComponetName(String componetName) {
        this.componetName = componetName;
    }

    public NifiJobExecution getJobExecution() {
        return jobExecution;
    }

    public void setJobExecution(NifiJobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }

    public Long getStepExecutionId() {
        return stepExecutionId;
    }

    public void setStepExecutionId(Long stepExecutionId) {
        this.stepExecutionId = stepExecutionId;
    }

    public void updateJobExecution() {
        if (getFirstEvent().getFlowFile().getRoot().getFirstEvent() != null) {
            setJobExecution(getFirstEvent().getFlowFile().getRoot().getFirstEvent().getFlowFileComponent().getJobExecution());
        }
        if(jobExecution == null &&  getFirstEvent().getFlowFile().getNifiJobExecution() != null){
           setJobExecution( getFirstEvent().getFlowFile().getNifiJobExecution());
            getFirstEvent().getFlowFile().getRoot().getFirstEvent().getFlowFileComponent().setJobExecution(jobExecution);
        }
    }

    public Set<ProvenanceEventRecordDTO> getEvents() {
        if (events == null) {
            events = new HashSet<>();
        }
        return events;
    }

    public void setEvents(Set<ProvenanceEventRecordDTO> events) {
        this.events = events;
    }

    public void addEvent(ProvenanceEventRecordDTO event) {
        if (getEvents().isEmpty()) {
            setFirstEvent(event);
        }
        getEvents().add(event);
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public ProvenanceEventRecordDTO getFirstEvent() {
        return firstEvent;
    }

    public List<ProvenanceEventRecordDTO> getEventsAsList() {
        List<ProvenanceEventRecordDTO> eventList = new ArrayList<>(getEvents());
        Collections.sort(eventList, new ProvenanceEventComparator());
        return eventList;
    }

    public List<Long> getEventIds(){
        List<Long> ids = new ArrayList<>();
        List<ProvenanceEventRecordDTO> events = getEventsAsList();
        for(ProvenanceEventRecordDTO e : events){
            ids.add(e.getEventId());
        }
        return ids;
    }

    public ProvenanceEventRecordDTO getLastEvent() {
        List<ProvenanceEventRecordDTO> eventList = getEventsAsList();
        if (!eventList.isEmpty()) {
            int size = eventList.size();
            return eventList.get((size - 1));
        }
        return null;
    }

    public void setFirstEvent(ProvenanceEventRecordDTO firstEvent) {
        this.firstEvent = firstEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FlowFileComponent that = (FlowFileComponent) o;

        return componentId.equals(that.componentId);

    }

    public boolean isExecutionContextSet() {
        return executionContextSet;
    }

    public void setExecutionContextSet(boolean executionContextSet) {
        this.executionContextSet = executionContextSet;
    }

    @Override
    public int hashCode() {
        return componentId.hashCode();
    }


    public boolean isStepFinished() {
        return stepFinished;
    }

    public void setStepFinished(boolean stepFinished) {
        this.stepFinished = stepFinished;
    }

    public Map<String, Object> getExecutionContextMap() {
        return executionContextMap;
    }

    public void setExecutionContextMap(Map<String, Object> executionContextMap) {
        this.executionContextMap = executionContextMap;
    }

    @Override
    public String toString() {


        return MoreObjects.toStringHelper(this)
                .add("eventIds", StringUtils.join(getEventIds()))
                .add("componentId", componentId)
                .add("componetName", componetName)
                .add("stepExecutionId", stepExecutionId)
                .add("version", version)
                .add("stepFinished", stepFinished)
                .toString();
    }
}
