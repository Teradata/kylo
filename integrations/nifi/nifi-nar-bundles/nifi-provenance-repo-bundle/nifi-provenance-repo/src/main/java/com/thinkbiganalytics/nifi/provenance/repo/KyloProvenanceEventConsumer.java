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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventCollector;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventObjectPool;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventRecordConverter;
import com.thinkbiganalytics.nifi.provenance.config.NifiProvenanceConfig;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.util.ProvenanceEventRecordMapEntryComparator;
import com.thinkbiganalytics.nifi.provenance.util.SpringApplicationContext;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Take events off the queue and process/send to ops manager
 */
public class KyloProvenanceEventConsumer implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(KyloProvenanceEventConsumer.class);

    /**
     * An object pool to limit the overhead of creating objects from the many ProvenanceEventRecords
     */
    private ProvenanceEventObjectPool objectPool;

    /**
     * Shared queue the Provenance Events are written to which are pulled off this thread and processed
     */
   // private KyloProvenanceProcessingQueue processingQueue;

    private BlockingQueue<Map.Entry<Long,ProvenanceEventRecord>> processingQueue;


    public KyloProvenanceEventConsumer(BlockingQueue processingQueue) {
        this.processingQueue = processingQueue;
    }

    private AtomicLong eventsProcessed = new AtomicLong(0);

    /**
     * Partition the incoming evenets into groups of this size
     */
    private int partitionSize = 400;

    List<Long> processingTimes = new ArrayList<>();

    List<ProvenanceEventRecordDTO> processedEvents = new ArrayList<>();

    private Map.Entry<Long,ProvenanceEventRecord> take(){
        try {
            return processingQueue.take();
        }catch (InterruptedException e){
            return null;
        }
        }

    @Override
    public void run() {

        Map.Entry<Long,ProvenanceEventRecord> e = null;

        List<Map.Entry<Long,ProvenanceEventRecord>> events = new ArrayList<>(partitionSize);
        while((e = take())!=null)
        {
            processEvent(e);
            processingQueue.drainTo(events,partitionSize);
            if (!events.isEmpty()) {
                try {
                    for (Map.Entry<Long, ProvenanceEventRecord> entry : events) {
                        ProvenanceEventRecordDTO eventDto = processEvent(entry);
                        if(eventDto != null){
                            processedEvents.add(eventDto);
                        }
                    }
                    getProvenanceEventCollector().sendToJms();
                }finally{
                    returnToObjectsPool(processedEvents);
                }
                OptionalDouble avg = processingTimes.stream().mapToLong(t->t).average();
                log.info("Total Events Processed: {}, Avg Processing Time:{}/{}, ProvenanceEventPool: Pool Stats: Created:[{}] Borrowed:[{}]",eventsProcessed.get(),avg.getAsDouble(),processingTimes.size(), getProvenanceEventObjectPool().getCreatedCount(), getProvenanceEventObjectPool().getBorrowedCount() );
            }
            events.clear();
        }
    }

    private ProvenanceEventRecordDTO processEvent(Map.Entry<Long,ProvenanceEventRecord> entry) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        ProvenanceEventRecordDTO event = null;
            event = ProvenanceEventRecordConverter.getPooledObject(getProvenanceEventObjectPool(), entry.getValue());
            event.setEventId(entry.getKey());
            getProvenanceEventCollector().process(event);
            eventsProcessed.incrementAndGet();
            stopwatch.stop();
            processingTimes.add(stopwatch.elapsed(TimeUnit.MILLISECONDS));

        return event;
    }

    /**
     * Return the list of objects back to the pool
     * @param events
     */
    private void returnToObjectsPool(List<ProvenanceEventRecordDTO> events) {
        ProvenanceEventObjectPool pool = getProvenanceEventObjectPool();
        events.stream().forEach(dto ->  returnObjectToPool(dto) );
    }

    private void returnObjectToPool(ProvenanceEventRecordDTO dto){
        if (dto != null) {
            ProvenanceEventObjectPool pool = getProvenanceEventObjectPool();
            try {
                dto.reset();
                pool.returnObject(dto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ProvenanceEventObjectPool getProvenanceEventObjectPool() {
        if(objectPool == null) {
            objectPool = SpringApplicationContext.getInstance().getBean(ProvenanceEventObjectPool.class);
        }
        return objectPool;
    }

    private ProvenanceEventCollector getProvenanceEventCollector(){
        return SpringApplicationContext.getInstance().getBean(ProvenanceEventCollector.class);
    }

}
