package com.thinkbiganalytics.jobrepo.nifi.provenance;


import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileComponent;
import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileEvents;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sr186054 on 2/26/16.
 */
@Component
public class ProvenanceEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(ProvenanceEventListener.class);


    private AtomicInteger eventCounter = new AtomicInteger(0);
    private AtomicInteger componentCounter = new AtomicInteger(0);
    private List<String> startedComponents = new ArrayList<>();


    public ProvenanceEventListener(){
        int i =0;
    }

    @Autowired
    private ProvenanceFeedManager provenanceFeedManager;

    private Map<String,FlowFileEvents> flowFileMap = new HashMap<>();
    private Set<ProvenanceEventRecordDTO> events = new HashSet<>();

    private List<Long> eventIds = new ArrayList<>();




    public void receiveEvent(ProvenanceEventRecordDTO event){
        if(eventIds.contains(event.getEventId())) {
            int i = 0;
        }
        eventIds.add(event.getEventId());
        String flowFileId = event.getFlowFileUuid();

        FlowFileEvents flowFile = addFlowFile(flowFileId);

        //find the Group Id associated with this event
        provenanceFeedManager.populateGroupIdForEvent(event);
        LOG.info("EVENT "+event+" for "+event.getGroupId()+", "+provenanceFeedManager.getProcessor(event.getComponentId()).getName());

        flowFile.addEvent(event);
        if(event.getParentUuids() != null && ! event.getParentUuids().isEmpty()) {
            for (String parent: event.getParentUuids()) {
                FlowFileEvents parentFlowFile = addFlowFile(parent);
                flowFile.addParent(parentFlowFile);
                parentFlowFile.addChild(flowFile);
            }
        }

        if(event.getChildUuids() != null && ! event.getChildUuids().isEmpty()) {
            for(String child: event.getChildUuids()) {
                FlowFileEvents childFlowFile = addFlowFile(child);
                flowFile.addChild(childFlowFile);
                childFlowFile.addParent(flowFile);
            }
        }
        events.add(event);
        setJobExecutionToComponent(event);
        provenanceFeedManager.setComponentName(event);

        //Trigger the start of the flow if it is the first one in the chain of events
        if(flowFile.isRootEvent(event)){
            provenanceFeedManager.feedStart(event);
        }



        //only update if the event has a Job execution on it
        if(event.hasJobExecution()) {
            provenanceFeedManager.updateJobType(event);
            updateEventRunContext(event);
            provenanceFeedManager.feedEvent(event);
        }
        else {
            LOG.info("Skipping event "+event.getEventId()+", "+event.getEventType()+".  No Job Execution exists for it");
        }

    }
    private void setJobExecutionToComponent(ProvenanceEventRecordDTO event) {
        String flowFileId = event.getFlowFileUuid();
        FlowFileEvents flowFile = flowFileMap.get(flowFileId);
        FlowFileComponent component = flowFile.getOrAddComponent(event.getComponentId());
        event.setFlowFileComponent(component);
        //set the components job execution
        if(component.getJobExecution() == null){
            component.updateJobExecution();
        }
    }

    private void setFeedToFlowFiles(FlowFileEvents flowFileEvents){
        FlowFileEvents root = flowFileEvents.getRoot();
        //    provenanceFeedManager.getFeedProcessGroupIdForFlowFile(root.getFirstEvent())
    }

    private DateTime getStepStartTime(ProvenanceEventRecordDTO event){
        String flowFileId = event.getFlowFileUuid();
        FlowFileEvents flowFile = flowFileMap.get(flowFileId);
        ProvenanceEventRecordDTO previousEvent = flowFile.getPreviousEventInCurrentFlowFile(event);
        //assume if we get to the next Event in the chain than the previous event is Complete
        if(previousEvent == null){
            previousEvent = flowFile.getPreviousEvent(event);
        }
        if(previousEvent != null) {
            LOG.info("Getting StartTime for {} from previous EndTime of {} ({})",event.getFlowFileComponent(),previousEvent.getFlowFileComponent().getEndTime(),previousEvent.getFlowFileComponent());
            return new DateTime(previousEvent.getFlowFileComponent().getEndTime());
        }
        else {
            LOG.info("Cant Find Previous Event to get Start Time for ", event.getFlowFileComponent());
            return new DateTime();
        }
    }

    private void markEventComplete(ProvenanceEventRecordDTO event) {
        //mark the component as complete
        FlowFileEvents eventFlowFile = flowFileMap.get(event.getFlowFileUuid());
        if(eventFlowFile.markComponentComplete(event.getComponentId(), new DateTime())){
            LOG.info("COMPLETING Component Event {} ",event.getFlowFileComponent());
            //if the current Event is a failure Processor that means the previous component failed.
            //mark the previous event as failed
            if(provenanceFeedManager.isFailureProcessor(event)){
                //get or create the event and component for failure
                //lookup bulletins for failure events
                boolean addedFailure = provenanceFeedManager.processBulletinsAndFailComponents(event);
                if(addedFailure){
                    LOG.info("Added Failure for bulletins for {} ({}) ",event.getFlowFileComponent().getComponetName(),event.getComponentId());
                    provenanceFeedManager.componentFailed(event);
                }
                else {
                    provenanceFeedManager.componentCompleted(event);
                }
            }
            else {
                provenanceFeedManager.componentCompleted(event);
            }
            componentCounter.decrementAndGet();
            startedComponents.remove(event.getComponentId());
        }
    }

    private boolean isCompletionEvent(ProvenanceEventRecordDTO event){
        String[] nonCompletionEvents = {"SEND","CLONE","ROUTE"};

        return !Arrays.asList(nonCompletionEvents).contains(event.getEventType());
    }

    private void updateEventRunContext(ProvenanceEventRecordDTO event){
        String flowFileId = event.getFlowFileUuid();
        FlowFileEvents flowFile = flowFileMap.get(flowFileId);



        ProvenanceEventRecordDTO previousEvent = flowFile.getPreviousEventInCurrentFlowFile(event);


        //We get the events after they actual complete, so the Start Time is really the previous event in the flowfile
        DateTime startTime = getStepStartTime(event);

        //mark the flow file as running if it is not already
        flowFile.markRunning(startTime);
        //mark the event as running
        if(event.markRunning()) {
            eventCounter.incrementAndGet();
        }

        //mark the component as running
        if(flowFile.markComponentRunning(event.getComponentId(), startTime)){
            LOG.info("Starting Component IDS: FF ID: {}, Comp ID: {}, Component: {} in flowfile: {}, Flow File Component: {}, RUN_STATUS: {} ",System.identityHashCode(flowFile), System.identityHashCode(flowFile.getComponent(event.getComponentId())),event.getFlowFileComponent(), flowFile, flowFile.getComponent(event.getComponentId()), flowFile.getComponent(event.getComponentId()).getRunStatus());
            //if we start a failure processor check bulletins for other events
            if(provenanceFeedManager.isFailureProcessor(event)) {
                //get or create the event and component for failure
                //lookup bulletins for failure events
                boolean addedFailure = provenanceFeedManager.processBulletinsAndFailComponents(event);
            }

            String componentId = event.getComponentId();
            provenanceFeedManager.componentStarted(event);
            componentCounter.incrementAndGet();
            startedComponents.add(event.getComponentId());
        }

        //if the previous Event is the first in the flow file or if is for a different Component then mark the this one as complete
        if((previousEvent != null && !previousEvent.getComponentId().equalsIgnoreCase(event.getComponentId()))) {
            if(previousEvent.markCompleted()) {
                eventCounter.decrementAndGet();
                LOG.info("MARKING PREVIOUS EVENT {} as Complete . EndTime of: {}",previousEvent);
                markEventComplete(previousEvent);
            }
            if(isCompletionEvent(event)) {
                markEventComplete(event);
            }
        }



        if(previousEvent != null && !previousEvent.getFlowFileUuid().equalsIgnoreCase(event.getFlowFileUuid())) {
            //mark flow file as complete
            FlowFileEvents previousEventFlowFile = flowFileMap.get(previousEvent.getFlowFileUuid());
            previousEventFlowFile.updateCompletedStatus();

        }


        if(event.isDropEvent()){
            //Drop Events indicate the end of the event
            if(event.markCompleted()){
                eventCounter.decrementAndGet();
            }
            //mark the component as complete
            if(flowFile.markComponentComplete(event.getComponentId(), new DateTime())){
                provenanceFeedManager.componentCompleted(event);
                componentCounter.decrementAndGet();
                startedComponents.remove(event.getComponentId());
            }
            //mark the flow file as complete if all components are complete
            flowFile.updateCompletedStatus();
            //if(flowFile.markCompleted() && !flowFile.isParent()){
            //    flowFile.getRoot().removeRunningFlowFile(flowFile);
            // }
        }

        //update parent flow file status
        //loop through the child flow files and if they are all complete then mark parent as complete.
        if(flowFile.getRoot().areAllComponentsComplete() && !flowFile.getRoot().hasInitialFlowFiles() && flowFile.getRoot().getRunningFlowFiles().size() ==0){
            provenanceFeedManager.feedCompleted(event);
        }


    }

    private FlowFileEvents addFlowFile(String flowFileId)
    {
        if(!flowFileMap.containsKey(flowFileId)){
            flowFileMap.put(flowFileId,new FlowFileEvents(flowFileId));
        }
        return flowFileMap.get(flowFileId);
    }

    private ProvenanceEventRecordDTO getPreviousEvent(ProvenanceEventRecordDTO e){
        FlowFileEvents flowFile = flowFileMap.get(e.getFlowFileUuid());
        return flowFile.getPreviousEvent(e);
    }


    /**
     * get lineage of incoming event and all of its parents
     * @param event
     * @return
     */
    public List<ProvenanceEventRecordDTO> getLineagePriorToEvent(ProvenanceEventRecordDTO event){
        FlowFileEvents flowFile = flowFileMap.get(event.getFlowFileUuid());
        return flowFile.getLineagePriorToEvent(event);
    }

    /**
     * get all events for the incoming flow file and all of its parents
     * @param flowFileUuid
     * @return
     */
    public List<ProvenanceEventRecordDTO>getLineagePriorToFlowFile(String flowFileUuid){
        FlowFileEvents flowFile = flowFileMap.get(flowFileUuid);
        return flowFile.getLineagePriorToMe();
    }


    /**
     * Returns the full lineage for any event passed in getting both parents and children
     * @param event
     * @return
     */
    public List<ProvenanceEventRecordDTO> getFullLineage(ProvenanceEventRecordDTO event){
        String flowFileUuid = event.getFlowFileUuid();
        return getFullLineage(flowFileUuid);
    }

    /**
     * gets full lineage for an incoming flow file getting both parents and children
     * @param flowFileUuid
     * @return
     */
    public List<ProvenanceEventRecordDTO> getFullLineage(String flowFileUuid){
        FlowFileEvents flowFile = flowFileMap.get(flowFileUuid);
        return flowFile.getFullLineage();
    }



    /**
     * get Current Event and anything after it (all if the children)
     * @param event
     * @return
     */
    public List<ProvenanceEventRecordDTO> getLineageStartingWithEvent(ProvenanceEventRecordDTO event){
        String flowFileUuid = event.getFlowFileUuid();
        FlowFileEvents flowFile = flowFileMap.get(flowFileUuid);
        return flowFile.getLineageStartingWithEvent(event);
    }

    /**
     * gets the Lineage starting with the incoming flow file and getting all if its events and any children
     * @param flowFileUuid
     * @return
     */
    public List<ProvenanceEventRecordDTO> getLineageStartingWitFlowFile(String flowFileUuid){
        FlowFileEvents flowFile = flowFileMap.get(flowFileUuid);
        return flowFile.getLineageStartingWithMe();
    }


    public Set<ProvenanceEventRecordDTO> getEvents() {
        return events;
    }
}


