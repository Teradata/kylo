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

import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventCollectorV2;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventObjectPool;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventRecordConverter;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.util.ProvenanceEventRecordMapEntryComparator;
import com.thinkbiganalytics.nifi.provenance.util.SpringApplicationContext;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by sr186054 on 5/26/17.
 */
public class KyloProvenanceEventConsumer implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(KyloProvenanceEventConsumer.class);
    private Long lastEventId;

    @Autowired
    KyloNiFiFlowCacheUpdater kyloNiFiFlowCacheUpdater;

    private ProvenanceEventObjectPool objectPool;

    private KyloProvenanceProcessingQueue processingQueue;

    public KyloProvenanceEventConsumer(KyloProvenanceProcessingQueue processingQueue) {
        this.processingQueue = processingQueue;


    }

    private List<Long> processTimes = new ArrayList<>();

    private List<Long> conversionTimes = new ArrayList<>();

    private List<Long> eventTimes = new ArrayList<>();

    private Long eventCount = 0L;

    private List<Long> sortTimes = new ArrayList<>();

    private List<Long> totalTimes = new ArrayList<>();

    private void resetTimes(){
        processTimes.clear();
        conversionTimes.clear();
        eventCount = 0L;
        sortTimes.clear();
        totalTimes.clear();
    }

    @Override
    public void run() {

        int partitionSize = 2000;

        while (true) {
            long startTime = System.currentTimeMillis();
            List<Map.Entry<Long,ProvenanceEventRecord>> events = processingQueue.takeAll();
            List<ProvenanceEventRecordDTO> pooledEvents = new ArrayList<>();
            if (!events.isEmpty()) {
                long startTime2 = System.currentTimeMillis();
                Collections.sort(events, new ProvenanceEventRecordMapEntryComparator());
                sortTimes.add((System.currentTimeMillis() - startTime2));
                log.info("Time to sort {} event entries {} ms ",events.size(),(System.currentTimeMillis() - startTime2));
                Lists.partition(events,partitionSize).stream().forEach(list -> {
                    ProvenanceEventRecordDTO event = null;
                    try {
                        for(Map.Entry<Long,ProvenanceEventRecord> entry : list) {
                            long time1 = System.currentTimeMillis();
                            event = ProvenanceEventRecordConverter.getPooledObject(getProvenanceEventObjectPool(), entry.getValue());
                            event.setEventId(entry.getKey());
                            conversionTimes.add(System.currentTimeMillis() - time1);

                            if (lastEventId == null || event.getEventId() != lastEventId) {
                                long time2 = System.currentTimeMillis();
                                getProvenanceEventCollector().process(event);
                                processTimes.add(System.currentTimeMillis() - time2);
                                eventTimes.add(System.currentTimeMillis() - time1);
                            }
                            lastEventId = event.getEventId();
                            eventCount++;
                        }
                        getProvenanceEventCollector().sendToJms();

                    }finally {
                        returnObjectToPool(event);
                    }

                });
                totalTimes.add(System.currentTimeMillis() - startTime);
                log.info("ProvenanceEventPool: Pool Stats: Created:[" + getProvenanceEventObjectPool().getCreatedCount() + "], Borrowed:[" + getProvenanceEventObjectPool().getBorrowedCount() + "]");
                log.info("Averages for {} events.  Sort: {}, conversion: {}, process: {}, event: {}, totalTime: {}, ",eventCount, (sortTimes.stream().mapToDouble(Long::doubleValue).sum()/eventCount),
                         conversionTimes.stream().mapToDouble(Long::doubleValue).average(),
                         processTimes.stream().mapToDouble(Long::doubleValue).average(),
                         eventTimes.stream().mapToDouble(Long::doubleValue).average(),
                         (totalTimes.stream().mapToDouble(Long::doubleValue).sum()/eventCount));

            }
        }
    }

    private void returnToObjectsPool(List<ProvenanceEventRecordDTO> events) {
        ProvenanceEventObjectPool pool = getProvenanceEventObjectPool();
        events.stream().forEach(dto ->  returnObjectToPool(dto) );

        log.info("ProvenanceEventPool: Pool Stats: Created:[" + pool.getCreatedCount() + "], Borrowed:[" + pool.getBorrowedCount() + "]");
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


    private void abortProcessing() {

    }


    private ProvenanceEventObjectPool getProvenanceEventObjectPool() {
        if(objectPool == null) {
            objectPool = SpringApplicationContext.getInstance().getBean(ProvenanceEventObjectPool.class);
        }
        return objectPool;
    }


    /**
     * The Spring managed bean to collect and process the event for Kylo
     */
    private ProvenanceEventCollectorV2 getProvenanceEventCollector() {
        return SpringApplicationContext.getInstance().getBean(ProvenanceEventCollectorV2.class);
    }


}
