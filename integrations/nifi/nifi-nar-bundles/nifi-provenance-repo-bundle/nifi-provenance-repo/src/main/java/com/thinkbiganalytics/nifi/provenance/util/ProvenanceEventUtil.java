package com.thinkbiganalytics.nifi.provenance.util;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;

import java.util.Arrays;

/**
 */
public class ProvenanceEventUtil {

    public static final String AUTO_TERMINATED_FAILURE_RELATIONSHIP = "auto-terminated by failure relationship";

    public static final String FLOWFILE_QUEUE_EMPTIED = "flowfile queue emptied";

    private static final ProvenanceEventType[] STARTING_EVENT_TYPES = {ProvenanceEventType.RECEIVE, ProvenanceEventType.CREATE};

    private static final ProvenanceEventType[] ENDING_EVENT_TYPES = {ProvenanceEventType.DROP, ProvenanceEventType.EXPIRE};

    public static boolean contains(ProvenanceEventType[] allowedEvents, ProvenanceEventType event) {
        return Arrays.stream(allowedEvents).anyMatch(event::equals);
    }

    /**
     * Check if the event is one that kicks off the flow
     */
    public static boolean isStartingFeedFlow(ProvenanceEventRecord event) {
        return contains(STARTING_EVENT_TYPES, event.getEventType());
    }


    public static boolean isEndingFlowFileEvent(ProvenanceEventRecord event) {
        return contains(ENDING_EVENT_TYPES, event.getEventType());
    }

    public static boolean isTerminatedByFailureRelationship(ProvenanceEventRecord event) {
        return event.getDetails() != null && AUTO_TERMINATED_FAILURE_RELATIONSHIP.equalsIgnoreCase(event.getDetails());
    }

    public static boolean isFlowFileQueueEmptied(ProvenanceEventRecord event) {
        return (isEndingFlowFileEvent(event) && StringUtils.isNotBlank(event.getDetails()) && event.getDetails().toLowerCase().startsWith(FLOWFILE_QUEUE_EMPTIED));
    }

}
