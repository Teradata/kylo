package com.thinkbiganalytics.feedmgr.nifi;

import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.layout.AlignProcessGroupComponents;
import com.thinkbiganalytics.nifi.rest.client.layout.ProcessGroupAndConnections;
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
 * This code will find all Process groups matching the Version name of  <Name> - {13 digit timestamp} and if the group doesnt have anything in its Queue, it will remove it
 * Created by sr186054 on 12/15/16.
 */
public class CleanupStaleFeedRevisions {

    private static final Logger log = LoggerFactory.getLogger(CleanupStaleFeedRevisions.class);

    private String processGroupId;
    LegacyNifiRestClient restClient;

    private Set<PortDTO> stoppedPorts = new HashSet<>();

    private Set<ProcessGroupDTO> deletedProcessGroups = new HashSet<>();


    public CleanupStaleFeedRevisions(LegacyNifiRestClient restClient, String processGroupId) {
        this.processGroupId = processGroupId;
        this.restClient = restClient;
    }


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
        if(!deletedItems.isEmpty()) {
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
        return StringUtils.isNotBlank(statusDTO.getAggregateSnapshot().getQueuedCount()) && !statusDTO.getAggregateSnapshot().getQueuedCount().equalsIgnoreCase("0") ;
    }


}
