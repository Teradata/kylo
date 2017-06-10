package com.thinkbiganalytics.nifi.provenance;

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

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sr186054 on 6/5/17.
 */
public class BatchFeedProcessorEventCacheEntry {

    private String mapKey;

    private DateTime lastEventTime;

    /**
     * count of events processed
     *
     */
    AtomicInteger feedProcessorEventCount = new AtomicInteger(0);

    private Long sampleTimeInMillis;

    private Integer sampleEventRate;


    public BatchFeedProcessorEventCacheEntry(String mapKey, DateTime firstEventTime, Long sampleTimeInMillis, Integer sampleEventRate){
        this.mapKey = mapKey;
        this.lastEventTime = firstEventTime;
        this.sampleTimeInMillis = sampleTimeInMillis;
        this.sampleEventRate = sampleEventRate;
    }

    public String getMapKey() {
        return mapKey;
    }

    public void setLastEventTime(DateTime lastEventTime) {
        this.lastEventTime = lastEventTime;
    }

    public DateTime getLastEventTime() {
        return lastEventTime;
    }

    /**
     * if an event coming in was throttled and now its not this will get called
     * @param event
     */
    protected void resetSampleEventCount(ProvenanceEventRecordDTO event){
        lastEventTime =event.getEventTime();
        //reset
        feedProcessorEventCount.set(0);
    }


    public boolean isProcessEvent(ProvenanceEventRecordDTO event) {
        if(lastEventTime == null) {
            lastEventTime = event.getEventTime();
        }

        boolean process = false;
        //if the event is the last one for the entire feed flow, and its being tracked as a job in ops manager send it
        if (event.isFinalJobEvent() && !event.getFeedFlowFile().isStream()) {
            process = true;
        }
        else {
            Long diff = event.getEventTime().getMillis() - lastEventTime.getMillis();

            Integer eventsPerThreshold = 0;
            if (diff < sampleTimeInMillis) {
                eventsPerThreshold = feedProcessorEventCount.incrementAndGet();
                if (eventsPerThreshold <= sampleEventRate) {
                    process = true;
                    lastEventTime = event.getEventTime();
                } else {
                    //over the sample rate.
                    // do we also peek in and get some value if we get over xx events?
                    int i =0;
                }
            } else {
                process = true;
                resetSampleEventCount(event);
            }
        }


        return process;
    }


    public boolean process(ProvenanceEventRecordDTO event) {

        boolean process = isProcessEvent(event);
        if (!process) {
           event.setStream(true);
        }

        return process;
    }

}
