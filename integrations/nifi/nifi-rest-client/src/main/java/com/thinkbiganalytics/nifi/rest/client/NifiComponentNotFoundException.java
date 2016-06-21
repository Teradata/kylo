package com.thinkbiganalytics.nifi.rest.client;

import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

/**
 * Created by sr186054 on 6/15/16.
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


    public String getComponentId() {
        return componentId;
    }


}
