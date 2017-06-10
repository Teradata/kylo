package com.thinkbiganalytics.nifi.provenance;/*-
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
import com.thinkbiganalytics.nifi.provenance.repo.ThrottleEvents;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.apache.nifi.provenance.StandardProvenanceEventRecord;

import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 6/8/17.
 */
public class TestProducer {

    AtomicLong eventId = new AtomicLong(0);

    private BlockingQueue<Map.Entry<Long,ProvenanceEventRecord>> processingQueue;

    public TestProducer(BlockingQueue<Map.Entry<Long,ProvenanceEventRecord>> processingQueue) {
        this.processingQueue = processingQueue;
    }

    private String componentId1 = UUID.randomUUID().toString();
    private String componentId2 = UUID.randomUUID().toString();
    private String componentId3 = UUID.randomUUID().toString();

    private AtomicLong skippedCount = new AtomicLong(0);

    private String componentType = "com.thinkbiganalytics.ComponentType";
    public void create(boolean throttle) {


        String flowFileId = UUID.randomUUID().toString();
        ProvenanceEventRecord start =  buildEvent(componentId1,ProvenanceEventType.CREATE,flowFileId);
        ProvenanceEventRecord next =  buildEvent(componentId2,ProvenanceEventType.ATTRIBUTES_MODIFIED,flowFileId);
        ProvenanceEventRecord last =  buildEvent(componentId3,ProvenanceEventType.DROP,flowFileId);
       addToFlow(start,throttle);
       addToFlow(next,throttle);
        addToFlow(last,throttle);

    }
    private boolean addToFlow(ProvenanceEventRecord event,boolean throttle){
        boolean process = true;
        if(throttle){
            process = ThrottleEvents.getInstance().isProcessEvent(event);
        }
        if(process) {
            processingQueue.add(new AbstractMap.SimpleEntry<Long, ProvenanceEventRecord>(eventId.incrementAndGet(), event));
        }
        else {
            //skipped
            skippedCount.incrementAndGet();
        }
        return process;
    }

    private ProvenanceEventRecord buildEvent(String componentId,ProvenanceEventType type, String flowfileId ){
        StandardProvenanceEventRecord.Builder  builder = new  StandardProvenanceEventRecord.Builder();
        ProvenanceEventRecord eventRecord = builder.setEventTime(System.currentTimeMillis())
            .setComponentId(componentId)
            .setComponentType(componentType)
            .setCurrentContentClaim("container", "section", "identifier", 0L, 0L)
            .setFlowFileUUID(flowfileId)
            .setEventType(type)
            .build();
        return eventRecord;
    }

    public Long getSkippedCount() {
        return skippedCount.get();
    }
}
