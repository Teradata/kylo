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

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.nifi.provenance.ProvenanceEventRecord;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by sr186054 on 5/26/17.
 */
public class KyloProvenanceProcessingQueue  {

    LinkedBlockingQueue<Map.Entry<Long,ProvenanceEventRecord>> queue = new LinkedBlockingQueue();


    public void put(Long eventId, ProvenanceEventRecord event){
        queue.add(new AbstractMap.SimpleEntry<>(eventId,event));
    }

    public List<Map.Entry<Long,ProvenanceEventRecord>>  takeAll(){
        List<Map.Entry<Long,ProvenanceEventRecord>> events = new ArrayList();
      queue.drainTo(events);
      return events;
    }

    public List<Map.Entry<Long,ProvenanceEventRecord>>  take(int maxElements){
        List<Map.Entry<Long,ProvenanceEventRecord>> events = new ArrayList();
        queue.drainTo(events, maxElements);
        return events;
    }

}
