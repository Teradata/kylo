package com.thinkbiganalytics.nifi.rest.client;

/**
 * Operating state of a NiFi component.
 */
public enum NiFiComponentState {

    /** The component is schedule to execute */
    RUNNING,

    /** The component will not be executed */
    STOPPED
}
