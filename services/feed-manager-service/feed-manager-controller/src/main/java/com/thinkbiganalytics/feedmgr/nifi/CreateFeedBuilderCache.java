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


import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;

import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

/**
 * Cache used to help speed up creating the feed.
 *
 * @see CreateFeedBuilder
 */
public class CreateFeedBuilderCache {


    @Inject
    private LegacyNifiRestClient restClient;

    private ProcessGroupDTO rootProcessGroup;

    private ProcessGroupDTO reusableTemplateCategory;

    private String reusableTemplateCategoryName = TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME;

    private Map<String, PortDTO> reusableTemplateCategoryInputPortsByName = new ConcurrentHashMap<>();

    //Make a evictingQueue  to reduce size??
    private Map<String, Map<String, PortDTO>> categoryProcessGroupIdToOutputPortByName = new ConcurrentHashMap<>();

    //Make a evictingQueue  to reduce size??
    private Map<String, Set<ConnectionDTO>> processGroupConnections = new ConcurrentHashMap<>();


    public CreateFeedBuilderCache() {
    }


    /**
     * Returns the root process group (non recursive call)
     */
    public ProcessGroupDTO getRootProcessGroup() {
        if (rootProcessGroup == null) {
            rootProcessGroup = restClient.getNiFiRestClient().processGroups().findById("root", false, false).orElseThrow(IllegalStateException::new);
        }
        return rootProcessGroup;
    }


    /**
     * returns the 'reusable_templates' process group
     */
    public ProcessGroupDTO getReusableTemplateCategoryProcessGroup() {
        if (reusableTemplateCategory == null) {
            reusableTemplateCategory = restClient.getProcessGroupByName("root", reusableTemplateCategoryName);
        }
        return reusableTemplateCategory;
    }

    public void setRestClient(LegacyNifiRestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * return the matching inputport in the 'reusable_templates' process group
     */
    public PortDTO getReusableTemplateInputPort(String inputPortName) {
        if (reusableTemplateCategoryInputPortsByName.containsKey(inputPortName)) {
            return reusableTemplateCategoryInputPortsByName.get(inputPortName);
        } else {
            ProcessGroupDTO reusableTemplateCategoryGroupId = getReusableTemplateCategoryProcessGroup();
            Set<PortDTO> inputPortsEntity = restClient.getNiFiRestClient().processGroups().getInputPorts(reusableTemplateCategoryGroupId.getId());
            if (inputPortsEntity != null) {
                inputPortsEntity.stream().forEach(inputPort -> reusableTemplateCategoryInputPortsByName.putIfAbsent(inputPort.getName(), inputPort));
                PortDTO inputPort = NifiConnectionUtil.findPortMatchingName(inputPortsEntity, inputPortName);
                return inputPort;
            }
        }
        return null;


    }

    /**
     * finds the matching outputport for the category looking by the port name
     */
    public PortDTO getCategoryOutputPort(String categoryProcessGroupId, String outputPortName) {

        if (!categoryProcessGroupIdToOutputPortByName.containsKey(categoryProcessGroupId)) {
            categoryProcessGroupIdToOutputPortByName.put(categoryProcessGroupId, new ConcurrentHashMap<>());
        }

        PortDTO outputPort = categoryProcessGroupIdToOutputPortByName.get(categoryProcessGroupId).get(outputPortName);
        if (outputPort == null) {
            Set<PortDTO> outputPorts = restClient.getNiFiRestClient().processGroups().getOutputPorts(categoryProcessGroupId);
            if (outputPorts != null) {
                outputPorts.stream().forEach(port -> categoryProcessGroupIdToOutputPortByName.get(categoryProcessGroupId).putIfAbsent(port.getName(), port));
                outputPort =
                    NifiConnectionUtil.findPortMatchingName(outputPorts, outputPortName);
            }
        }
        return outputPort;
    }

    public void addCategoryOutputPort(String categoryProcessGroupId, PortDTO portDTO) {
        categoryProcessGroupIdToOutputPortByName.putIfAbsent(categoryProcessGroupId, new ConcurrentHashMap<>()).put(portDTO.getName(), portDTO);
    }

    /**
     * @param processGroupId    (i.e. category process group)
     * @param sourcePortId      (feed output port)
     * @param destinationPortId (category output port)
     */
    public ConnectionDTO getConnection(String processGroupId, String sourcePortId, String destinationPortId) {

        ConnectionDTO
            connection = null;

        Set<ConnectionDTO> connectionDTOS = processGroupConnections.get(processGroupId);

        if (connectionDTOS == null) {
            //find all connections in the category
            Set<ConnectionDTO> connectionsEntity = restClient.getNiFiRestClient().processGroups().getConnections(processGroupId);
            if (connectionsEntity != null) {
                processGroupConnections.put(processGroupId, connectionsEntity);
                connectionDTOS = connectionsEntity;
            }
        }

        if (connectionDTOS != null) {

            connection =
                NifiConnectionUtil.findConnection(connectionDTOS, sourcePortId,
                                                  destinationPortId);

        }
        return connection;
    }

    public void addConnection(String processGroupId, ConnectionDTO connectionDTO) {
        processGroupConnections.putIfAbsent(processGroupId, new HashSet<>()).add(connectionDTO);
    }

    public void addProcessGroupConnections(Set<ConnectionDTO> connections) {
        connections.stream().forEach(c -> addConnection(c.getParentGroupId(), c));
    }

}
