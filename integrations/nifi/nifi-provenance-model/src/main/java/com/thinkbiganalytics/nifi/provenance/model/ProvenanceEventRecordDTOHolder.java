package com.thinkbiganalytics.nifi.provenance.model;

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

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Group all the Batch ProvenanceEvent objects together and send this parent object over to JMS
 */
public class ProvenanceEventRecordDTOHolder implements Serializable {

    /**
     * Unique UUID for the group of events
     */
    private String batchId;

    /**
     * The list of events to send
     */
    private List<ProvenanceEventRecordDTO> events;

    public ProvenanceEventRecordDTOHolder() {
        this.batchId = UUID.randomUUID().toString();

    }

    /**
     * calculate the max event id
     *
     * @return The max event id in the collection
     */
    public Long getMaxEventId() {
        if (events != null) {
            return events.stream().mapToLong(e -> e.getEventId()).max().getAsLong();
        }
        return -1L;
    }

    /**
     * Calculate the min event Id
     *
     * @return the min event id in the collection
     */
    public Long getMinEventId() {
        if (events != null) {
            return events.stream().mapToLong(e -> e.getEventId()).min().getAsLong();
        }
        return -1L;
    }

    /**
     * get all the events in the collection
     */
    public List<ProvenanceEventRecordDTO> getEvents() {
        return events;
    }

    /**
     * sets the events to be processed
     *
     * @param events the events to collect
     */
    public void setEvents(List<ProvenanceEventRecordDTO> events) {
        this.events = events;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProvenanceEventRecordDTOHolder{");
        sb.append("events=").append(events != null ? events.size() : 0);
        sb.append('}');
        return sb.toString();
    }

    /**
     * get the Unique Id for this collection of events
     */
    public String getBatchId() {
        return batchId;
    }

}
