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

import java.util.List;

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


    /**
     * Return a List of all input ports for the parent group id
     * @param parentGroupId the  parent group id to search for the input ports
     * @return a list of the input ports under the parent group id
     */
    List<PortDTO> findInputPorts(String parentGroupId);

    /**
     * Deletes an input port with a given id
     * @param portId the port to delete
     * @return the port object that has been deleted
     */
    PortDTO deleteInputPort(String portId);
}
