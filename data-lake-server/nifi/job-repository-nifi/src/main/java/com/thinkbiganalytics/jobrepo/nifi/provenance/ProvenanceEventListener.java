package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileComponent;
import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileEvents;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

     }

    @Autowired
    private  ProvenanceFeedManager provenanceFeedManager;

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


    private void updateEventRunContext(ProvenanceEventRecordDTO event){
        String flowFileId = event.getFlowFileUuid();
        FlowFileEvents flowFile = flowFileMap.get(flowFileId);


        ProvenanceEventRecordDTO previousEvent = flowFile.getPreviousEventInCurrentFlowFile(event);
        //assume if we get to the next Event in the chain than the previous event is Complete
        if(previousEvent != null) {
            if(previousEvent.markCompleted()) {
                eventCounter.decrementAndGet();
            }
        }


        //if the previous Event is for a different Component then mark the previous one as complete
        if(previousEvent != null && !previousEvent.getComponentId().equalsIgnoreCase(event.getComponentId())) {
            //mark the component as complete
            FlowFileEvents previousEventFlowFile = flowFileMap.get(previousEvent.getFlowFileUuid());
            if(previousEventFlowFile.markComponentComplete(previousEvent.getComponentId())){
                //if the current Event is a failure Processor that means the previous component failed.
                //mark the previous event as failed
                if(provenanceFeedManager.isFailureProcessor(event.getComponentId())){
                    //get or create the event and component for failure
                    //lookup bulletins for failure events
                    boolean addedFailure = provenanceFeedManager.processBulletinsAndFailComponents(event);
                    if(!addedFailure){
                        provenanceFeedManager.componentFailed(previousEvent);
                    }
                    else {
                        provenanceFeedManager.componentCompleted(previousEvent);
                    }
                }
                else {
                    provenanceFeedManager.componentCompleted(previousEvent);
                }
                componentCounter.decrementAndGet();
                startedComponents.remove(previousEvent.getComponentId());
            }

        }





        //mark the flow file as running if it is not already
        flowFile.markRunning();
        //mark the event as running
         if(event.markRunning()) {
             eventCounter.incrementAndGet();
         }

        //mark the component as running
        if(flowFile.markComponentRunning(event.getComponentId())){

            //if we start a failure processor check bulletins for other events
            if(provenanceFeedManager.isFailureProcessor(event.getComponentId())) {
                //get or create the event and component for failure
                //lookup bulletins for failure events
                boolean addedFailure = provenanceFeedManager.processBulletinsAndFailComponents(event);
            }

            String componentId = event.getComponentId();
            provenanceFeedManager.componentStarted(event);
            componentCounter.incrementAndGet();
            startedComponents.add(event.getComponentId());
        }




        if(previousEvent != null && !previousEvent.getFlowFileUuid().equalsIgnoreCase(event.getFlowFileUuid())) {
            //mark flow file as complete?
            FlowFileEvents previousEventFlowFile = flowFileMap.get(previousEvent.getFlowFileUuid());
           previousEventFlowFile.updateCompletedStatus();

        }


        if(event.isDropEvent()){
            //Drop Events indicate the end of the event
            if(event.markCompleted()){
                eventCounter.decrementAndGet();
            }
            //mark the component as complete
            if(flowFile.markComponentComplete(event.getComponentId())){
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


