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

import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.layout.AlignProcessGroupComponents;
import com.thinkbiganalytics.nifi.rest.client.layout.ProcessGroupAndConnections;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiTemplateNameUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.status.ProcessGroupStatusDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This code will find all Process groups matching the Version name of  <Name> - {13 digit timestamp} and if the group doesn't have anything in its Queue, it will delete it from NiFi
 */
public class CleanupStaleFeedRevisions {

    private static final Logger log = LoggerFactory.getLogger(CleanupStaleFeedRevisions.class);
    LegacyNifiRestClient restClient;
    /**
     * the name of the parent group to look at or the word 'all' to access everything
     **/
    private String processGroupId;
    private Set<PortDTO> stoppedPorts = new HashSet<>();

    private Set<ProcessGroupDTO> deletedProcessGroups = new HashSet<>();
    private NiFiPropertyDescriptorTransform propertyDescriptorTransform;


    /**
     * Cleanup versioned process groups that are no longer active
     *
     * @param restClient                  the nifi rest client
     * @param processGroupId              a parent process group (i.e. a category) to look at, or the word 'all' to clean up everything
     * @param propertyDescriptorTransform the transformation bean
     */
    public CleanupStaleFeedRevisions(LegacyNifiRestClient restClient, String processGroupId, NiFiPropertyDescriptorTransform propertyDescriptorTransform) {
        this.processGroupId = processGroupId;
        this.restClient = restClient;
        this.propertyDescriptorTransform = propertyDescriptorTransform;
    }


    /**
     * Cleanup all versioned feed process groups
     * if the {@link #processGroupId} == 'all' then it will clean up everything, otherwise it will cleanup just the children under the {@link #processGroupId}
     */
    public void cleanup() {
        deletedProcessGroups.clear();
        Set<String> categoriesToCleanup = new HashSet<>();
        if ("all".equalsIgnoreCase(processGroupId)) {
            ProcessGroupDTO root = restClient.getNiFiRestClient().processGroups().findRoot();

            root.getContents().getProcessGroups().stream().forEach(categoryGroup -> {
                categoriesToCleanup.add(categoryGroup.getId());
            });
        } else {
            categoriesToCleanup.add(processGroupId);
        }
        categoriesToCleanup.stream().forEach(categoryProcessGroupId -> doCleanup(categoryProcessGroupId));
        log.info("Successfully Cleaned up versioned ProcessGroups, deleting {} groups ", deletedProcessGroups.size());


    }


    /**
     * Return the list of groups that were deleted as part of the {@link #cleanup()} activity
     *
     * @return the list of groups that were deleted as part of the {@link #cleanup()} activity
     */
    public Set<ProcessGroupDTO> getDeletedProcessGroups() {
        return deletedProcessGroups;
    }

    private void doCleanup(String processGroupId) {
        stoppedPorts.clear();
        AlignProcessGroupComponents alignProcessGroupComponents = new AlignProcessGroupComponents(restClient.getNiFiRestClient(), processGroupId);
        alignProcessGroupComponents.groupItems();
        final Map<String, ProcessGroupAndConnections> groups = alignProcessGroupComponents.getProcessGroupWithConnectionsMap();
        Set<ProcessGroupAndConnections> deletedItems = new HashSet<>();
        groups.values().stream()
            .filter(groupAndConnections -> NifiTemplateNameUtil.isVersionedProcessGroup(groupAndConnections.getProcessGroup().getName()))
            .filter(groupAndConnections -> canDelete(groupAndConnections.getProcessGroup()))
            .forEach(groupAndConnections -> {
                cleanup(groupAndConnections);
                deletedItems.add(groupAndConnections);
            });
        startPorts();
        //relayout the group
        if (!deletedItems.isEmpty()) {
            new AlignProcessGroupComponents(restClient.getNiFiRestClient(), processGroupId).autoLayout();
        }
    }

    private void cleanup(ProcessGroupAndConnections groupAndConnections) {
        //stop the  ports
        stopPorts(groupAndConnections.getPorts());
        groupAndConnections.getConnections().stream().forEach(connectionDTO -> restClient.deleteConnection(connectionDTO, false));
        log.info("About to delete {}", groupAndConnections.getProcessGroup().getName());
        restClient.deleteProcessGroup(groupAndConnections.getProcessGroup());
        deletedProcessGroups.add(groupAndConnections.getProcessGroup());

    }

    private void stopPorts(Set<PortDTO> ports) {
        if (!ports.isEmpty()) {
            ports.stream()
                .filter(portDTO -> !stoppedPorts.contains(portDTO))
                .forEach(portDTO1 -> {
                    if (isInputPort(portDTO1)) {
                        restClient.stopInputPort(portDTO1.getParentGroupId(), portDTO1.getId());
                        stoppedPorts.add(portDTO1);
                    } else {
                        restClient.stopOutputPort(portDTO1.getParentGroupId(), portDTO1.getId());
                        stoppedPorts.add(portDTO1);
                    }
                });
        }
    }

    private void startPorts() {
        Set<PortDTO> startedPorts = new HashSet<>();
        if (!stoppedPorts.isEmpty()) {
            stoppedPorts.stream()
                .forEach(portDTO1 -> {
                    if (isInputPort(portDTO1)) {
                        restClient.startInputPort(portDTO1.getParentGroupId(), portDTO1.getId());
                        startedPorts.add(portDTO1);
                    } else {
                        restClient.startOutputPort(portDTO1.getParentGroupId(), portDTO1.getId());
                        startedPorts.add(portDTO1);
                    }
                });
        }
        stoppedPorts.removeAll(startedPorts);
    }

    private boolean isInputPort(PortDTO portDTO) {
        return NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name().equalsIgnoreCase(portDTO.getType());
    }

    private boolean isOutputPort(PortDTO portDTO) {
        return NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name().equalsIgnoreCase(portDTO.getType());
    }


    private boolean canDelete(ProcessGroupDTO groupDTO) {

        Optional<ProcessGroupStatusDTO> statusDTO = restClient.getNiFiRestClient().processGroups().getStatus(groupDTO.getId());
        if (statusDTO.isPresent()) {
            return !hasItemsInQueue(statusDTO.get());
        } else {
            return false;
        }
    }

    private boolean hasItemsInQueue(ProcessGroupStatusDTO statusDTO) {
        String queuedCount = propertyDescriptorTransform.getQueuedCount(statusDTO);
        return StringUtils.isNotBlank(queuedCount) && !queuedCount.equalsIgnoreCase("0");
    }


}
