package com.thinkbiganalytics.jobrepo.nifi.provenance;


import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileComponent;
import com.thinkbiganalytics.jobrepo.nifi.model.FlowFileEvents;
import com.thinkbiganalytics.jobrepo.nifi.model.NifiJobExecution;
import com.thinkbiganalytics.jobrepo.nifi.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.rest.client.NifiConnectionException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

/**
 * Act upon a Provenance Event. The ProvenanceEventReceiver JMS class will call this class when it gets a new event
 */
@Component
public class ProvenanceEventListener {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceEventListener.class);


    private AtomicInteger eventCounter = new AtomicInteger(0);
    private AtomicInteger componentCounter = new AtomicInteger(0);
    private List<String> startedComponents = new ArrayList<>();


    @Inject
    private FlowFileEventProvider flowFileEventProvider;


    public ProvenanceEventListener() {

    }

    @Autowired
    private ProvenanceFeedManager provenanceFeedManager;


    private List<Long> eventIds = new ArrayList<>();

    public FlowFileEvents attachFlowFileAndComponentsToEvent(ProvenanceEventRecordDTO event) {
        eventIds.add(event.getEventId());
        String flowFileId = event.getFlowFileUuid();

        FlowFileEvents flowFile = flowFileEventProvider.addFlowFile(flowFileId);

        //find the Group Id associated with this event
        provenanceFeedManager.populateGroupIdForEvent(event);

        flowFile.addEvent(event);
        if (event.getParentUuids() != null && !event.getParentUuids().isEmpty()) {
            for (String parent : event.getParentUuids()) {
                FlowFileEvents parentFlowFile = flowFileEventProvider.addFlowFile(parent);
                flowFile.addParent(parentFlowFile);
                parentFlowFile.addChild(flowFile);
            }
        }

        if (event.getChildUuids() != null && !event.getChildUuids().isEmpty()) {
            for (String child : event.getChildUuids()) {
                FlowFileEvents childFlowFile = flowFileEventProvider.addFlowFile(child);
                flowFile.addChild(childFlowFile);
                childFlowFile.addParent(flowFile);
            }
        }

        // flowFileEventProvider.addEvent(event);
        attachEventComponent(event);
        provenanceFeedManager.setComponentName(event);

        log.debug("added Event to internal Map.  About to process event for component: {} in the system", event, event.getFlowFileComponent().getComponetName());
        return flowFile;
    }


    public void receiveEvent(ProvenanceEventRecordDTO event) throws NifiConnectionException {
        long start = System.currentTimeMillis();
        FlowFileEvents flowFile = attachFlowFileAndComponentsToEvent(event);

        FlowFileComponent component = event.getFlowFileComponent();
        if (component == null) {
            attachEventComponent(event);
        }

        //Trigger the start of the flow if it is the first one in the chain of events
        if (flowFile.isRootEvent(event)) {
            log.info("Starting Feed for event {} ", event);
            provenanceFeedManager.feedStart(event);
        }
        setJobExecutionToComponent(event);

        log.debug("receiveEvent {}, event.hasJobExecution() = {} ", event, event.hasJobExecution());

        //only update if the event has a Job execution on it
        if (event.hasJobExecution()) {

            provenanceFeedManager.updateJobType(event);
            updateEventRunContext(event);
            provenanceFeedManager.feedEvent(event);

        } else {
            log.info("Skipping event {}, for type {}, with flowfile: {}  No Job Execution exists for it", event, event.getEventType(), event.getFlowFile());
        }

        long stop = System.currentTimeMillis();
        log.debug("************ Total time to process event {} ({}): {}",component.getComponetName(),event.getEventType(),(stop-start)+" ms");
    }

    private FlowFileComponent attachEventComponent(ProvenanceEventRecordDTO event) {
        String flowFileId = event.getFlowFileUuid();
        FlowFileEvents flowFile = flowFileEventProvider.getFlowFile(flowFileId);
        FlowFileComponent component = flowFile.getOrAddComponent(event.getComponentId());
        event.setFlowFileComponent(component);
        return component;
    }

    private void setJobExecutionToComponent(ProvenanceEventRecordDTO event) {
        FlowFileComponent component = event.getFlowFileComponent();
        if (component == null) {
            component = attachEventComponent(event);
        }
        //set the components job execution
        if (component.getJobExecution() == null) {
            component.updateJobExecution();
        }
        if (component.getJobExecution() == null) {
            log.info("component does not have a JobExecution... attempt to query and find previous job execution for event {} and flowfile {} ", event.getEventId(), event.getFlowFileUuid());
            ///find the Job by the executionId and then add it
            NifiJobExecution jobExecution = provenanceFeedManager.getNifiJobExection(event.getEventId(), event.getFlowFileUuid());
            if (jobExecution != null) {
                log.info("setJobExecution by querying by Event and flowfile as {} for event {} and flowfile {} ", jobExecution.getJobExecutionId(), event.getEventId(), event.getFlowFileUuid());
                component.setJobExecution(jobExecution);
                event.getFlowFile().setNifiJobExecution(jobExecution);
                if (component.getFirstEvent().getFlowFile().getNifiJobExecution() == null) {
                    component.getFirstEvent().getFlowFile().setNifiJobExecution(jobExecution);
                }
            } else {
                log.info("unable to get JobExecution for event {} and flowfile {} ", event.getEventId(), event.getFlowFileUuid());
            }
        }
    }

    private DateTime getStepStartTime(ProvenanceEventRecordDTO event) {
        String flowFileId = event.getFlowFileUuid();
        FlowFileEvents flowFile = flowFileEventProvider.getFlowFile(flowFileId);
        ProvenanceEventRecordDTO previousEvent = flowFile.getPreviousEventInCurrentFlowFile(event);
        //assume if we get to the next Event in the chain than the previous event is Complete
        if (previousEvent == null) {
            previousEvent = flowFile.getPreviousEvent(event);
        }
        if (previousEvent != null) {
            log.debug("Getting StartTime for {} from previous EndTime of {} ({})", event.getFlowFileComponent(), previousEvent.getFlowFileComponent().getEndTime(),
                      previousEvent.getFlowFileComponent());
            return new DateTime(previousEvent.getFlowFileComponent().getEndTime());
        } else {
            log.debug("Cant Find Previous Event to get Start Time for {}, returning current time ", event.getFlowFileComponent());
            return new DateTime();
        }
    }

    private DateTime getEndTime(ProvenanceEventRecordDTO event) {
        //  if(event.getEndTime() != null) {
        //      return new DateTime(event.getEndTime());
        //  }
        //  else {
        return new DateTime();
        //  }
    }

    private void markEventComplete(ProvenanceEventRecordDTO event) {
        //mark the component as complete
        FlowFileEvents eventFlowFile = flowFileEventProvider.getFlowFile(event.getFlowFileUuid());
        if (eventFlowFile.markComponentComplete(event.getComponentId(), getEndTime(event))) {
            boolean isFailureProcessor = provenanceFeedManager.isFailureProcessor(event);
            log.debug("COMPLETING Component {}.  Is Failure Processor: {} ", event.getFlowFileComponent(), isFailureProcessor);
            //if the current Event is a failure Processor that means the previous component failed.
            //mark the previous event as failed
            if (isFailureProcessor) {
                //get or create the event and component for failure
                //lookup bulletins for failure events
                boolean addedFailure = provenanceFeedManager.processBulletinsAndFailComponents(event);
                //if the step was not failed because of bulletin records then fail it
                if (!addedFailure) {
                    provenanceFeedManager.componentFailed(event);
                }
            } else {
                provenanceFeedManager.componentCompleted(event);
            }
            componentCounter.decrementAndGet();
            startedComponents.remove(event.getComponentId());
        }
    }

    private boolean isCompletionEvent(ProvenanceEventRecordDTO event) {
        String[] nonCompletionEvents = {"SEND", "CLONE", "ROUTE"};

        return !Arrays.asList(nonCompletionEvents).contains(event.getEventType());
    }

    private void updateEventRunContext(ProvenanceEventRecordDTO event) throws NifiConnectionException {

        String flowFileId = event.getFlowFileUuid();
        FlowFileEvents flowFile = flowFileEventProvider.getFlowFile(flowFileId);

        ProvenanceEventRecordDTO previousEvent = flowFile.getPreviousEventInCurrentFlowFile(event);
        log.debug("updateEventRunContext for {}.  Previous Event in FlowFile is {} ", event, previousEvent);

        //We get the events after they actual complete, so the Start Time is really the previous event in the flowfile
        DateTime startTime = getStepStartTime(event);

        //mark the flow file as running if it is not already
        flowFile.markRunning(startTime);
        //mark the event as running
        if (event.markRunning()) {
            eventCounter.incrementAndGet();
        }

        //mark the component as running
        if (flowFile.markComponentRunning(event.getComponentId(), startTime)) {
            log.info("Starting Component {} in flowfile: {}, RUN_STATUS: {} ", event.getFlowFileComponent(), flowFile, event.getFlowFileComponent().getRunStatus());
            //if we start a failure processor check bulletins for other events
            if (provenanceFeedManager.isFailureProcessor(event)) {
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
        if ((previousEvent != null && !previousEvent.getComponentId().equalsIgnoreCase(event.getComponentId()))) {
            if (previousEvent.markCompleted(getEndTime(previousEvent))) {
                eventCounter.decrementAndGet();
                log.debug("MARKING PREVIOUS EVENT {} as Complete.", previousEvent);
                markEventComplete(previousEvent);
            }
            if (isCompletionEvent(event)) {
                log.debug("MARKING EVENT {} as Complete.", event);
                markEventComplete(event);
            }
        }

        if (previousEvent != null && !previousEvent.getFlowFileUuid().equalsIgnoreCase(event.getFlowFileUuid())) {
            //mark flow file as complete
            FlowFileEvents previousEventFlowFile = flowFileEventProvider.getFlowFile(previousEvent.getFlowFileUuid());
            previousEventFlowFile.updateCompletedStatus();

        }

        if (event.isDropEvent()) {
            //Drop Events indicate the end of the event
            if (event.markCompleted()) {
                eventCounter.decrementAndGet();
            }
            //mark the component as complete
            log.debug("DROP EVENT {} MARK as completed ", event);
            markEventComplete(event);
          /*  if (flowFile.markComponentComplete(event.getComponentId(), new DateTime())) {
                provenanceFeedManager.componentCompleted(event);
                componentCounter.decrementAndGet();
                startedComponents.remove(event.getComponentId());
            }
            */
            //mark the flow file as complete if all components are complete
            flowFile.updateCompletedStatus();
            //if(flowFile.markCompleted() && !flowFile.isParent()){
            //    flowFile.getRoot().removeRunningFlowFile(flowFile);
            // }
        }

        //update parent flow file status
        //loop through the child flow files and if they are all complete then mark parent as complete.
        if (flowFile.getRoot().areAllComponentsComplete() && !flowFile.getRoot().hasInitialFlowFiles() && flowFile.getRoot().getRunningFlowFiles().size() == 0) {
            provenanceFeedManager.feedCompleted(event);
            flowFileEventProvider.removeFlowFile(flowFile.getRoot().getUuid());
        }


    }

    public boolean isConnectedToNifi() {
        return provenanceFeedManager.isConnectedToNifi();
    }

    public void setProvenanceFeedManager(ProvenanceFeedManager provenanceFeedManager) {
        this.provenanceFeedManager = provenanceFeedManager;
    }

    public void setFlowFileEventProvider(FlowFileEventProvider flowFileEventProvider) {
        this.flowFileEventProvider = flowFileEventProvider;
    }
}


