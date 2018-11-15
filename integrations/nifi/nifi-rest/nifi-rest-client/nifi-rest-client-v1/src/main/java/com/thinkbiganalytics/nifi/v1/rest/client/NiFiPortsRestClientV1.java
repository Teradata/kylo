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
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.entity.InputPortsEntity;
import org.apache.nifi.web.api.entity.OutputPortsEntity;
import org.apache.nifi.web.api.entity.PortEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

/**
 * Implements a {@link NiFiPortsRestClient} for communicating with NiFi v1.0.
 */
public class NiFiPortsRestClientV1 implements NiFiPortsRestClient {

    private static final Logger log = LoggerFactory.getLogger(NiFiPortsRestClientV1.class);
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

    private boolean isModified(PortDTO current, PortDTO port) {
        return current == null || port.getId() == null || (!current.getId().equals(port.getId()) ||
                                                           !current.getName().equals(port.getName()) ||
                                                           !current.getState().equals(port.getState()) ||
                                                           positionsDiffer(current,port));
    }

    private boolean positionsDiffer(PortDTO current, PortDTO port){
        if(current.getPosition() != null && port.getPosition() == null){
            return true;
        } else  if(current.getPosition() == null && port.getPosition() != null){
            return true;
        }
        else {
            return (current.getPosition().getX() != null && port.getPosition().getX() != null
                   && current.getPosition().getX().doubleValue() != port.getPosition().getX().doubleValue()) ||
                   (current.getPosition().getY() != null && port.getPosition().getY() != null
                    && current.getPosition().getY().doubleValue() != port.getPosition().getY().doubleValue());
        }
    }

    @Nonnull
    @Override
    public PortDTO updateInputPort(@Nonnull final String processGroupId, @Nonnull final PortDTO inputPort) {
        PortEntity entity = updateInputPortEntity(processGroupId, inputPort, 0);
        return entity != null ? entity.getComponent() : inputPort;
    }

    private PortEntity updateInputPortEntity(@Nonnull final String processGroupId, @Nonnull final PortDTO inputPort, Integer retryAttempt) {
        // Get revision
        PortEntity current;
        try {
            current = client.get("/input-ports/" + inputPort.getId(), null, PortEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(inputPort.getId(), NifiConstants.NIFI_COMPONENT_TYPE.INPUT_PORT, e);
        }
        if (current == null || (current != null && isModified(current.getComponent(), inputPort))) {

            boolean update = true;

            //if we are trying to do anything but disable the port and the port is currently invalid prevent it from updating.
            if (current != null && current.getStatus().getRunStatus().equalsIgnoreCase("invalid") && !inputPort.getState().equalsIgnoreCase(NifiProcessUtil.PROCESS_STATE.DISABLED.name())) {
                update = false;
            } else if (StringUtils.isNotBlank(inputPort.getId()) && NifiProcessUtil.PROCESS_STATE.RUNNING.name().equals(inputPort.getState())) {
                //only mark input port as running if we have connections to it
                //NIFI bug
                Set<ConnectionDTO> connectionDTOS = client.connections().findConnectionsToEntity(processGroupId, inputPort.getId());
                //find this ports processgroup parent
                Optional<ProcessGroupDTO> group = client.processGroups().findById(processGroupId, false, false);
                if(group.isPresent() && StringUtils.isNotBlank(group.get().getParentGroupId())){
                    Set<ConnectionDTO> upstreamConnections = client.connections().findConnectionsToEntity(group.get().getParentGroupId(), inputPort.getId());
                    if(upstreamConnections != null && !upstreamConnections.isEmpty() ) {
                    if(connectionDTOS == null){
                        connectionDTOS = new HashSet<>();
                    }
                        connectionDTOS = Stream.concat(connectionDTOS.stream(), upstreamConnections.stream()).collect(Collectors.toSet());

                    }
                }

                boolean isRootProcessGroup = client.processGroups().isRoot(processGroupId);
                //always update root input portsgroups as they will be needed to be running if used by Remote Process Groups
                if (!isRootProcessGroup && (connectionDTOS == null || connectionDTOS.isEmpty() || connectionDTOS.stream().noneMatch(connectionDTO -> connectionDTO.getDestination() != null && connectionDTO.getDestination().getId().equals(inputPort.getId())))) {
                    log.warn("System will not start the input port [{}] [{}] in the process group [{}] since there are no upstream connections to it ", inputPort.getId(), inputPort.getName(),
                             processGroupId);
                    update = false;
                }

            }
            if (update) {
                if(StringUtils.isNotBlank(inputPort.getState())) {
                    //if trying to make a DISABLED port RUNNING you need to make it STOPPED first and then mark it as RUNNING
                    if (current != null && current.getComponent().getState().equalsIgnoreCase(NifiProcessUtil.PROCESS_STATE.DISABLED.name()) && inputPort.getState()
                        .equalsIgnoreCase(NifiProcessUtil.PROCESS_STATE.RUNNING.name())) {
                        //first need to make it ENABLED
                        inputPort.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
                        current = updateInputPortEntity(processGroupId, inputPort, retryAttempt);
                        inputPort.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
                    }

                    //if trying to make a RUNNING port DISABLED you need to make it STOPPED first and then mark it as DISABLED
                    if (current != null && current.getComponent().getState().equalsIgnoreCase(NifiProcessUtil.PROCESS_STATE.RUNNING.name()) && inputPort.getState()
                        .equalsIgnoreCase(NifiProcessUtil.PROCESS_STATE.DISABLED.name())) {
                        //first need to make it ENABLED
                        inputPort.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
                        current = updateInputPortEntity(processGroupId, inputPort, retryAttempt);
                        inputPort.setState(NifiProcessUtil.PROCESS_STATE.DISABLED.name());
                    }
                }
                // Update input port
                final PortEntity entity = new PortEntity();
                entity.setComponent(inputPort);

                final RevisionDTO revision = new RevisionDTO();
                revision.setVersion(current.getRevision().getVersion());
                entity.setRevision(revision);

                try {
                    return client.put("/input-ports/" + inputPort.getId(), entity, PortEntity.class);
                } catch (final NotFoundException e) {
                    throw new NifiComponentNotFoundException(inputPort.getId(), NifiConstants.NIFI_COMPONENT_TYPE.INPUT_PORT, e);
                } catch (Exception e) {
                    if (retryAttempt < 3) {
                        retryAttempt++;
                        log.info("An exception occurred attempting to update input port {}.  Retrying update {}/3", inputPort.getId(), retryAttempt);
                        delay(500L);
                        return updateInputPortEntity(processGroupId, inputPort, retryAttempt);
                    } else {
                        log.info("An exception occurred attempting to update input port {}. Max retries of 3 reached.", inputPort.getId(), e);
                        throw e;
                    }
                }
            }
        }
        if (current == null) {
            PortEntity entity = new PortEntity();
            entity.setComponent(inputPort);
            entity.setPortType(inputPort.getType());
            return entity;
        } else {
            return current;
        }

    }

    @Nonnull
    @Override
    public PortDTO updateOutputPort(@Nonnull final String processGroupId, @Nonnull final PortDTO outputPort) {
        PortEntity entity = updateOutputPortEntity(processGroupId, outputPort, 0);
        return entity != null ? entity.getComponent() : outputPort;
    }

    @Nonnull
    private PortEntity updateOutputPortEntity(@Nonnull final String processGroupId, @Nonnull final PortDTO outputPort, Integer retryAttempt) {
        // Get revision
        PortEntity current;
        try {
            current = client.get("/output-ports/" + outputPort.getId(), null, PortEntity.class);
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(outputPort.getId(), NifiConstants.NIFI_COMPONENT_TYPE.OUTPUT_PORT, e);
        }
        PortDTO currentPort = current != null ? current.getComponent() : null;

        boolean update = true;
        //if we are trying to do anything but disable the port and the port is currently invalid prevent it from updating.
        if (current != null && current.getStatus().getRunStatus().equalsIgnoreCase("invalid") && !outputPort.getState().equalsIgnoreCase(NifiProcessUtil.PROCESS_STATE.DISABLED.name())) {
            update = false;
        } else if (StringUtils.isNotBlank(outputPort.getId()) && NifiProcessUtil.PROCESS_STATE.RUNNING.name().equals(outputPort.getState())) {
            //only mark port as running if we have connections to it
            //NIFI bug
            Set<ConnectionDTO> connectionDTOS = client.connections().findConnectionsToEntity(processGroupId, outputPort.getId());
            if (connectionDTOS == null ||
                connectionDTOS.isEmpty() ||
                connectionDTOS.stream().noneMatch(connectionDTO -> (connectionDTO.getSource() != null && connectionDTO.getSource().getId().equals(outputPort.getId())) ||
                                                                   (connectionDTO.getDestination() != null && connectionDTO.getDestination().getId().equalsIgnoreCase(outputPort.getId())))) {
                log.warn("System will not start the output port [{}] [{}] in the process group [{}] since there are no connections to it ", outputPort.getId(), outputPort.getName(), processGroupId);
                update = false;
            }

        }
        if (update) {
            if(StringUtils.isNotBlank(outputPort.getState())) {
                //if trying to make a DISABLED port RUNNING you need to make it STOPPED first and then mark it as RUNNING
                if (current != null && current.getComponent().getState().equalsIgnoreCase(NifiProcessUtil.PROCESS_STATE.DISABLED.name()) && outputPort.getState()
                    .equalsIgnoreCase(NifiProcessUtil.PROCESS_STATE.RUNNING.name())) {
                    //first need to make it ENABLED
                    outputPort.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
                    current = updateOutputPortEntity(processGroupId, outputPort, retryAttempt);
                    delay(500L);
                    outputPort.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
                }

                //if trying to make a RUNNING port DISABLED you need to make it STOPPED first and then mark it as DISABLED
                if (current != null && current.getComponent().getState().equalsIgnoreCase(NifiProcessUtil.PROCESS_STATE.RUNNING.name()) && outputPort.getState()
                    .equalsIgnoreCase(NifiProcessUtil.PROCESS_STATE.DISABLED.name())) {
                    //first need to make it ENABLED
                    outputPort.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
                    current = updateOutputPortEntity(processGroupId, outputPort, retryAttempt);
                    delay(500L);
                    outputPort.setState(NifiProcessUtil.PROCESS_STATE.DISABLED.name());
                }
            }

            // Update output port
            final PortEntity entity = new PortEntity();
            entity.setComponent(outputPort);

            final RevisionDTO revision = new RevisionDTO();
            revision.setVersion(current.getRevision().getVersion());
            entity.setRevision(revision);

            try {
                return client.put("/output-ports/" + outputPort.getId(), entity, PortEntity.class);
            } catch (final NotFoundException e) {
                throw new NifiComponentNotFoundException(outputPort.getId(), NifiConstants.NIFI_COMPONENT_TYPE.OUTPUT_PORT, e);
            } catch (Exception e) {
                if (retryAttempt < 3) {
                    retryAttempt++;
                    log.info("An exception occurred attempting to update output port {}.  Retrying update {}/3", outputPort.getId(), retryAttempt);
                    delay(500L);
                    return updateOutputPortEntity(processGroupId, outputPort, retryAttempt);
                } else {
                    log.info("An exception occurred attempting to update output port {}. Max retries of 3 reached.", outputPort.getId(), e);
                    throw e;
                }
            }
        }
        if (current == null) {
            PortEntity entity = new PortEntity();
            entity.setComponent(outputPort);
            entity.setPortType(outputPort.getType());
            return entity;
        } else {
            return current;
        }
    }

    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Do nothing
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

    public List<PortDTO> findInputPorts(String parentGroupId) {

        InputPortsEntity inputPortsEntity = client.get("/process-groups/" + parentGroupId + "/input-ports", null, InputPortsEntity.class);
        if (inputPortsEntity != null && inputPortsEntity.getInputPorts() != null) {
            return inputPortsEntity.getInputPorts().stream().map(portEntity -> portEntity.getComponent()).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public List<PortDTO> findOutputPorts(String parentGroupId) {

        OutputPortsEntity outputPortsEntity = client.get("/process-groups/" + parentGroupId + "/output-ports", null, OutputPortsEntity.class);
        if (outputPortsEntity != null && outputPortsEntity.getOutputPorts() != null) {
            return outputPortsEntity.getOutputPorts().stream().map(portEntity -> portEntity.getComponent()).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public List<PortDTO> findPorts(String parentGroupId) {

        List<PortDTO> inputPorts = findInputPorts(parentGroupId);
        List<PortDTO> outputPorts = findOutputPorts(parentGroupId);
        return Stream.concat(inputPorts.stream(), outputPorts.stream()).collect(Collectors.toList());
    }

    @Override
    public PortDTO deleteInputPort(@Nonnull String portId) {
        try {
            PortEntity current = client.get("/input-ports/" + portId, null, PortEntity.class);
            if (current != null) {
                Map<String, Object> params = new HashMap<>();
                params.put("version", current.getRevision().getVersion());
                params.put("clientId", current.getRevision().getClientId());
                PortEntity inputPortsEntity = client.delete("/input-ports/" + portId, params, PortEntity.class);
                if (inputPortsEntity != null) {
                    return inputPortsEntity.getComponent();
                }
            }
        } catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(portId, NifiConstants.NIFI_COMPONENT_TYPE.INPUT_PORT, e);
        }
        return null;
    }
}
