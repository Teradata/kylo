package com.thinkbiganalytics.nifi.provenance.repo;
/*-
 * #%L
 * thinkbig-nifi-provenance-repo
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
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.joda.time.DateTime;

public class ProvenanceEventRecordWithId {

    private Long eventId;
    private ProvenanceEventRecord event;
    private Long timeAdded;

    public ProvenanceEventRecordWithId(Long eventId, ProvenanceEventRecord event) {
        this.eventId = eventId;
        this.event = event;
        this.timeAdded = DateTime.now().getMillis();
    }

    public Long getEventId() {
        return eventId;
    }

    public ProvenanceEventRecord getEvent() {
        return event;
    }

    public Long getTimeAdded() {
        return timeAdded;
    }

    public Long getWaitTimeMillis(){
        return DateTime.now().getMillis() - timeAdded;
    }

    public boolean isWaitingTooLong(Long maxWaitTime){
        return getWaitTimeMillis() > maxWaitTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProvenanceEventRecordWithId)) {
            return false;
        }

        ProvenanceEventRecordWithId that = (ProvenanceEventRecordWithId) o;

        if (eventId != null ? !eventId.equals(that.eventId) : that.eventId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return eventId != null ? eventId.hashCode() : 0;
    }
}
