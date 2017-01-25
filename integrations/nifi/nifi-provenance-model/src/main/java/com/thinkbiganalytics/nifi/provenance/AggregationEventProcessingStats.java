package com.thinkbiganalytics.nifi.provenance;

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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Track event counts sent to the JMS queue
 */
public class AggregationEventProcessingStats {

    private static AtomicLong streamingEventsSentToJms = new AtomicLong(0L);

    private static AtomicLong batchEventsSentToJms = new AtomicLong(0L);


    public static Long addStreamingEvents(int num) {
        return streamingEventsSentToJms.addAndGet(new Long(num));
    }

    public static Long getStreamingEventsSent() {
        return streamingEventsSentToJms.get();
    }

    public static Long addBatchEvents(int num) {
        return batchEventsSentToJms.addAndGet(new Long(num));
    }

    public static Long getBatchEventsSent() {
        return batchEventsSentToJms.get();
    }

}
