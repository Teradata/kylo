package com.thinkbiganalytics.nifi.provenance;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 10/14/16.
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
