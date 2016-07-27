package com.thinkbiganalytics.metadata.event.jms;

/**
 * JMS topics for communicating with NiFi.
 */
public interface MetadataTopics {
    /** Triggers a feed's cleanup flow */
    String CLEANUP_TRIGGER = "cleanupTrigger";

    /** Triggers a feed based on preconditions */
    String PRECONDITION_TRIGGER = "preconditionTrigger";

    /** Indicates changes to a data source */
    String DATASOURCE_CHANGE = "datasourceChange";
}
