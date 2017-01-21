package com.thinkbiganalytics.nifi.rest.support;

/**
 * Defines NiFi enumerations.
 */
public class NifiConstants {

    /**
     * Indicates a type of NiFi port.
     */
    public enum NIFI_PORT_TYPE {
        OUTPUT_PORT, INPUT_PORT
    }

    /**
     * Indicates a type of NiFi processor.
     */
    public enum NIFI_PROCESSOR_TYPE {
        PROCESSOR
    }

    /**
     * Indicates a type of NiFi component.
     */
    public enum NIFI_COMPONENT_TYPE {
        OUTPUT_PORT, INPUT_PORT, PROCESSOR, PROCESS_GROUP, CONNECTION, TEMPLATE, CONTROLLER_SERVICE
    }
}
