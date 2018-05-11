package com.thinkbiganalytics.feedmgr.nifi;
/*-
 * #%L
 * thinkbig-feed-manager-controller
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

import com.google.common.util.concurrent.Uninterruptibles;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiRemoteProcessGroupUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.RemoteProcessGroupDTO;
import org.apache.nifi.web.api.dto.RemoteProcessGroupPortDTO;
import org.apache.nifi.web.api.entity.ControllerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class RemoteProcessGroupValidator {

    private static final Logger log = LoggerFactory.getLogger(RemoteProcessGroupValidator.class);

    private List<NifiProperty> modifiedProperties;
    private LegacyNifiRestClient restClient;
    private RemoteProcessGroupValidation validation;
    private TemplateConnectionUtil templateConnectionUtil;

    public RemoteProcessGroupValidator(LegacyNifiRestClient restClient, TemplateConnectionUtil templateConnectionUtil, List<NifiProperty> modifiedProperties) {
        this.restClient = restClient;
        this.templateConnectionUtil = templateConnectionUtil;
        this.modifiedProperties = modifiedProperties;
        validation = new RemoteProcessGroupValidation();
    }


    /**
     * 1. get the flow for the feed process group
     * 2. if Connections.component.destination ==  REMOTE_INPUT_PORT
     * 2a. Match destination id to remoteProcessGroup.component.contents.inputPorts
     * 2b. if remoteProcessGroup.component.contents.inputPorts.exists == false, attempt to find one in the (remoteProcessGroup.component.contents.inputPorts) with the same name that has 'exists' == true
     * 2c. if found, make thecd cd  new connection (update the connections.component.destination == the good input port
     */
    public RemoteProcessGroupValidation validateAndFixRemoteProcessGroups(ProcessGroupDTO feedProcessGroup) {
        validation = new RemoteProcessGroupValidation();
        Map<String, RemoteProcessGroupDTO> remoteProcessGroupMap = new HashMap<>();
        Map<String, EnhancedRemoteProcessGroupPortDTO> remoteInputPortsById = new HashMap<>();
        Map<String, EnhancedRemoteProcessGroupPortDTO> remoteInputPortsByName = new HashMap<>();

        feedProcessGroup.getContents().getRemoteProcessGroups().stream().forEach(remoteProcessGroup -> {
            remoteProcessGroupMap.putIfAbsent(remoteProcessGroup.getId(), remoteProcessGroup);
            remoteProcessGroup.getContents().getInputPorts().stream().forEach(inputPort -> {
                remoteInputPortsById.putIfAbsent(inputPort.getId(), new EnhancedRemoteProcessGroupPortDTO(inputPort, remoteProcessGroup));
                remoteInputPortsByName.putIfAbsent(inputPort.getName(), new EnhancedRemoteProcessGroupPortDTO(inputPort, remoteProcessGroup));
            });
        });

        //Map of the old connection id to this connectoin object
        Map<String, UpdatedConnection> remoteProcessGroupConnections =
            feedProcessGroup.getContents().getConnections().stream()
                .filter(connectionDTO -> connectionDTO.getDestination().getType().equalsIgnoreCase(NifiConstants.REMOTE_INPUT_PORT))
                .map(conn -> {
                    UpdatedConnection connection = new UpdatedConnection(conn, remoteInputPortsById.get(conn.getDestination().getId()));
                    return connection;
                })
                .collect(Collectors.toMap(conn -> conn.getOldConnection().getId(), conn -> conn));

        //if the user changed the targetUri  or targetUris then we need to:
        // 1. create a new remote process group with the same parameters as this one
        // 2. update the targeturis
        // 3. reset the connection to this process group id
        // 4. notify the NiFiFlowCache of the update (or it might happe implicitly)

        checkAndCreateNewRemoteProcessGroup(remoteProcessGroupConnections, remoteProcessGroupMap);

        if (!remoteProcessGroupConnections.isEmpty() && validation.isValid()) {

            Collection<String> authIssues = new HashSet<String>();
            for (UpdatedConnection updatedConnection : remoteProcessGroupConnections.values()) {
                if (updatedConnection.getRemoteProcessGroupDTO().getRemoteProcessGroup().getAuthorizationIssues() != null && !updatedConnection.getRemoteProcessGroupDTO().getRemoteProcessGroup()
                    .getAuthorizationIssues().isEmpty()) {
                    authIssues.addAll(updatedConnection.getRemoteProcessGroupDTO().getRemoteProcessGroup().getAuthorizationIssues());
                }
            }

            if (!authIssues.isEmpty()) {
                //invalidate the group
                remoteInputPortsByName.values().stream().forEach(enhancedRemoteProcessGroupPortDTO -> enhancedRemoteProcessGroupPortDTO.invalidate(authIssues));
            }

            validateConnectionData(remoteProcessGroupConnections, remoteInputPortsByName);
            updateConnectionsInNiFi(remoteProcessGroupConnections.values());

            remoteProcessGroupConnections.values().stream()
                .filter(conn -> conn.isNewlyCreatedConnection())
                .forEach(remoteProcessGroupConnection -> {
                    //reset the connections on the entity feedProcessGroup to be this one instead of the other one
                    //find the connectionsToUpdate.getOldConnection.getId() that matches this connection
                    //remove it
                    //add in the connectionsToUpdate.getNewConnection()
                    feedProcessGroup.getContents().getConnections().remove(remoteProcessGroupConnection.getOldConnection());
                    feedProcessGroup.getContents().getConnections().add(remoteProcessGroupConnection.getNewConnection());

                });
            //delete the entity.getRemoteProcessGroups matching the id
            //add in the newly created remote process group
            List<RemoteProcessGroupDTO>
                removedGroups =
                feedProcessGroup.getContents().getRemoteProcessGroups().stream().filter(rpg -> validation.getRemovedRemoteProcessGroupIds().contains(rpg.getId())).collect(Collectors.toList());
            feedProcessGroup.getContents().getRemoteProcessGroups().removeAll(removedGroups);
            feedProcessGroup.getContents().getRemoteProcessGroups().addAll(validation.getCreatedRemoteProcessGroups());

        }
        return validation;
    }

    private void validateConnectionData(Map<String, UpdatedConnection> remoteProcessGroupConnections, Map<String, EnhancedRemoteProcessGroupPortDTO> remoteInputPortsByName) {
        ControllerEntity details = restClient.getNiFiRestClient().siteToSite().details();
        Map<String, PortDTO> siteToSitePortByNameMap = details.getController().getInputPorts().stream()
            .collect(Collectors.toMap(port -> port.getName(), port -> port));

        remoteProcessGroupConnections.values().stream().forEach(conn -> {
            String destinationName = conn.getOldConnection().getDestination().getName();
            EnhancedRemoteProcessGroupPortDTO remoteProcessGroupPortDTO = conn.getRemoteProcessGroupDTO();

            if (remoteProcessGroupPortDTO != null) {
                if (!siteToSitePortByNameMap.containsKey(remoteProcessGroupPortDTO.getName())) {
                    validation.addNonExistentConnection(conn.getOldConnection());
                }
            } else {
                validation.addNonExistentConnection(conn.getOldConnection());
            }
        });
    }


    private void updateConnectionsInNiFi(Collection<UpdatedConnection> updatedConnections) {
        updatedConnections.stream().forEach(remoteProcessGroupConnection -> {
            updateRemoteConnection(remoteProcessGroupConnection, validation, 0);
        });
    }


    private RemoteProcessGroupDTO copyRemoteProcessGroup(RemoteProcessGroupDTO remoteProcessGroupDTO) {
        RemoteProcessGroupDTO copy = new RemoteProcessGroupDTO();
        try {
            BeanUtils.copyProperties(copy, remoteProcessGroupDTO);
        } catch (Exception e) {

        }
        return copy;
    }

    /**
     * If we are modifying targetUris we need to delete the RemoteProcessGroup (and connections) and recreate the Remote Process Group with the new target Uri
     *
     * @param remoteProcessGroupConnections the connection map connecting to the remote process groups
     * @param remoteProcessGroupMap         the map of remote process groups
     */
    private void checkAndCreateNewRemoteProcessGroup(Map<String, UpdatedConnection> remoteProcessGroupConnections, Map<String, RemoteProcessGroupDTO> remoteProcessGroupMap) {
        Map<String, List<NifiProperty>> updatedRemoteProcessGroupProperties = findPropertiesRequiringNewRemoteProcessGroupGroupedByRemoteProcessGroupid();

        if (!updatedRemoteProcessGroupProperties.isEmpty()) {

            for (Map.Entry<String, List<NifiProperty>> entry : updatedRemoteProcessGroupProperties.entrySet()) {
                String id = entry.getKey();
                List<NifiProperty> properties = entry.getValue();
                RemoteProcessGroupDTO existingRemoteProcessGroup = remoteProcessGroupMap.get(id);

                //delete the connections to the rpg

                remoteProcessGroupConnections.values().stream().forEach(c -> {
                    restClient.deleteConnection(c.getOldConnection(), true);
                });

                //delete the rpg
                restClient.getNiFiRestClient().remoteProcessGroups().delete(id);
                validation.addDeletedRemoteProcessGroupId(id);
                RemoteProcessGroupDTO newRemoteProcessGroupDTO = copyRemoteProcessGroup(existingRemoteProcessGroup);

                //update the properties with the modified properties
                NifiRemoteProcessGroupUtil.updateRemoteProcessGroup(newRemoteProcessGroupDTO, properties);
                newRemoteProcessGroupDTO.setId(null);
                Optional<RemoteProcessGroupDTO> newRemoteProcessGroup = restClient.getNiFiRestClient().remoteProcessGroups().create(newRemoteProcessGroupDTO);
                if (newRemoteProcessGroup.isPresent()) {
                    validation.addNewRemoteProcessGroup(newRemoteProcessGroup.get());
                    //make the new connections
                    remoteProcessGroupConnections.values().stream().forEach(c -> {
                        c.getNewConnection().setId(null);
                        c.getNewConnection().getDestination().setId(null);
                        c.getNewConnection().getDestination().setGroupId(newRemoteProcessGroup.get().getId());
                        c.updateRemoteProcessGroup(newRemoteProcessGroup.get());
                    });

                } else {
                    //fail validation
                    validation.setUnableToUpdateNewInstance(true);
                    break;
                }
            }
        }

    }

    private Map<String, List<NifiProperty>> findPropertiesRequiringNewRemoteProcessGroupGroupedByRemoteProcessGroupid() {
        Map<String, List<NifiProperty>> updatedRemoteProcessGroupProperties = new HashMap<>();
        modifiedProperties.stream()
            .filter(p -> p.getProcessorType().equalsIgnoreCase("REMOTE_PROCESS_GROUP")
                         && (p.getKey().equalsIgnoreCase("targetUri")
                             || p.getKey().equalsIgnoreCase("targetUris")))
            .forEach(p -> {
                updatedRemoteProcessGroupProperties.computeIfAbsent(p.getProcessorId(), prop -> new ArrayList<>()).add(p);
            });
        return updatedRemoteProcessGroupProperties;
    }

    /**
     * When updating the connections to remote process groups the system needs to detect if the RemoteProcessGroup is valid (has no issues connecting to the targetURI prior to making the connection)
     * This call will try and sleep and retry if the remoteProcessGroup has connection issues
     *
     * @param remoteProcessGroupConnectionDTO the connection to update
     * @param validation                      the holder of validation success/failures
     * @param retryCount                      the number of retries already attempted for this connection
     * @return true if successful, false if not.  The Validation object will also be populated with the validation information
     */
    private boolean updateRemoteConnection(UpdatedConnection updatedConnection, RemoteProcessGroupValidation validation, int retryCount) {
        //ensure we are not attempting to authorize
        //TODO pull timeouts to configurable parameters
        boolean success = false;
        final int sleepTimeMillis = templateConnectionUtil.getRemoteProcessGroupSleepTime();
        final int maxAttempts = templateConnectionUtil.getRemoteProcessGroupMaxAttempts();
        ConnectionDTO connectionDTO = updatedConnection.getNewConnection();
        //fetch RPG
        //http://localhost:8079/nifi-api/remote-process-groups/4d883118-0163-1000-9638-372a2ff95da7
        //look in contents/input-ports
        //get the id of that input port (it will be different than the on on the root
        //set the destination to that id
        if (updatedConnection.getRemoteProcessGroupDTO().hasRemoteProcessGroupAuthorizationIssues() || updatedConnection.getRemoteProcessGroupDTO().getRemoteProcessGroup().getInputPortCount() == 0) {
            //wait
            log.info("Authorization issue found when attempting to update Remote Process Group Port connection for {}.  Retry Attempt: {} ", connectionDTO.getDestination().getName(), retryCount);
            if (retryCount <= maxAttempts) {
                Uninterruptibles.sleepUninterruptibly(sleepTimeMillis, TimeUnit.MILLISECONDS);
                Optional<RemoteProcessGroupDTO>
                    remoteProcessGroupDTO =
                    restClient.getNiFiRestClient().remoteProcessGroups().findById(updatedConnection.getRemoteProcessGroupDTO().getRemoteProcessGroup().getId());
                if (remoteProcessGroupDTO.isPresent()) {
                    updatedConnection.updateRemoteProcessGroup(remoteProcessGroupDTO.get());
                    retryCount++;
                    return updateRemoteConnection(updatedConnection, validation, retryCount);
                } else {
                    validation.addInvalidConnection(connectionDTO);
                    success = false;
                }
            } else {
                validation.addInvalidConnection(connectionDTO);
                success = false;
            }
        } else if (connectionDTO.getId() == null) {
            try {
                ConnectionDTO
                    connection = new ConnectionDTO();
                connection.setSource(connectionDTO.getSource());
                connection.setDestination(connectionDTO.getDestination());
                connection.setParentGroupId(connectionDTO.getParentGroupId());

                connection.setSelectedRelationships(new HashSet<>());
                connection.getSelectedRelationships().add("success");
                connection
                    .setName(StringUtils.isNotBlank(connectionDTO.getName()) ? connectionDTO.getName() : (connectionDTO.getSource().getName() + " - " + connectionDTO.getDestination().getName()));
                connection = restClient.getNiFiRestClient().processGroups().createConnection(connection);
                if (connection != null) {
                    updatedConnection.setNewConnection(connection);
                    updatedConnection.setUpdated(true);
                    success = true;
                } else {
                    success = false;
                }
            } catch (Exception e) {
                log.info("Error found attempting to create the new connection to the Remote Process Group for {} in Parent Process Group: {}.  Retry Attempt: {} ",
                         connectionDTO.getDestination().getName(), connectionDTO.getParentGroupId(), retryCount);

                if (retryCount <= maxAttempts) {
                    Uninterruptibles.sleepUninterruptibly(sleepTimeMillis, TimeUnit.MILLISECONDS);
                    retryCount++;
                    return updateRemoteConnection(updatedConnection, validation, retryCount);
                } else {
                    //unable to create new connection
                    validation.addInvalidConnection(connectionDTO);
                    success = false;
                }
            }

        } else {
            log.info("Updating Remote Process Group Port connection for {} ", connectionDTO.getDestination().getName());
            try {
                Optional<ConnectionDTO> connection = restClient.getNiFiRestClient().connections().update(connectionDTO);
                if (connection.isPresent()) {
                    validation.addUpdatedConnection(connection.get());
                    success = true;
                } else {
                    validation.addInvalidConnection(connectionDTO);
                    success = false;
                }
            } catch (Exception e) {
                validation.addInvalidConnection(connectionDTO);
                success = false;
            }
        }
        return success;

    }


    public static class RemoteProcessGroupValidation {

        /**
         * Connections successfully updated
         */
        private List<ConnectionDTO> updatedConnections;
        /**
         * Connections that errored out duing update
         */
        private List<ConnectionDTO> invalidConnections;
        /**
         * Connections that dont exist and could not find a viable remote input port
         */
        private List<ConnectionDTO> nonExistentPortConnections;

        private boolean unableToUpdateNewInstance;

        private Set<String> removedRemoteProcessGroupIds = new HashSet<>();
        private Set<RemoteProcessGroupDTO> createdRemoteProcessGroups = new HashSet<>();

        public void addUpdatedConnection(ConnectionDTO connectionDTO) {
            if (updatedConnections == null) {
                updatedConnections = new ArrayList<>();
            }
            updatedConnections.add(connectionDTO);
        }


        public void addInvalidConnection(ConnectionDTO connectionDTO) {
            if (invalidConnections == null) {
                invalidConnections = new ArrayList<>();
            }
            invalidConnections.add(connectionDTO);
        }

        public void addNonExistentConnection(ConnectionDTO connectionDTO) {
            if (nonExistentPortConnections == null) {
                nonExistentPortConnections = new ArrayList<>();
            }
            nonExistentPortConnections.add(connectionDTO);
        }

        public void setUnableToUpdateNewInstance(boolean unableToUpdateNewInstance) {
            this.unableToUpdateNewInstance = unableToUpdateNewInstance;
        }

        public boolean isUnableToUpdateNewInstance() {
            return unableToUpdateNewInstance;
        }

        public void addNewRemoteProcessGroup(RemoteProcessGroupDTO remoteProcessGroupDTO) {
            createdRemoteProcessGroups.add(remoteProcessGroupDTO);
        }

        public void addDeletedRemoteProcessGroupId(String remoteProcessGroupId) {
            removedRemoteProcessGroupIds.add(remoteProcessGroupId);
        }

        public Set<String> getRemovedRemoteProcessGroupIds() {
            return removedRemoteProcessGroupIds;
        }

        public Set<RemoteProcessGroupDTO> getCreatedRemoteProcessGroups() {
            return createdRemoteProcessGroups;
        }

        public boolean isValid() {
            return !unableToUpdateNewInstance && isValid(invalidConnections) && isValid(nonExistentPortConnections);
        }

        private boolean isValid(List list) {
            return list == null || (list != null && list.isEmpty());
        }

        @Nullable
        public List<ConnectionDTO> getUpdatedConnections() {
            return updatedConnections;
        }

        @Nullable
        public List<ConnectionDTO> getInvalidConnections() {
            return invalidConnections;
        }

        @Nullable
        public List<ConnectionDTO> getNonExistentPortConnections() {
            return nonExistentPortConnections;
        }

        public List<ConnectionDTO> getAllInvalidConnections() {
            List<ConnectionDTO> all = new ArrayList<>();
            if (invalidConnections != null) {
                all.addAll(invalidConnections);
            }
            if (nonExistentPortConnections != null) {
                all.addAll(nonExistentPortConnections);
            }
            return all;
        }

    }

    private static class EnhancedRemoteProcessGroupPortDTO {

        private static RemoteProcessGroupPortDTO UNDEFINED_PORT = new RemoteProcessGroupPortDTO();

        static {
            UNDEFINED_PORT.setId("UNDEFINED");
        }

        private RemoteProcessGroupPortDTO port;
        private RemoteProcessGroupDTO remoteProcessGroup;

        public EnhancedRemoteProcessGroupPortDTO(RemoteProcessGroupPortDTO port, RemoteProcessGroupDTO remoteProcessGroup) {
            this.port = port;
            this.remoteProcessGroup = remoteProcessGroup;
        }

        public EnhancedRemoteProcessGroupPortDTO(RemoteProcessGroupDTO remoteProcessGroup) {
            this.port = UNDEFINED_PORT;
            this.remoteProcessGroup = remoteProcessGroup;
        }

        public boolean isPortUndefined() {
            return this.port.equals(UNDEFINED_PORT);
        }

        public void setPort(RemoteProcessGroupPortDTO port) {
            this.port = port;
        }

        public RemoteProcessGroupPortDTO getPort() {
            return port;
        }

        public RemoteProcessGroupDTO getRemoteProcessGroup() {
            return remoteProcessGroup;
        }

        public String getId() {
            return port.getId();
        }

        public String getName() {
            return port.getName();
        }

        public boolean hasRemoteProcessGroupAuthorizationIssues() {
            return remoteProcessGroup != null && remoteProcessGroup.getAuthorizationIssues() != null && !remoteProcessGroup.getAuthorizationIssues().isEmpty();
        }

        public void updateRemoteProcessGroupDTO(RemoteProcessGroupDTO remoteProcessGroupDTO) {
            if (remoteProcessGroupDTO != null) {
                this.remoteProcessGroup = remoteProcessGroupDTO;
            }
        }

        public void invalidate(Collection<String> authIssues) {
            getRemoteProcessGroup().setAuthorizationIssues(authIssues);
        }

    }


    private class UpdatedConnection {

        private ConnectionDTO oldConnection;
        private ConnectionDTO newConnection;
        private EnhancedRemoteProcessGroupPortDTO remoteProcessGroupDTO;
        private boolean updated;

        public boolean isUpdated() {
            return updated;
        }

        public void setUpdated(boolean updated) {
            this.updated = updated;
        }

        public boolean isNewlyCreatedConnection() {
            return newConnection != null && newConnection.getId() != null && !newConnection.getId().equalsIgnoreCase(oldConnection.getId());
        }

        public UpdatedConnection(ConnectionDTO oldConnection, EnhancedRemoteProcessGroupPortDTO remoteProcessGroupDTO) {
            this.oldConnection = oldConnection;
            this.remoteProcessGroupDTO = remoteProcessGroupDTO;
            newConnectionFromOld();
        }

        public ConnectionDTO getOldConnection() {
            return oldConnection;
        }

        public void setOldConnection(ConnectionDTO oldConnection) {
            this.oldConnection = oldConnection;
        }

        public ConnectionDTO getNewConnection() {
            return newConnection;
        }

        public void setNewConnection(ConnectionDTO newConnection) {
            this.newConnection = newConnection;
        }

        private ConnectableDTO copyyConnectable(ConnectableDTO connectableDTO) {
            ConnectableDTO connectable = new ConnectableDTO();
            connectable.setId(connectableDTO.getId());
            connectable.setGroupId(connectableDTO.getGroupId());
            connectable.setType(connectableDTO.getType());
            connectable.setName(connectableDTO.getName());
            connectable.setTransmitting(connectableDTO.getTransmitting());
            return connectable;
        }

        public ConnectionDTO newConnectionFromOld() {
            ConnectionDTO newConnection = new ConnectionDTO();
            newConnection.setId(oldConnection.getId());
            newConnection.setName(oldConnection.getName());
            newConnection.setParentGroupId(oldConnection.getParentGroupId());
            newConnection.setSource(copyyConnectable(oldConnection.getSource()));
            newConnection.setDestination(copyyConnectable(oldConnection.getDestination()));
            newConnection.setSelectedRelationships(oldConnection.getSelectedRelationships());
            this.newConnection = newConnection;
            return newConnection;
        }

        public EnhancedRemoteProcessGroupPortDTO getRemoteProcessGroupDTO() {
            return remoteProcessGroupDTO;
        }


        public void updateRemoteProcessGroup(RemoteProcessGroupDTO remoteProcessGroupDTO) {
            this.remoteProcessGroupDTO.updateRemoteProcessGroupDTO(remoteProcessGroupDTO);
            if (remoteProcessGroupDTO.getInputPortCount() > 0) {
                RemoteProcessGroupPortDTO
                    newPort =
                    remoteProcessGroupDTO.getContents().getInputPorts().stream().filter(port -> port.getName().equalsIgnoreCase(this.getOldConnection().getDestination().getName())).findFirst()
                        .orElse(null);
                if (newPort != null) {
                    getNewConnection().getDestination().setId(newPort.getId());
                    getNewConnection().getDestination().setGroupId(remoteProcessGroupDTO.getId());
                    this.remoteProcessGroupDTO.setPort(newPort);
                }
            }
        }
    }

}
