package com.thinkbiganalytics.nifi.provenance.model.util;
/*-
 * #%L
 * thinkbig-nifi-provenance-model
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Builder that helps applications create custom Provenance Events that can be sent to Kylo Operations Manager to create Jobs and Steps
 */
public class ProvenanceEventDtoBuilder {

    public static enum EventType {
        CREATE, DROP;
    }


    private String feedName;
    private String flowFileId;
    private String jobFlowFileId;
    private String componentName;
    private boolean startingEvent;
    private String componentType;
    private String firstEventProcessorId;
    private Long eventId;
    private Map<String, String> updatedAttributes;
    private Map<String, String> previousAttributes;
    private EventType eventType;
    private Long startTime;
    private Long eventTime;
    private boolean stream;
    private boolean endingEvent;

    private String componentId;

    /**
     * The Event.
     * @param feedName the Feed name to associate this event with.  This is the category.feed
     * @param flowFileId the flow file Id for this step.  Note you should also set the jobFlowFileId to relate this step to a corresponding job if it results in a split/join downstream
     * @param componentName  this will be the name of the Step generated in Ops Manager
     */
    public ProvenanceEventDtoBuilder(String feedName, String flowFileId, String componentName) {
        this.feedName = feedName;
        this.flowFileId = flowFileId;
        this.componentName = componentName;

    }

    /**
     * The tyoe of event. this will be automatically set based upon the <code>startingEvent</code> boolean.
     * @param eventType the type of event.  CREATE will start a new job in ops manager.  DROP will add to an existing job.
     * @return the builder
     */
    public ProvenanceEventDtoBuilder eventType(EventType eventType) {
        this.eventType = eventType;
        return this;
    }

    public ProvenanceEventDtoBuilder componentId(String componentId) {
        this.componentId = componentId;
        return this;
    }

    /**
     * The flowfile for the job which to attach this event to.  If not set the <code>flowfileId</code> will be used as the job flow file in Ops Manager
     * @param jobFlowFileId the flow file matching the job for this event
     * @return the builder
     */
    public ProvenanceEventDtoBuilder jobFlowFileId(String jobFlowFileId) {
        this.jobFlowFileId = jobFlowFileId;
        return this;
    }

    /**
     * The event id for this job.  If not set Kylo will use its internal decrement (- Long) id to represent its interal event ids.
     * If you dont set this it will automatically be set in the <code>build</code> method
     * @param eventId the event it.
     * @return the builder
     */
    public ProvenanceEventDtoBuilder eventId(Long eventId) {
        this.eventId = eventId;
        return this;
    }

    /**
     * The id for the first event for this flow.  This is not needed and probably should not be set.  the builder will handle this for you
     * @param firstEventProcessorId
     * @return
     */
    public ProvenanceEventDtoBuilder firstEventProcessorId(String firstEventProcessorId) {
        this.firstEventProcessorId = firstEventProcessorId;
        return this;
    }

    public ProvenanceEventDtoBuilder stream(boolean stream) {
        this.stream = stream;
        return this;
    }

    /**
     * Add an attibute to the update map to show up in the step execution context
     * @param key the property key
     * @param value the value
     * @return the builder
     */
    public ProvenanceEventDtoBuilder addUpdateAttribute(String key, String value) {
        if(this.updatedAttributes == null){
            this.updatedAttributes = new HashMap<>();
        }
        this.updatedAttributes.put(key,value);
        return this;
    }

    /**
     * Any context data you wish to add to the Step to show up in Operations manager add it here.
     * @param updatedAttributes the map of attributes to add to this step
     * @return the builder
     */
    public ProvenanceEventDtoBuilder updatedAttributes(Map<String, String> updatedAttributes) {
        this.updatedAttributes = updatedAttributes;
        return this;
    }

    public ProvenanceEventDtoBuilder endingEvent(boolean endingEvent) {
        this.endingEvent = endingEvent;
        return this;
    }
    public ProvenanceEventDtoBuilder startingEvent(boolean startingEvent) {
        this.startingEvent = startingEvent;
        return this;
    }

    public ProvenanceEventDtoBuilder startTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    public ProvenanceEventDtoBuilder eventTime(Long eventTime) {
        this.eventTime = eventTime;
        return this;
    }

    public ProvenanceEventDtoBuilder previousAttributes(Map<String, String> previousAttributes) {
        this.previousAttributes = previousAttributes;
        return this;
    }

    /**
     * Build the event record
     * @return the event record
     */
    public ProvenanceEventRecordDTO build() {
        if (this.eventId == null) {
            this.eventId = LongIdGenerator.nextId();
        }
        if (this.eventType == null) {
            this.eventType = EventType.DROP;
        }
        if (StringUtils.isBlank(componentType)) {
            componentType = "CustomComponent";
        }
        Long now = DateTime.now().getMillis();
        if (startTime == null) {
            startTime = now;
        }
        if (eventTime == null) {
            eventTime = now;
        }
        if(jobFlowFileId == null) {
            jobFlowFileId = flowFileId;
        }

        ProvenanceEventRecordDTO event = new ProvenanceEventRecordDTO();
        event.setFeedName(feedName);
        event.setFirstEventProcessorId(firstEventProcessorId);
        event.setJobFlowFileId(jobFlowFileId);
        event.setFlowFileUuid(flowFileId);
        event.setComponentName(componentName);
        event.setStartTime(startTime);
        event.setEventTime(eventTime);
        event.setStream(stream);
        event.setIsFinalJobEvent(endingEvent);
        event.setComponentId(UUID.randomUUID().toString());


        event.setEventId(eventId);
        event.setIsStartOfJob(startingEvent);
        event.setEventType(eventType.name());
        if (startingEvent) {
            event.setEventType("CREATE");
        }
        event.setComponentType(componentType);
        Map<String, String> attrs = new HashMap<>();
        if (updatedAttributes == null) {
            updatedAttributes = attrs;
        }
        if (previousAttributes == null) {
            previousAttributes = new HashMap<>();
        }
        event.setUpdatedAttributes(updatedAttributes);
        event.setPreviousAttributes(previousAttributes);

        Map<String, String> attributeMap = updatedAttributes;
        if (!previousAttributes.isEmpty()) {
            attributeMap = new HashMap<>();
            attributeMap.putAll(previousAttributes);
            attributeMap.putAll(updatedAttributes);
        }
        event.setAttributeMap(attributeMap);
        return event;

    }

}
