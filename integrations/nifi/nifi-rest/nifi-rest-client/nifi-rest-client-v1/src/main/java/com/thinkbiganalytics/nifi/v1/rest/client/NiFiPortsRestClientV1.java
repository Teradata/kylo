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

import com.thinkbiganalytics.nifi.rest.client.AbstractNiFiProcessorsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiPortsRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.RevisionDTO;
import org.apache.nifi.web.api.entity.InputPortsEntity;
import org.apache.nifi.web.api.entity.PortEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

    private boolean isModified(PortDTO current, PortDTO port){
        return current == null || port.getId() == null || (!current.getId().equals(port.getId()) ||
            !current.getName().equals(port.getName())|| !current.getState().equals(port.getState()));
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
       if(current == null || (current != null && isModified(current.getComponent(),inputPort))) {

            boolean update  = true;
            //only mark input port as running if we have connections to it
           //NIFI bug
           if(StringUtils.isNotBlank(inputPort.getId())  && NifiProcessUtil.PROCESS_STATE.RUNNING.name().equals(inputPort.getState())) {
               Set<ConnectionDTO> connectionDTOS = client.connections().findConnectionsToEntity(processGroupId, inputPort.getId());
               if(connectionDTOS == null || connectionDTOS.isEmpty() || connectionDTOS.stream().noneMatch(connectionDTO -> connectionDTO.getSource().getId().equals(inputPort.getId()))){
                   log.warn("System will not start the input port [{}] [{}] in the process group [{}] since there are on upstream connections to it ",inputPort.getId(), inputPort.getName(), processGroupId);
                   update = false;
               }

           }
           if(update) {
               // Update input port
               final PortEntity entity = new PortEntity();
               entity.setComponent(inputPort);

               final RevisionDTO revision = new RevisionDTO();
               revision.setVersion(current.getRevision().getVersion());
               entity.setRevision(revision);

               //if trying to make a DISABLED port RUNNING you need to make it STOPPED first and then mark it as RUNNING
               if(current != null && current.getComponent().getState().equalsIgnoreCase(NifiProcessUtil.PROCESS_STATE.DISABLED.name()) && inputPort.getState().equalsIgnoreCase(NifiProcessUtil.PROCESS_STATE.RUNNING.name())){
                   //first need to make it ENABLED
                   inputPort.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());
                   PortDTO port = updateInputPort(processGroupId,inputPort);
                   inputPort.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
               }
               try {
                   return client.put("/input-ports/" + inputPort.getId(), entity, PortEntity.class).getComponent();
               } catch (final NotFoundException e) {
                   throw new NifiComponentNotFoundException(inputPort.getId(), NifiConstants.NIFI_COMPONENT_TYPE.INPUT_PORT, e);
               }
           }
           else {
               return inputPort;
           }


       }
       return inputPort;


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

    public List<PortDTO> findInputPorts(String parentGroupId) {

        InputPortsEntity inputPortsEntity = client.get("/process-groups/" + parentGroupId + "/input-ports", null, InputPortsEntity.class);
        if (inputPortsEntity != null && inputPortsEntity.getInputPorts() != null) {
            return inputPortsEntity.getInputPorts().stream().map(portEntity -> portEntity.getComponent()).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
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
        }
        catch (NotFoundException e) {
            throw new NifiComponentNotFoundException(portId, NifiConstants.NIFI_COMPONENT_TYPE.INPUT_PORT, e);
            }
        return null;
    }
}
