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

import java.util.Arrays;
import java.util.Comparator;

/**
 */
public class ProvenanceEventDtoUtil {

    public static final String AUTO_TERMINATED_FAILURE_RELATIONSHIP = "auto-terminated by failure relationship";

    public static final String FLOWFILE_QUEUE_EMPTIED = "flowfile queue emptied";

    protected static final String[] STARTING_EVENT_TYPES = {"RECEIVE", "CREATE"};

    protected static final String[] ENDING_EVENT_TYPES = {"DROP", "EXPIRE"};

    public static boolean contains(String[] allowedEvents, String event) {
        return Arrays.stream(allowedEvents).anyMatch(event::equals);
    }

    /**
     * Check if the event is one that kicks off the flow
     */
    public static boolean isFirstEvent(ProvenanceEventRecordDTO event) {
        return contains(STARTING_EVENT_TYPES, event.getEventType());
    }


    public static boolean isEndingFlowFileEvent(ProvenanceEventRecordDTO event) {
        return contains(ENDING_EVENT_TYPES, event.getEventType());
    }

    public static boolean isTerminatedByFailureRelationship(ProvenanceEventRecordDTO event) {
        return event.getDetails() != null && AUTO_TERMINATED_FAILURE_RELATIONSHIP.equalsIgnoreCase(event.getDetails());
    }

    public static boolean isFlowFileQueueEmptied(ProvenanceEventRecordDTO event) {
        return (isEndingFlowFileEvent(event) && StringUtils.isNotBlank(event.getDetails()) && event.getDetails().toLowerCase().startsWith(FLOWFILE_QUEUE_EMPTIED));
    }

}
