package com.thinkbiganalytics.nifi.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-common-api
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

import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

/**
 * Thrown to indicate that the requested NiFi component does not exist.
 */
public class NifiComponentNotFoundException extends NifiClientRuntimeException {

    private String componentId;
    private NifiConstants.NIFI_COMPONENT_TYPE componentType;

    public NifiComponentNotFoundException(String componentId, NifiConstants.NIFI_COMPONENT_TYPE componentType, Throwable cause) {
        this("Unable to find NiFi component of type: " + componentType + " for: " + componentId, componentId, componentType, cause);
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
     *
     * @return the component id
     */
    public String getComponentId() {
        return componentId;
    }
}
