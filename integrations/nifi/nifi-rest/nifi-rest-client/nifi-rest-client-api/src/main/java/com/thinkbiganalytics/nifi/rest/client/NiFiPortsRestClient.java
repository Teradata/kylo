package com.thinkbiganalytics.nifi.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import org.apache.nifi.web.api.dto.PortDTO;

import javax.annotation.Nonnull;

/**
 * Exposes the NiFi Ports REST endpoint as a Java class.
 */
public interface NiFiPortsRestClient {

    /**
     * Updates an input port.
     *
     * @param processGroupId the process group id
     * @param inputPort      the input port
     * @return the updated input port
     * @throws NifiComponentNotFoundException if the process group does not exist
     */
    @Nonnull
    PortDTO updateInputPort(@Nonnull String processGroupId, @Nonnull PortDTO inputPort);

    /**
     * Updates an output port.
     *
     * @param processGroupId the process group id
     * @param outputPort     the output port
     * @return the updated output port
     * @throws NifiComponentNotFoundException if the process group does not exist
     */
    @Nonnull
    PortDTO updateOutputPort(@Nonnull String processGroupId, @Nonnull PortDTO outputPort);

    /**
     * Returns the port matching the portId
     * @param portId the port id
     * @return the port
     */
    PortDTO getInputPort(@Nonnull String portId);

    /**
     * Returns the port matching the portId
     * @param portId the port id
     * @return the port
     */
    PortDTO getOutputPort(@Nonnull String portId);
}
