package com.thinkbiganalytics.nifi.rest.client;

import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

/**
 * Thrown to indicate that the requested NiFi component does not exist.
 */
public class NifiComponentNotFoundException extends NifiClientRuntimeException {

    private String componentId;
    private NifiConstants.NIFI_COMPONENT_TYPE componentType;

    public NifiComponentNotFoundException(String componentId, NifiConstants.NIFI_COMPONENT_TYPE componentType, Throwable cause) {
        this("Unable to find Nifi Compoent of type: " + componentType + " for: " + componentId, componentId, componentType, cause);
    }

    public NifiComponentNotFoundException(String message) {
        super(message);
    }

    public NifiComponentNotFoundException(String message, String componentId, NifiConstants.NIFI_COMPONENT_TYPE componentType, Throwable cause) {
        super(message, cause);
        this.componentId = componentId;
        this.componentType = componentType;

    }

    public NifiComponentNotFoundException(String message, String componentId) {
        super(message);
        this.componentId = componentId;

    }

    /**
     * Gets the id of the component that was requested.
     * @return the component id
     */
    public String getComponentId() {
        return componentId;
    }
}
