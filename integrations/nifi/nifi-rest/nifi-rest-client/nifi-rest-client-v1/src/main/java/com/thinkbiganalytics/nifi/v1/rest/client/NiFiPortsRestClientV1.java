package com.thinkbiganalytics.nifi.v1.rest.client;

/*-
 * #%L
 * thinkbig-nifi-rest-client-v1
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
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.entity.PortEntity;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

/**
 * Implements a {@link NiFiPortsRestClient} for communicating with NiFi v1.0.
 */
public class NiFiPortsRestClientV1 implements NiFiPortsRestClient {

    /**
     * REST client for communicating with NiFi
     */
    private final NiFiRestClientV1 client;

    /**
     * Constructs a {@code NiFiPortsRestClientV1} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiPortsRestClientV1(@Nonnull final NiFiRestClientV1 client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public PortDTO updateInputPort(@Nonnull final String processGroupId, @Nonnull final PortDTO inputPort) {
        // Get revision
        final PortEntity current;
        try {
            current = client.get("/input-ports/" + inputPort.getId(), null, PortEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(inputPort.getId(), NifiConstants.NIFI_COMPONENT_TYPE.INPUT_PORT, e);
        }

        // Update input port
        final PortEntity entity = new PortEntity();
        entity.setComponent(inputPort);

        final RevisionDTO revision = new RevisionDTO();
        revision.setVersion(current.getRevision().getVersion());
        entity.setRevision(revision);

        try {
            return client.put("/input-ports/" + inputPort.getId(), entity, PortEntity.class).getComponent();
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(inputPort.getId(), NifiConstants.NIFI_COMPONENT_TYPE.INPUT_PORT, e);
        }
    }

    @Nonnull
    @Override
    public PortDTO updateOutputPort(@Nonnull final String processGroupId, @Nonnull final PortDTO outputPort) {
        // Get revision
        final PortEntity current;
        try {
            current = client.get("/output-ports/" + outputPort.getId(), null, PortEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(outputPort.getId(), NifiConstants.NIFI_COMPONENT_TYPE.OUTPUT_PORT, e);
        }

        // Update output port
        final PortEntity entity = new PortEntity();
        entity.setComponent(outputPort);

        final RevisionDTO revision = new RevisionDTO();
        revision.setVersion(current.getRevision().getVersion());
        entity.setRevision(revision);

        try {
            return client.put("/output-ports/" + outputPort.getId(), entity, PortEntity.class).getComponent();
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(outputPort.getId(), NifiConstants.NIFI_COMPONENT_TYPE.OUTPUT_PORT, e);
        }
    }


    @Override
    public PortDTO getInputPort(@Nonnull String portId) {
        final PortEntity current;
        try {
            current = client.get("/input-ports/" + portId, null, PortEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(portId, NifiConstants.NIFI_COMPONENT_TYPE.INPUT_PORT, e);
        }
        return current.getComponent();
    }

    @Override
    public PortDTO getOutputPort(@Nonnull String portId) {
        final PortEntity current;
        try {
            current = client.get("/output-ports/" + portId, null, PortEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(portId, NifiConstants.NIFI_COMPONENT_TYPE.OUTPUT_PORT, e);
        }
        return current.getComponent();
    }
}
