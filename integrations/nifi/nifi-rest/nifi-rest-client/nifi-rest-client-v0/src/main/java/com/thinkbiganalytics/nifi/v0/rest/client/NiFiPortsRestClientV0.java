package com.thinkbiganalytics.nifi.v0.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-v0
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

import com.thinkbiganalytics.nifi.rest.client.NiFiPortsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.entity.InputPortEntity;
import org.apache.nifi.web.api.entity.OutputPortEntity;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

/**
 * Implements a {@link NiFiPortsRestClient} for communicating with NiFi v0.6.
 */
public class NiFiPortsRestClientV0 implements NiFiPortsRestClient {

    /** REST client for communicating with NiFi */
    private final NiFiRestClientV0 client;

    /**
     * Constructs a {@code NiFiPortsRestClientV0} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiPortsRestClientV0(@Nonnull final NiFiRestClientV0 client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public PortDTO updateInputPort(@Nonnull final String processGroupId, @Nonnull final PortDTO inputPort) {
        final InputPortEntity entity = new InputPortEntity();
        entity.setInputPort(inputPort);

        try {
            return client.put("/controller/process-groups/" + processGroupId + "/input-ports/" + inputPort.getId(), entity, InputPortEntity.class).getInputPort();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public PortDTO updateOutputPort(@Nonnull final String processGroupId, @Nonnull final PortDTO outputPort) {
        final OutputPortEntity entity = new OutputPortEntity();
        entity.setOutputPort(outputPort);

        try {
            return client.put("/controller/process-groups/" + processGroupId + "/output-ports/" + outputPort.getId(), entity, OutputPortEntity.class).getOutputPort();
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }
}
