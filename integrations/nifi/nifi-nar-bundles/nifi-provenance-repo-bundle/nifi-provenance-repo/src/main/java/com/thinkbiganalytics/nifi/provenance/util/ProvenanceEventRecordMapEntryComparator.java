package com.thinkbiganalytics.nifi.provenance.util;

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

import java.util.Comparator;
import java.util.Map;

/**
 * Comparator sorting events by eventId
 */
public class ProvenanceEventRecordMapEntryComparator implements Comparator<Map.Entry<Long,ProvenanceEventRecord>> {


    public int compare(Map.Entry<Long,ProvenanceEventRecord> o1, Map.Entry<Long,ProvenanceEventRecord> o2) {
        if (o1 == null && o1 == null) {
            return 0;
        } else if (o1 != null && o2 == null) {
            return -1;
        } else if (o1 == null && o2 != null) {
            return 1;
        } else {
            return new Long(o1.getKey()).compareTo(new Long(o2.getKey()));
        }
    }
}