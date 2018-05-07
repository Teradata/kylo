package com.thinkbiganalytics.feedmgr.service.template;
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
import com.thinkbiganalytics.feedmgr.nifi.TemplateConnectionUtil;
import com.thinkbiganalytics.feedmgr.rest.model.TemplateRemoteInputPortConnections;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.status.ProcessGroupStatusDTO;
import org.apache.nifi.web.api.dto.status.ProcessGroupStatusSnapshotDTO;
import org.apache.nifi.web.api.entity.ConnectionStatusEntity;
import org.apache.nifi.web.api.entity.RemoteProcessGroupStatusSnapshotEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

public class RemoteInputPortService {

    private static final Logger log = LoggerFactory.getLogger(RemoteInputPortService.class);

    @Inject
    private TemplateConnectionUtil templateConnectionUtil;

    @Inject
    private LegacyNifiRestClient nifiRestClient;

    private static class RemoteProcessGroupStatusHelper {

        public static Stream<RemoteProcessGroupStatusSnapshotEntity> flattened(ProcessGroupStatusSnapshotDTO processGroupStatusSnapshots) {

            Stream<RemoteProcessGroupStatusSnapshotEntity> s = processGroupStatusSnapshots.getRemoteProcessGroupStatusSnapshots().stream();

            return Stream.concat(
                s,
                processGroupStatusSnapshots.getProcessGroupStatusSnapshots().stream().flatMap(snap -> flattened(snap.getProcessGroupStatusSnapshot())));
        }

    }

    public static class RemoteInportPortRemovalData {

        private Set<String> inputPortNamesToRemove;
        private String templateName;
        private TemplateRemoteInputPortConnections connectionsToRemove;
        private List<String> progressErrorMessages;
        private List<String> severeErrorMessages;

        private List<PortDTO> deletedPorts;

        private List<ConnectionDTO> deletedConnections;

        private String successMessage;

        public RemoteInportPortRemovalData(Set<String> inputPortNamesToRemove) {
            this.inputPortNamesToRemove = inputPortNamesToRemove;
        }

        public TemplateRemoteInputPortConnections getConnectionsToRemove() {
            return connectionsToRemove;
        }

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public Set<String> getInputPortNamesToRemove() {
            return inputPortNamesToRemove;
        }

        public void setConnectionsToRemove(TemplateRemoteInputPortConnections connectionsToRemove) {
            this.connectionsToRemove = connectionsToRemove;
        }

        public void addErrorMessage(String msg, boolean addAsSevere) {
            getProgressErrorMessages().add(msg);
            if (addAsSevere) {
                getSevereErrorMessages().add(msg);
            }
        }

        public List<String> getProgressErrorMessages() {
            if (progressErrorMessages == null) {
                progressErrorMessages = new ArrayList<>();
            }
            return progressErrorMessages;
        }

        public List<String> getSevereErrorMessages() {
            if (severeErrorMessages == null) {
                severeErrorMessages = new ArrayList<>();
            }
            return severeErrorMessages;
        }


        public boolean isSuccess() {
            return (progressErrorMessages == null || progressErrorMessages.isEmpty()) && (severeErrorMessages == null || severeErrorMessages.isEmpty());
        }

        public void addDeletedPort(PortDTO portDTO) {
            getDeletedPorts().add(portDTO);
        }

        public void addDeletedConnection(ConnectionDTO connectionDTO) {
            getDeletedConnections().add(connectionDTO);
        }

        public List<PortDTO> getDeletedPorts() {
            if (deletedPorts == null) {
                deletedPorts = new ArrayList<>();
            }
            return deletedPorts;
        }

        public List<ConnectionDTO> getDeletedConnections() {
            if (deletedConnections == null) {
                deletedConnections = new ArrayList<>();
            }
            return deletedConnections;
        }

        public String getSuccessMessage() {
            return successMessage;
        }

        public void setSuccessMessage(String successMessage) {
            this.successMessage = successMessage;
        }
    }

    private Optional<TemplateRemoteInputPortConnections> getExistingRemoteProcessInputPortInformation(String templateName) {
        if (templateName == null) {
            return templateConnectionUtil.getAllReusableTemplateRemoteInputPorts();
        } else {
            return templateConnectionUtil.getRemoteInputPortsForReusableTemplate(templateName);
        }
    }

    public boolean removeRemoteInputPorts(RemoteInportPortRemovalData removalData) {

        Optional<TemplateRemoteInputPortConnections> remoteInputPortConnections = Optional.ofNullable(removalData.getConnectionsToRemove());
        if (!remoteInputPortConnections.isPresent()) {
            remoteInputPortConnections = getExistingRemoteProcessInputPortInformation(removalData.getTemplateName());
        }

        Set<String> inputPortNamesToRemove = removalData.getInputPortNamesToRemove();

        //Find the connections that match the input ports that are to be removed
        Set<ConnectionDTO>
            connectionsToRemove =
            remoteInputPortConnections.get().getExistingRemoteConnectionsToTemplate().stream().filter(connectionDTO -> inputPortNamesToRemove.contains(connectionDTO.getSource().getName())).collect(
                Collectors.toSet());

        Set<String> remoteInputPortIdsToRemove = connectionsToRemove.stream().map(connectionDTO -> connectionDTO.getSource().getId()).collect(Collectors.toSet());
        log.info("Removing input ports {}", inputPortNamesToRemove);

        Set<ConnectionDTO> connectionsWithQueue = new HashSet<>();
        //first validate the queues are empty ... if not warn user the following ports cant be deleted and rollback
        connectionsToRemove.stream().forEach(connection -> {
            Optional<ConnectionStatusEntity> connectionStatus = nifiRestClient.getNiFiRestClient().connections().getConnectionStatus(connection.getId());
            if (connectionStatus.isPresent() && connectionStatus.get().getConnectionStatus().getAggregateSnapshot().getFlowFilesQueued() > 0) {
                connectionsWithQueue.add(connection);
            }
        });

        if (!connectionsWithQueue.isEmpty()) {

            removalData.addErrorMessage(
                "Unable to remove inputPort and connection for :" + connectionsWithQueue.stream().map(c -> c.getSource().getName()).collect(Collectors.joining(",")) + ". The Queues are not empty." + (
                    removalData.getTemplateName() != null ? "Failure for template: " + removalData.getTemplateName() : ""), false);
            return false;
        } else {
            if (!remoteInputPortIdsToRemove.isEmpty()) {
                //verify we are allowed to delete this input port
                // if there are any remoteprocessgroups connected to this input port then we should not be allowed to delete it
                ProcessGroupStatusDTO flow = nifiRestClient.getNiFiRestClient().processGroups().flowStatus("root", true);

                boolean remoteProcessGroupsExist = flow.getAggregateSnapshot().getProcessGroupStatusSnapshots()
                    .stream()
                    .flatMap(s -> RemoteProcessGroupStatusHelper.flattened(s.getProcessGroupStatusSnapshot()))
                    .map(remoteProcessGroupStatusSnapshotEntity ->
                             nifiRestClient.getNiFiRestClient().remoteProcessGroups()
                                 .findById(
                                     remoteProcessGroupStatusSnapshotEntity.getId()))
                    .filter(Optional::isPresent).map(Optional::get)
                    .flatMap(remoteProcessGroupDTO -> remoteProcessGroupDTO.getContents().getInputPorts().stream())
                    .anyMatch(remoteProcessGroupPortDTO -> remoteInputPortIdsToRemove.contains(remoteProcessGroupPortDTO.getId()));
                if (remoteProcessGroupsExist) {
                    String msg = "Unable to remove inputPort  There are feed flows with RemotProcessGroups that are linked to one or more of the input ports you are trying to remove"
                                 + (removalData.getTemplateName() != null ? ", for template: " + removalData.getTemplateName() : "");

                    removalData.addErrorMessage(msg, true);

                    //importTemplate.getTemplateResults().addError(NifiError.SEVERITY.WARN, msg, "");
                    //remoteProcessGroupOption.getErrorMessages().add(msg);
                    return false;
                } else {
                    connectionsToRemove.stream().forEach(connection -> {
                        nifiRestClient.deleteConnection(connection, false);
                        removalData.addDeletedConnection(connection);
                        try {
                            PortDTO deletedPort = nifiRestClient.getNiFiRestClient().ports().deleteInputPort(connection.getSource().getId());
                            if (deletedPort != null) {
                                removalData.addDeletedPort(deletedPort);
                            }
                        } catch (NifiComponentNotFoundException e) {
                            //this is ok to catch as its deleted already
                        }
                    });
                }
            }

            String successMsg = "Removed inputPort and connection for :" + connectionsToRemove.stream().map(c -> c.getSource().getName()).collect(
                Collectors.joining(",")) + " for template: " + (removalData.getTemplateName() != null ? removalData.getTemplateName() : "");

        }
        return true;
    }

}
