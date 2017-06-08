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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */
public class KyloProvenanceProcessingQueue  {

    private static final Logger log = LoggerFactory.getLogger(KyloProvenanceProcessingQueue.class);

    LinkedBlockingQueue<Map.Entry<Long,ProvenanceEventRecord>> queue = new LinkedBlockingQueue();


    /**
     * Add an Event and its corresponding id to thte queue
     * @param eventId the event id
     * @param event the event
     */
    public void put(Long eventId, ProvenanceEventRecord event){
        queue.add(new AbstractMap.SimpleEntry<>(eventId,event));
    }

    /**
     * Drain the queue to a list
     * @return the list of id,event objects
     */
    public List<Map.Entry<Long,ProvenanceEventRecord>>  takeAll(){
        List<Map.Entry<Long,ProvenanceEventRecord>> events = new ArrayList();
        if(!queue.isEmpty()) {
            log.info("about to drain {} events", queue.size());
        }
      queue.drainTo(events);
        if(!events.isEmpty()) {
            log.info("successfully drained {} events", events.size());
        }
      return events;
    }

    /**
     * Take a subset of the elements off the queue
     * @param maxElements number of elements to drain
     * @return the list of id,event objects
     */
    public List<Map.Entry<Long,ProvenanceEventRecord>>  take(int maxElements){
        List<Map.Entry<Long,ProvenanceEventRecord>> events = new ArrayList();
        queue.drainTo(events, maxElements);
        return events;
    }

    /**
     * Take a subset of the elements off the queue
     * @param maxElements number of elements to drain
     * @return the list of id,event objects
     */
    public Map.Entry<Long,ProvenanceEventRecord>  take() {
        try {
            return queue.take();
        }catch (InterruptedException e){
            return null;
        }
    }

}
