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
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.dto.flow.FlowDTO;
import org.apache.nifi.web.api.dto.flow.ProcessGroupFlowDTO;
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
 * Implements a {@link NiFiProcessGroupsRestClient} for communicating with NiFi v1.0.
 */
public class NiFiProcessGroupsRestClientV1 extends AbstractNiFiProcessGroupsRestClient {

    /**
     * Base path for process group requests
     */
    private static final String BASE_PATH = "/process-groups/";

    /**
     * REST client for communicating with NiFi
     */
    private final NiFiRestClientV1 client;

    /**
     * Constructs a {@code NiFiProcessGroupsRestClientV1} with the specified NiFi REST client.
     *
     * @param client the REST client
     */
    public NiFiProcessGroupsRestClientV1(@Nonnull final NiFiRestClientV1 client) {
        this.client = client;
    }

    @Nonnull
    @Override
    public ProcessGroupDTO create(@Nonnull String parentProcessGroupId, @Nonnull String name) {
        final ProcessGroupEntity entity = new ProcessGroupEntity();

        final ProcessGroupDTO processGroup = new ProcessGroupDTO();
        processGroup.setName(name);
        entity.setComponent(processGroup);

        final RevisionDTO revision = new RevisionDTO();
        revision.setVersion(0L);
        entity.setRevision(revision);

        try {
            return client.post(BASE_PATH + parentProcessGroupId + "/process-groups", entity, ProcessGroupEntity.class).getComponent();
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(parentProcessGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public ConnectionDTO createConnection(@Nonnull final String processGroupId, @Nonnull final ConnectableDTO source, @Nonnull final ConnectableDTO dest) {
        final ConnectionEntity entity = new ConnectionEntity();

        final ConnectionDTO connection = new ConnectionDTO();
        connection.setDestination(dest);
        connection.setName(source.getName() + "-" + dest.getName());
        connection.setSource(source);
        entity.setComponent(connection);

        final RevisionDTO revision = new RevisionDTO();
        revision.setVersion(0L);
        entity.setRevision(revision);

        try {
            return client.post(BASE_PATH + processGroupId + "/connections", entity, ConnectionEntity.class).getComponent();
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public ControllerServiceDTO createControllerService(@Nonnull final String processGroupId, @Nonnull final ControllerServiceDTO controllerService) {
        final ControllerServiceEntity entity = new ControllerServiceEntity();
        entity.setComponent(controllerService);

        final RevisionDTO revision = new RevisionDTO();
        revision.setVersion(0L);
        entity.setRevision(revision);

        try {
            return client.post(BASE_PATH + processGroupId + "/controller-services", entity, ControllerServiceEntity.class).getComponent();
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public PortDTO createInputPort(@Nonnull final String processGroupId, @Nonnull final PortDTO inputPort) {
        final PortEntity entity = new PortEntity();
        entity.setComponent(inputPort);

        final RevisionDTO revision = new RevisionDTO();
        revision.setVersion(0L);
        entity.setRevision(revision);

        try {
            return client.post(BASE_PATH + processGroupId + "/input-ports", entity, PortEntity.class).getComponent();
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public PortDTO createOutputPort(@Nonnull final String processGroupId, @Nonnull final PortDTO outputPort) {
        final PortEntity entity = new PortEntity();
        entity.setComponent(outputPort);

        final RevisionDTO revision = new RevisionDTO();
        revision.setVersion(0L);
        entity.setRevision(revision);

        try {
            return client.post(BASE_PATH + processGroupId + "/output-ports", entity, PortEntity.class).getComponent();
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    //@Override
    public Set<ProcessGroupDTO> findAllx(@Nonnull final String parentGroupId) {
        try {
            return client.get(BASE_PATH + parentGroupId + "/process-groups", null, ProcessGroupsEntity.class)
                .getProcessGroups().stream()
                .map(ProcessGroupEntity::getComponent)
                .collect(Collectors.toSet());
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(parentGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public Set<ProcessGroupDTO> findAll(@Nonnull final String parentGroupId) {
        try {
            return client.get("/flow"+BASE_PATH + parentGroupId , null, ProcessGroupFlowEntity.class)
                .getProcessGroupFlow().getFlow()
                .getProcessGroups().stream()
                .map(ProcessGroupEntity::getComponent)
                .collect(Collectors.toSet());
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(parentGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public ProcessGroupFlowDTO flow(@Nonnull final String parentGroupId) {
        try {
            return client.get("/flow"+BASE_PATH + parentGroupId , null, ProcessGroupFlowEntity.class)
                .getProcessGroupFlow();
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(parentGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public Optional<ProcessGroupDTO> findById(@Nonnull final String processGroupId, final boolean recursive, final boolean verbose) {
        return findById(processGroupId, recursive, verbose, true);
    }

    public Optional<ProcessGroupDTO> findById(@Nonnull final String processGroupId, final boolean recursive, final boolean verbose, boolean logRestAccessErrors) {
        return findEntityById(processGroupId, logRestAccessErrors).filter(processGroupEntity -> processGroupEntity != null)
            .map(ProcessGroupEntity::getComponent)
            .map(processGroup -> {
                if (verbose) {
                    processGroup.setContents(getFlowSnippet(processGroupId, recursive));
                }
                return processGroup;
            });
    }

    /**
     * Gets a process group entity.
     *
     * @param processGroupId the process group id
     * @return the process group entity, if found
     */
    @Nonnull
    public Optional<ProcessGroupEntity> findEntityById(@Nonnull final String processGroupId) {
        return findEntityById(processGroupId, true);
    }

    /**
     * Gets a process group entity.
     *
     * @param processGroupId      the process group id
     * @param logRestAccessErrors true to log any REST exceptions.  false will not log exceptions
     * @return the process group entity, if found
     */
    @Nonnull
    public Optional<ProcessGroupEntity> findEntityById(@Nonnull final String processGroupId, boolean logRestAccessErrors) {
        try {
            return Optional.ofNullable(client.get(BASE_PATH + processGroupId, null, ProcessGroupEntity.class, logRestAccessErrors));
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }

    public Optional<ProcessGroupStatusDTO> getStatus(String processGroupId) {
        return Optional.ofNullable(findEntityById(processGroupId).map(processGroupEntity -> processGroupEntity.getStatus()).orElse(null));
    }


    /**
     * This is very slow once you have a lot of process groups
     * @param processGroupId
     * @return
     */
    @Nonnull
    public Set<ConnectionDTO> getConnectionsxx(@Nonnull final String processGroupId) {
        try {
            return client.get(BASE_PATH + processGroupId + "/connections", null, ConnectionsEntity.class)
                .getConnections().stream()
                .map(ConnectionEntity::getComponent)
                .collect(Collectors.toSet());
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public Set<ConnectionDTO> getConnections(@Nonnull final String processGroupId) {
        try {
            return client.get("/flow"+BASE_PATH + processGroupId , null, ProcessGroupFlowEntity.class)
                .getProcessGroupFlow().getFlow().getConnections().stream()
                .map(ConnectionEntity::getComponent)
                .collect(Collectors.toSet());
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public Set<ControllerServiceDTO> getControllerServices(@Nonnull final String processGroupId) {
        try {
            return client.get("/flow/process-groups/" + processGroupId + "/controller-services", null, ControllerServicesEntity.class)
                .getControllerServices().stream()
                .map(ControllerServiceEntity::getComponent)
                .collect(Collectors.toSet());
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public Set<PortDTO> getInputPorts(@Nonnull final String processGroupId) {
        try {
            return client.get(BASE_PATH + processGroupId + "/input-ports", null, InputPortsEntity.class)
                .getInputPorts().stream()
                .map(PortEntity::getComponent)
                .collect(Collectors.toSet());
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public Set<PortDTO> getOutputPorts(@Nonnull final String processGroupId) {
        try {
            return client.get(BASE_PATH + processGroupId + "/output-ports", null, OutputPortsEntity.class)
                .getOutputPorts().stream()
                .map(PortEntity::getComponent)
                .collect(Collectors.toSet());
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public FlowSnippetDTO instantiateTemplate(@Nonnull final String processGroupId, @Nonnull final String templateId) {
        final InstantiateTemplateRequestEntity entity = new InstantiateTemplateRequestEntity();
        entity.setOriginX(10.0);
        entity.setOriginY(10.0);
        entity.setTemplateId(templateId);

        try {
            final FlowEntity flow = client.post(BASE_PATH + processGroupId + "/template-instance", entity, FlowEntity.class);
            return toFlowSnippet(flow.getFlow(),true);
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        } catch (final ClientErrorException e) {
            final String msg = e.getResponse().readEntity(String.class);
            throw new NifiComponentNotFoundException("Unable to create Template instance for templateId: " + templateId + " under Process Group " + processGroupId + ". " + msg);
        }
    }

    @Override
    public void schedule(@Nonnull final String processGroupId, @Nonnull final String parentGroupId, @Nonnull final NiFiComponentState state) {
        final ScheduleComponentsEntity entity = new ScheduleComponentsEntity();
        entity.setId(processGroupId);
        entity.setState(state.toString());

        try {
            client.put("/flow/process-groups/" + processGroupId, entity, ScheduleComponentsEntity.class);
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    @Nonnull
    @Override
    public ProcessGroupDTO update(@Nonnull final ProcessGroupDTO processGroup) {
        return findEntityById(processGroup.getId())
            .flatMap(current -> {
                final ProcessGroupEntity entity = new ProcessGroupEntity();
                entity.setComponent(processGroup);

                final RevisionDTO revision = new RevisionDTO();
                revision.setVersion(current.getRevision().getVersion());
                entity.setRevision(revision);

                try {
                    return Optional.of(client.put(BASE_PATH + processGroup.getId(), entity, ProcessGroupEntity.class).getComponent());
                } catch (final NotFoundException e) {
                    return Optional.empty();
                }
            })
            .orElseThrow(() -> new NifiComponentNotFoundException(processGroup.getId(), NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, null));
    }

    @Nonnull
    @Override
    protected Optional<ProcessGroupDTO> doDelete(@Nonnull final ProcessGroupDTO processGroup) {
        return findEntityById(processGroup.getId())
            .flatMap(entity -> {
                final Long version = entity.getRevision().getVersion();
                try {
                    return Optional.of(client.delete(BASE_PATH + processGroup.getId(), ImmutableMap.of("version", version), ProcessGroupEntity.class).getComponent());
                } catch (final NotFoundException e) {
                    return Optional.empty();
                }
            });
    }


    /**
     * Fetches the flow snippet for the specified process group.
     *
     * @param processGroupId the process group id
     * @param recursive      {@code true} to include flows for child process groups, or {@code false} to leave child process group flows empty
     * @return the flow for the process group
     */
    @Nonnull
    private FlowSnippetDTO getFlowSnippet(@Nonnull final String processGroupId, final boolean recursive) {
        // Fetch the flow
        final FlowSnippetDTO snippet;
        try {
            snippet = toFlowSnippet(client.get("/flow/process-groups/" + processGroupId, null, ProcessGroupFlowEntity.class).getProcessGroupFlow().getFlow(),recursive);
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(processGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }



        // Return flow
        return snippet;
    }

    private Set<ProcessGroupEntity> findAllEntities(@Nonnull final String parentGroupId) {
        try {
            return client.get(BASE_PATH + parentGroupId + "/process-groups", null, ProcessGroupsEntity.class)
                .getProcessGroups();
        } catch (final NotFoundException e) {
            throw new NifiComponentNotFoundException(parentGroupId, NifiConstants.NIFI_COMPONENT_TYPE.PROCESS_GROUP, e);
        }
    }

    /**
     * Converts the specified flow to a flow snippet.
     *
     * @param flow the flow
     * @return the flow snippet
     */
    @Nonnull
    private FlowSnippetDTO toFlowSnippet(@Nonnull final FlowDTO flow, boolean recursive) {
        final FlowSnippetDTO snippet = new FlowSnippetDTO();
        snippet.setConnections(flow.getConnections().stream().map(ConnectionEntity::getComponent).collect(Collectors.toSet()));
        snippet.setControllerServices(Collections.emptySet());
        snippet.setFunnels(flow.getFunnels().stream().map(FunnelEntity::getComponent).collect(Collectors.toSet()));
        snippet.setInputPorts(flow.getInputPorts().stream().map(PortEntity::getComponent).collect(Collectors.toSet()));
        snippet.setLabels(flow.getLabels().stream().map(LabelEntity::getComponent).collect(Collectors.toSet()));
        snippet.setOutputPorts(flow.getOutputPorts().stream().map(PortEntity::getComponent).collect(Collectors.toSet()));
        snippet.setProcessGroups(flow.getProcessGroups().stream().map(ProcessGroupEntity::getComponent).collect(Collectors.toSet()));
        snippet.setProcessors(flow.getProcessors().stream().map(ProcessorEntity::getComponent).collect(Collectors.toSet()));
        snippet.setRemoteProcessGroups(flow.getRemoteProcessGroups().stream().map(RemoteProcessGroupEntity::getComponent).collect(Collectors.toSet()));

        // Add flow for child process groups
        if (recursive) {
            for (final ProcessGroupDTO processGroup : snippet.getProcessGroups()) {
                processGroup.setContents(getFlowSnippet(processGroup.getId(), true));
            }
        }


        return snippet;
    }
}
