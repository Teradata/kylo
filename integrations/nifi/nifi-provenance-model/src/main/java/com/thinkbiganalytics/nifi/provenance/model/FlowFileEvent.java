package com.thinkbiganalytics.nifi.provenance.model;

import org.joda.time.DateTime;

import java.util.Set;

/**
 * Created by sr186054 on 8/20/16.
 */
public interface FlowFileEvent<P extends FlowFileEvent> {

    Long getEventId();

    void setEventId(Long eventId);

    Long getEventDuration();

    void setEventDuration(Long eventDuration);

    DateTime getEventTime();

    void setEventTime(DateTime eventTime);

    P getPreviousEvent();

    void setPreviousEvent(P previousEvent);

    String getComponentId();

    void setComponentId(String componentId);

    Set<String> getParentFlowFileIds();

    void setParentFlowFileIds(Set<String> parentFlowFileIds);

    String getEventType();

    void setEventType(String eventType);

    void setFlowFile(FlowFile flowfile);

    FlowFile getFlowFile();
}
