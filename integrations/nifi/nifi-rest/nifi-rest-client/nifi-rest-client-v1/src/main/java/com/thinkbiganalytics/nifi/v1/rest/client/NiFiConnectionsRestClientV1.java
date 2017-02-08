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

import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.nifi.rest.client.AbstractNiFiConnectionsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiConnectionsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.DropRequestDTO;
import org.apache.nifi.web.api.entity.ConnectionEntity;
import org.apache.nifi.web.api.entity.DropRequestEntity;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

/**
 * Implements a {@link NiFiConnectionsRestClient} for communicating with NiFi v1.0.
 */
public class NiFiConnectionsRestClientV1 extends AbstractNiFiConnectionsRestClient {

    /**
     * Path to a connection entity
     */
    private static final String CONNECTION_PATH = "/connections/";

    /**
     * Path to a FlowFile queue entity
     */
    private static final String QUEUE_PATH = "/flowfile-queues/";

    /**
     * REST client for communicating with NiFi
     */
    private final NiFiRestClientV1 client;

    /**
     * Constructs a {@code NiFiConnectionsRestClientV1} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiConnectionsRestClientV1(@Nonnull final NiFiRestClientV1 client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public Optional<ConnectionDTO> delete(@Nonnull final String processGroupId, @Nonnull final String connectionId) {
        return findEntityById(connectionId)
            .flatMap(connection -> {
                final Long version = connection.getRevision().getVersion();
                try {
                    return Optional.of(client.delete(CONNECTION_PATH + connectionId, ImmutableMap.of("version", version), ConnectionEntity.class).getComponent());
                } catch (final NotFoundException e) {
                    return Optional.empty();
                }
            });
    }

    @Nonnull
    @Override
    protected DropRequestDTO createDropRequest(@Nonnull final String processGroupId, @Nonnull final String connectionId) {
        try {
            return client.post(QUEUE_PATH + connectionId + "/drop-requests", null, DropRequestEntity.class).getDropRequest();
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(connectionId, NifiConstants.NIFI_COMPONENT_TYPE.CONNECTION, e);
        }
    }

    @Nonnull
    @Override
    protected Optional<DropRequestDTO> deleteDropRequest(@Nonnull final String processGroupId, @Nonnull final String connectionId, @Nonnull final String dropRequestId) {
        try {
            return Optional.of(client.delete(QUEUE_PATH + connectionId + "/drop-requests/" + dropRequestId, null, DropRequestEntity.class).getDropRequest());
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    protected Optional<DropRequestDTO> getDropRequest(@Nonnull final String processGroupId, @Nonnull final String connectionId, @Nonnull final String dropRequestId) {
        try {
            return Optional.of(client.get(QUEUE_PATH + connectionId + "/drop-requests/" + dropRequestId, null, DropRequestEntity.class).getDropRequest());
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets a connection entity.
     *
     * @param connectionId the connection id
     * @return the connection entity, if found
     */
    @Nonnull
    private Optional<ConnectionEntity> findEntityById(@Nonnull final String connectionId) {
        try {
            return Optional.of(client.get(CONNECTION_PATH + connectionId, null, ConnectionEntity.class));
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }
}
