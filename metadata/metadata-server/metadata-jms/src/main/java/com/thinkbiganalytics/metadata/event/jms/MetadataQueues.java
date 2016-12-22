package com.thinkbiganalytics.metadata.event.jms;

/**
 * JMS queues for communicating with NiFi.
 */
public interface MetadataQueues {
    /** Triggers a feed's cleanup flow */
    String CLEANUP_TRIGGER = "cleanupTrigger";

    /** Triggers a feed based on preconditions */
    String PRECONDITION_TRIGGER = "preconditionTrigger";
}
