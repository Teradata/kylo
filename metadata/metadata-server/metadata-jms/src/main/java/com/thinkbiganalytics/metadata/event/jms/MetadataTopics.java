package com.thinkbiganalytics.metadata.event.jms;

/**
 * JMS topics for communicating with NiFi.
 */
public interface MetadataTopics {
    /** Indicates changes to a data source */
    String DATASOURCE_CHANGE = "datasourceChange";
}
