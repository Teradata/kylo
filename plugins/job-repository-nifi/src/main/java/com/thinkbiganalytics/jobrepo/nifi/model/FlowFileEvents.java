package com.thinkbiganalytics.jobrepo.nifi.model;


import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 * Created by sr186054 on 2/25/16.
 */

public class FlowFileEvents extends RunStatusContext implements Serializable {


    private static final Logger LOG = LoggerFactory.getLogger(FlowFileEvents.class);


    private String uuid;
    private Set<ProvenanceEventRecordDTO> events;
    private Set<FlowFileEvents> parents;

    private Set<FlowFileEvents> children;

    private Map<String, FlowFileComponent> components;

    private Set<FlowFileComponent> runningComponents = new HashSet<FlowFileComponent>();

    private Set<FlowFileEvents> runningFlowFiles = new HashSet<FlowFileEvents>();

    private ProvenanceEventRecordDTO firstEvent;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    private DateTime createDate;

    private boolean insertedIntoDatabase;

    private Long jobExecutionId;

    private NifiJobExecution nifiJobExecution;


    public Long getJobExecutionId() {
        if(this.jobExecutionId == null) {
            if (this.getFirstEvent() != null && this.getFirstEvent().getFlowFileComponent() != null && this.getFirstEvent().getFlowFileComponent().getJobExecution() != null) {
                this.jobExecutionId = this.getFirstEvent().getFlowFileComponent().getJobExecution().getJobExecutionId();
            }
        }
        return this.jobExecutionId;
    }

    public Set<String> getAllFlowFileIds(){
        Set<String>flowFileIds = new HashSet();
        flowFileIds.add(getUuid());
       for(FlowFileEvents child : getChildren()){
           flowFileIds.addAll(child.getAllFlowFileIds());
       }
        return flowFileIds;

    }

    public FlowFileEvents(String uuid) {
        this.uuid = uuid;
        this.createDate = new DateTime();
    }


    public Set<ProvenanceEventRecordDTO> getEvents() {
        if (events == null) {
            events = new HashSet<>();
        }
        return events;
    }

    public void addEvent(ProvenanceEventRecordDTO event) {
        getEvents().add(event);
        event.setFlowFile(this);
    }

    public void removeEvent(ProvenanceEventRecordDTO event) {
        getEvents().remove(event);
    }

    public Set<FlowFileEvents> getParents() {
        if (parents == null) {
            parents = new HashSet<>();
        }
        return parents;
    }

    public Set<FlowFileEvents> getChildren() {
        if (children == null) {
            children = new HashSet<>();
        }
        return children;
    }

    public void addChild(FlowFileEvents flowFile) {
        if (!flowFile.equals(this)) {
            getChildren().add(flowFile);
        }
    }

    public void addParent(FlowFileEvents flowFile) {
        if (!flowFile.equals(this)) {
            getParents().add(flowFile);
        }
    }

    public FlowFileComponent getOrAddComponent(String componentId) {
        FlowFileComponent component = null;
        if (!getComponents().containsKey(componentId)) {
            component = new FlowFileComponent(componentId);
            addComponent(component);
            LOG.info("Creating new FlowFileComponent for {} ", component);
        } else {
            component = getComponent(componentId);
        }
        return component;
    }

    public void addComponent(FlowFileComponent component) {
        getComponents().put(component.getComponentId(), component);
    }

    public Map<String, FlowFileComponent> getComponents() {
        if (components == null) {
            components = new HashMap<>();
        }
        return components;
    }

    public Set<FlowFileComponent> getAllComponents() {
        Set<FlowFileComponent> components = new HashSet<>();
        components.addAll(getComponents().values());
        if (getChildren() != null) {
            for (FlowFileEvents event : getChildren()) {
                components.addAll(event.getAllComponents());
            }
        }
        return components;
    }


    public Map<String, FlowFileComponent> getAllComponentsAsMap() {
        Map<String, FlowFileComponent> components = new HashMap<>();
        components.putAll(getComponents());
        if (getChildren() != null) {
            for (FlowFileEvents event : getChildren()) {
                components.putAll(event.getAllComponentsAsMap());
            }
        }
        return components;
    }

    public boolean containsComponent(String componentId) {
        Map<String, FlowFileComponent> componentMap = getAllComponentsAsMap();
        return componentMap.containsKey(componentId);
    }

    public FlowFileComponent getComponent(String componentId) {
        return getComponents().get(componentId);
    }

    public boolean markComponentComplete(String componentId, DateTime startTime) {
        FlowFileComponent component = getComponent(componentId);
        if (component != null) {
            boolean complete = component.markCompleted(startTime);
            if (complete) {
                runningComponents.remove(component);
            }
            return complete;
        }
        return false;
    }

    public boolean markComponentRunning(String componentId, DateTime startTime) {
        FlowFileComponent component = getComponent(componentId);
        if (component != null) {
            LOG.debug("Flow File Events... mark component as Running for {}... current status: {} ", component, component.getRunStatus());
            boolean running = component.markRunning(startTime);
            LOG.debug("Flow File Events running status:... mark component as Running? {} for {} ", running, component);
            if (running) {
                runningComponents.add(component);
            }
            return running;
        }
        return false;
    }

    public boolean isParent() {
        return getParents().isEmpty();
    }

    public boolean hasEvents() {
        return !getEvents().isEmpty();
    }

    /**
     * returns true if the incoming event is the first one in the flow file
     *
     * @param event
     * @return
     */
    public boolean isStartingEvent(ProvenanceEventRecordDTO event) {
        return (getEvents().size() == 1 && events.iterator().next().equals(event));
    }

    /**
     * returns true if the incoming event is the first one in the entire flow  (all flow files)
     *
     * @param event
     * @return
     */
    public boolean isRootEvent(ProvenanceEventRecordDTO event) {
        return (isParent() && event.getEventType().equalsIgnoreCase("RECEIVE") || event.getEventType().equalsIgnoreCase("CREATE")); //&& getEvents().size() == 1 && events.iterator().next().equals(event));
    }

    public ProvenanceEventRecordDTO getPreviousEventInCurrentFlowFile(ProvenanceEventRecordDTO e) {
        List<ProvenanceEventRecordDTO> events = sortEvents(getEvents());
        Integer index = events.indexOf(e);
        if (index != null && index > 0) {
            return events.get(index - 1);
        }
        return null;
    }

    public ProvenanceEventRecordDTO getLastEvent() {
        if (this.hasEvents()) {
            Set<ProvenanceEventRecordDTO> events = getEvents();
            return sortEvents(events).get(events.size() - 1);
        }
        return null;
    }

    public ProvenanceEventRecordDTO getFirstEvent() {
        if (this.firstEvent == null) {
            if (this.hasEvents()) {
                Set<ProvenanceEventRecordDTO> events = getEvents();
                ProvenanceEventRecordDTO event = sortEvents(events).get(0);
                this.firstEvent = event;
                return event;
            }
            return null;
        } else {
            return firstEvent;
        }
    }


    /**
     * Lookup at the current flow file and then up the chain if there are other events to in the prev. flow file
     *
     * @param e
     * @return
     */
    public ProvenanceEventRecordDTO getPreviousEvent(ProvenanceEventRecordDTO e) {
        ProvenanceEventRecordDTO previousEvent = getPreviousEventInCurrentFlowFile(e);
        if (previousEvent != null) {
            return previousEvent;
        } else if (!getParents().isEmpty()) {
            //get all the Events for the immediate Parents
            Set<ProvenanceEventRecordDTO> parentEvents = new HashSet<>();
            for (FlowFileEvents parent : getParents()) {
                if (parent.hasEvents()) {
                    parentEvents.addAll(parent.getEvents());
                }
            }
            if (!parentEvents.isEmpty()) {
                List<ProvenanceEventRecordDTO> flowFileEvents = sortEvents(parentEvents);
                //reverse it as we are going backwards
                Collections.reverse(flowFileEvents);
                return flowFileEvents.get(0);
            }
        }
        return null;
    }

    public boolean containsChild(FlowFileEvents flowFile) {
        boolean contains = getChildren().contains(flowFile);
        if (!contains && !getChildren().isEmpty()) {
            for (FlowFileEvents child : getChildren()) {
                contains = child.containsChild(flowFile);
            }
        }
        return contains;
    }

    public boolean isActive() {
        boolean active = isRunning();
        if (!active) {
            for (FlowFileEvents children : getChildren()) {
                active = children.isActive();
                if (active) {
                    break;
                }
            }
        }
        return active;
    }


    public boolean areComponentsComplete() {
        for (FlowFileComponent component : getComponents().values()) {
            if (component.isRunning()) {
                return false;
            }
        }
        return true;
    }

    public FlowFileEvents findFlowFile(String flowFileUUID) {
        FlowFileEvents match = null;
        if (getUuid().equalsIgnoreCase(flowFileUUID)) {
            match = this;
        } else {
            for (FlowFileEvents child : getChildren()) {
                match = child.findFlowFile(flowFileUUID);
                if (match != null) {
                    break;
                }
            }
        }
        return match;
    }

    public void addEventPriorTo(ProvenanceEventRecordDTO eventToAdd, ProvenanceEventRecordDTO priorToEvent) {
        eventToAdd.setEventId(priorToEvent.getEventId());
        eventToAdd.setEventTime(DateUtils.addMilliseconds(priorToEvent.getEventTime(), -1));
        getEvents().add(eventToAdd);
    }

    public boolean areAllComponentsComplete() {
        boolean complete = true;

        if (complete) {
            for (FlowFileComponent component : getComponents().values()) {
                if (component.isRunning()) {
                    complete = false;
                    break;
                }
            }
            if (!getChildren().isEmpty()) {
                for (FlowFileEvents c : getChildren()) {
                    complete = c.areAllComponentsComplete();
                    if (!complete) {
                        break;
                    }
                }
            }
        }

        return complete;
    }

    public boolean updateCompletedStatus() {
        if (isRunning() && areComponentsComplete()) {
            boolean isComplete = markCompleted();
            return isComplete;
        }
        return false;
    }

    public Set<FlowFileEvents> getInitialFlowFiles() {
        Set<FlowFileEvents> files = new HashSet<>();
        if (isInitial()) {
            files.add(this);
        }
        for (FlowFileEvents flowFile : getChildren()) {
            files.addAll(flowFile.getInitialFlowFiles());
        }
        return files;
    }

    public boolean hasInitialFlowFiles() {
        return !getInitialFlowFiles().isEmpty();
    }


    public Set<FlowFileEvents> getRunningFlowFiles() {

        Set<FlowFileEvents> runningEvents = new HashSet<>();
        if (isRunning()) {
            runningEvents.add(this);
        }
        for (FlowFileEvents flowFile : getChildren()) {
            runningEvents.addAll(flowFile.getRunningFlowFiles());
        }
        return runningEvents;

    }

    private List<ProvenanceEventRecordDTO> sortEvents(Set<ProvenanceEventRecordDTO> events) {
        List<ProvenanceEventRecordDTO> eventList = new ArrayList<ProvenanceEventRecordDTO>(events);
        Collections.sort(eventList, new ProvenanceEventComparator());
        return eventList;
    }


    public Set<ProvenanceEventRecordDTO> getEventsAndParents() {
        Set<ProvenanceEventRecordDTO> events = getEvents();
        for (FlowFileEvents parent : getParents()) {
            events.addAll(parent.getEventsAndParents());
        }
        return events;
    }

    public List<ProvenanceEventRecordDTO> getEventsAndChildren() {
        List<ProvenanceEventRecordDTO> events = new ArrayList<>();
        events.addAll(sortEvents(getEvents()));
        for (FlowFileEvents child : getChildren()) {
            events.addAll(child.getEventsAndChildren());
        }
        return events;
    }

    public void printAllEvents() {
        List<ProvenanceEventRecordDTO> events = getEventsAndChildren();
        for (ProvenanceEventRecordDTO event : events) {
            System.out.println(" EVENT " + event.getFlowFileUuid() + ", " + event.getComponentId() + ", " + event.getComponentType() + "  " + event.getEventId() + ". " + event.getFlowFileComponent());
        }
    }

    /**
     * get all events starting with incoming event and then go up and get all parents
     *
     * @param event
     * @return
     */
    public List<ProvenanceEventRecordDTO> getLineagePriorToEvent(ProvenanceEventRecordDTO event) {
        //Sort the events in order
        List<ProvenanceEventRecordDTO> flowFileEvents = sortEvents(getEvents());
        //reverse it as we are going backwards
        Collections.reverse(flowFileEvents);

        Set<ProvenanceEventRecordDTO> events = new HashSet<>();
        for (ProvenanceEventRecordDTO flowFileEvent : flowFileEvents) {
            //start adding when the first event matches this event
            if (event.equals(flowFileEvent) && events.isEmpty()) {
                events.add(flowFileEvent);
            } else if (!events.isEmpty()) {
                events.add(flowFileEvent);
            }
        }
        //add all the parents
        for (FlowFileEvents parent : getParents()) {
            events.addAll(parent.getEventsAndParents());
        }
        return sortEvents(events);
    }

    /**
     * Gets all events on this FlowFile and then go back up and get all parent events
     *
     * @return
     */
    public List<ProvenanceEventRecordDTO> getLineagePriorToMe() {
        Set<ProvenanceEventRecordDTO> events = getEvents();
        //add all the children
        for (FlowFileEvents child : getParents()) {
            events.addAll(child.getEventsAndParents());
        }
        return sortEvents(events);
    }

    public List<ProvenanceEventRecordDTO> getLineageStartingWithMe() {
        Set<ProvenanceEventRecordDTO> events = getEvents();
        //add all the children
        for (FlowFileEvents child : getChildren()) {
            events.addAll(child.getEventsAndChildren());
        }
        return sortEvents(events);
    }

    public List<ProvenanceEventRecordDTO> getLineageStartingWithEvent(ProvenanceEventRecordDTO event) {
        //Sort the events in order
        List<ProvenanceEventRecordDTO> flowFileEvents = sortEvents(getEvents());

        Set<ProvenanceEventRecordDTO> events = new HashSet<>();
        for (ProvenanceEventRecordDTO flowFileEvent : flowFileEvents) {
            //start adding when the first event matchs this event
            if (event.equals(flowFileEvent) && events.isEmpty()) {
                events.add(flowFileEvent);
            } else if (!events.isEmpty()) {
                events.add(flowFileEvent);
            }
        }
        //add all the children
        for (FlowFileEvents child : getChildren()) {
            events.addAll(child.getEventsAndChildren());
        }
        return sortEvents(events);
    }

    public FlowFileEvents getRoot() {
        FlowFileEvents root = null;
        if (this.isParent()) {
            root = this;
        } else {
            for (FlowFileEvents flowFile : getParents()) {
                root = flowFile.getRoot();
            }
        }
        return root;
    }

    public List<ProvenanceEventRecordDTO> getFullLineage() {
        FlowFileEvents root = this.getRoot();
        return root.getEventsAndChildren();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlowFileEvents flowFile = (FlowFileEvents) o;

        return uuid.equals(flowFile.uuid);


    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public DateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(DateTime createDate) {
        this.createDate = createDate;
    }

    public boolean isInsertedIntoDatabase() {
        return insertedIntoDatabase;
    }

    public void setInsertedIntoDatabase(boolean insertedIntoDatabase) {
        this.insertedIntoDatabase = insertedIntoDatabase;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    public NifiJobExecution getNifiJobExecution() {
        return nifiJobExecution;
    }

    public void setNifiJobExecution(NifiJobExecution nifiJobExecution) {
        this.nifiJobExecution = nifiJobExecution;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uuid", uuid)
                .add("nifiJobExecution", nifiJobExecution)
                .add("jobExecutionId", jobExecutionId)
                .toString();
    }
}
