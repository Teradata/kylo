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
import com.thinkbiganalytics.nifi.rest.client.AbstractNiFiProcessGroupsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiComponentState;
import com.thinkbiganalytics.nifi.rest.client.NiFiProcessGroupsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRemoteProcessGroupsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.RemoteProcessGroupDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.dto.flow.FlowDTO;
import org.apache.nifi.web.api.dto.status.ProcessGroupStatusDTO;
import org.apache.nifi.web.api.entity.ConnectionEntity;
import org.apache.nifi.web.api.entity.ConnectionsEntity;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.FlowEntity;
import org.apache.nifi.web.api.entity.FunnelEntity;
import org.apache.nifi.web.api.entity.InputPortsEntity;
import org.apache.nifi.web.api.entity.InstantiateTemplateRequestEntity;
import org.apache.nifi.web.api.entity.LabelEntity;
import org.apache.nifi.web.api.entity.OutputPortsEntity;
import org.apache.nifi.web.api.entity.PortEntity;
import org.apache.nifi.web.api.entity.ProcessGroupEntity;
import org.apache.nifi.web.api.entity.ProcessGroupFlowEntity;
import org.apache.nifi.web.api.entity.ProcessGroupsEntity;
import org.apache.nifi.web.api.entity.ProcessorEntity;
import org.apache.nifi.web.api.entity.RemoteProcessGroupEntity;
import org.apache.nifi.web.api.entity.ScheduleComponentsEntity;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;

/**
 * Implements a {@link NiFiRemoteProcessGroupsRestClient} for communicating with NiFi v1.0.
 */
public class NiFiRemoteProcessGroupsRestClientV1 implements NiFiRemoteProcessGroupsRestClient {

    /**
     * Base path for process group requests
     */
    private static final String BASE_PATH = "/remote-process-groups/";

    /**
     * REST client for communicating with NiFi
     */
    private final NiFiRestClientV1 client;

    /**
     * Constructs a {@code NiFiProcessGroupsRestClientV1} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiRemoteProcessGroupsRestClientV1(@Nonnull final NiFiRestClientV1 client) {
        this.client = client;
    }


    /**
     * Finds the remoteProcessGroup by its Id
     * @param processGroupId the process group id
     * @return
     */
    public Optional<RemoteProcessGroupDTO> findById(@Nonnull final String processGroupId) {
        return findEntityById(processGroupId).filter(processGroupEntity -> processGroupEntity != null)
            .map(RemoteProcessGroupEntity::getComponent);
    }


    /**
     * Gets a process group entity.
     *
     * @param processGroupId      the process group id
     * @return the remote process group entity, if found
     */
    @Nonnull
    public Optional<RemoteProcessGroupEntity> findEntityById(@Nonnull final String processGroupId) {
        try {
            return Optional.ofNullable(client.get(BASE_PATH + processGroupId, null, RemoteProcessGroupEntity.class));
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public RemoteProcessGroupDTO update(@Nonnull final RemoteProcessGroupDTO processGroup) {
        return findEntityById(processGroup.getId())
            .flatMap(current -> {
                final RemoteProcessGroupEntity entity = new RemoteProcessGroupEntity();
                entity.setComponent(processGroup);

                final RevisionDTO revision = new RevisionDTO();
                revision.setVersion(current.getRevision().getVersion());
                entity.setRevision(revision);

                try {
                    return Optional.of(client.put(BASE_PATH + processGroup.getId(), entity, RemoteProcessGroupEntity.class).getComponent());
                } catch (final NotFoundException e) {
                    return Optional.empty();
                }
            })
            .orElseThrow(() -> new NifiComponentNotFoundException(processGroup.getId(), NifiConstants.NIFI_COMPONENT_TYPE.REMOTE_PROCESS_GROUP, null));
    }


}
