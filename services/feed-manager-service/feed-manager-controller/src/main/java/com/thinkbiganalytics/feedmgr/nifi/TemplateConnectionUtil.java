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

import com.google.common.base.Stopwatch;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.nifi.feedmgr.InputOutputPort;
import com.thinkbiganalytics.nifi.rest.NiFiObjectCache;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;

import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.status.ProcessGroupStatusDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Created by sr186054 on 12/7/17.
 */
public class TemplateConnectionUtil {

    private static final Logger log = LoggerFactory.getLogger(TemplateConnectionUtil.class);

    @Inject
    LegacyNifiRestClient restClient;
    @Inject
    private NiFiObjectCache niFiObjectCache;

    @Inject
    private NifiFlowCache nifiFlowCache;


    @Inject
    private NiFiPropertyDescriptorTransform propertyDescriptorTransform;

    @Nullable
    public ProcessGroupDTO getReusableTemplateCategoryProcessGroup() {
        return niFiObjectCache.getReusableTemplateCategoryProcessGroup();
    }

    @Nullable
    public String getReusableTemplateProcessGroupId() {
        return niFiObjectCache.getReusableTemplateProcessGroupId();
    }

    public void connectFeedToReusableTemplate(ProcessGroupDTO feedProcessGroup, ProcessGroupDTO categoryProcessGroup, List<InputOutputPort> inputOutputPorts) throws NifiComponentNotFoundException {

        Stopwatch stopwatch = Stopwatch.createStarted();
        String categoryProcessGroupId = categoryProcessGroup.getId();
        String categoryParentGroupId = categoryProcessGroup.getParentGroupId();
        String categoryProcessGroupName = categoryProcessGroup.getName();
        String feedProcessGroupId = feedProcessGroup.getId();
        String feedProcessGroupName = feedProcessGroup.getName();

        ProcessGroupDTO reusableTemplateCategory = niFiObjectCache.getReusableTemplateCategoryProcessGroup();

        if (reusableTemplateCategory == null) {
            throw new NifiClientRuntimeException("Unable to find the Reusable Template Group. Please ensure NiFi has the 'reusable_templates' processgroup and appropriate reusable flow for this feed."
                                                 + " You may need to import the base reusable template for this feed.");
        }
        String reusableTemplateCategoryGroupId = reusableTemplateCategory.getId();
        stopwatch.stop();
        log.debug("Time to get reusableTemplateCategory: {} ", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        stopwatch.reset();

        Stopwatch totalStopWatch = Stopwatch.createUnstarted();
        for (InputOutputPort port : inputOutputPorts) {
            totalStopWatch.start();
            stopwatch.start();
            PortDTO reusableTemplatePort = niFiObjectCache.getReusableTemplateInputPort(port.getInputPortName());
            stopwatch.stop();
            log.debug("Time to get reusableTemplate inputPort {} : {} ", port.getInputPortName(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            stopwatch.reset();
            if (reusableTemplatePort != null) {

                String categoryOutputPortName = categoryProcessGroupName + " to " + port.getInputPortName();
                stopwatch.start();
                PortDTO categoryOutputPort = niFiObjectCache.getCategoryOutputPort(categoryProcessGroupId, categoryOutputPortName);

                if (categoryOutputPort != null) {
                    //ensure it exists
                    try {
                        categoryOutputPort = restClient.getNiFiRestClient().ports().getOutputPort(categoryOutputPort.getId());
                    } catch (Exception e) {
                        categoryOutputPort = null;
                    }
                }
                stopwatch.stop();
                log.debug("Time to get categoryOutputPort {} : {} ", categoryOutputPortName, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                stopwatch.reset();

                if (categoryOutputPort == null) {
                    stopwatch.start();
                    //create it
                    PortDTO portDTO = new PortDTO();
                    portDTO.setParentGroupId(categoryProcessGroupId);
                    portDTO.setName(categoryOutputPortName);
                    categoryOutputPort = restClient.getNiFiRestClient().processGroups().createOutputPort(categoryProcessGroupId, portDTO);
                    niFiObjectCache.addCategoryOutputPort(categoryProcessGroupId, categoryOutputPort);
                    stopwatch.stop();
                    log.debug("Time to create categoryOutputPort {} : {} ", categoryOutputPortName, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                    stopwatch.reset();

                }
                stopwatch.start();
                Set<PortDTO> feedOutputPorts = feedProcessGroup.getContents().getOutputPorts();
                String feedOutputPortName = port.getOutputPortName();
                if (feedOutputPorts == null || feedOutputPorts.isEmpty()) {
                    feedOutputPorts = restClient.getNiFiRestClient().processGroups().getOutputPorts(feedProcessGroup.getId());
                }
                PortDTO feedOutputPort = NifiConnectionUtil.findPortMatchingName(feedOutputPorts, feedOutputPortName);
                stopwatch.stop();
                log.debug("Time to create feedOutputPort {} : {} ", feedOutputPortName, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                stopwatch.reset();
                if (feedOutputPort != null) {
                    stopwatch.start();
                    //make the connection on the category from feed to category
                    ConnectionDTO feedOutputToCategoryOutputConnection = niFiObjectCache.getConnection(categoryProcessGroupId, feedOutputPort.getId(), categoryOutputPort.getId());
                    stopwatch.stop();
                    log.debug("Time to get feedOutputToCategoryOutputConnection: {} ", stopwatch.elapsed(TimeUnit.MILLISECONDS));
                    stopwatch.reset();
                    if (feedOutputToCategoryOutputConnection == null) {
                        stopwatch.start();
                        //CONNECT FEED OUTPUT PORT TO THE Category output port
                        ConnectableDTO source = new ConnectableDTO();
                        source.setGroupId(feedProcessGroupId);
                        source.setId(feedOutputPort.getId());
                        source.setName(feedProcessGroupName);
                        source.setType(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name());
                        ConnectableDTO dest = new ConnectableDTO();
                        dest.setGroupId(categoryProcessGroupId);
                        dest.setName(categoryOutputPort.getName());
                        dest.setId(categoryOutputPort.getId());
                        dest.setType(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name());
                        //ensure the port exists
                        niFiObjectCache.addCategoryOutputPort(categoryProcessGroupId, categoryOutputPort);
                        feedOutputToCategoryOutputConnection = restClient.createConnection(categoryProcessGroupId, source, dest);
                        niFiObjectCache.addConnection(categoryProcessGroupId, feedOutputToCategoryOutputConnection);
                        nifiFlowCache.addConnectionToCache(feedOutputToCategoryOutputConnection);
                        stopwatch.stop();
                        log.debug("Time to create feedOutputToCategoryOutputConnection: {} ", stopwatch.elapsed(TimeUnit.MILLISECONDS));
                        stopwatch.reset();
                    }

                    stopwatch.start();
                    //connection made on parent (root) to reusable template
                    ConnectionDTO
                        categoryToReusableTemplateConnection = niFiObjectCache.getConnection(categoryProcessGroup.getParentGroupId(), categoryOutputPort.getId(), reusableTemplatePort.getId());
                    stopwatch.stop();
                    log.debug("Time to get categoryToReusableTemplateConnection: {} ", stopwatch.elapsed(TimeUnit.MILLISECONDS));
                    stopwatch.reset();
                    //Now connect the category ProcessGroup to the global template
                    if (categoryToReusableTemplateConnection == null) {
                        stopwatch.start();
                        ConnectableDTO categorySource = new ConnectableDTO();
                        categorySource.setGroupId(categoryProcessGroupId);
                        categorySource.setId(categoryOutputPort.getId());
                        categorySource.setName(categoryOutputPortName);
                        categorySource.setType(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name());
                        ConnectableDTO categoryToGlobalTemplate = new ConnectableDTO();
                        categoryToGlobalTemplate.setGroupId(reusableTemplateCategoryGroupId);
                        categoryToGlobalTemplate.setId(reusableTemplatePort.getId());
                        categoryToGlobalTemplate.setName(reusableTemplatePort.getName());
                        categoryToGlobalTemplate.setType(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name());
                        categoryToReusableTemplateConnection = restClient.createConnection(categoryParentGroupId, categorySource, categoryToGlobalTemplate);
                        niFiObjectCache.addConnection(categoryParentGroupId, categoryToReusableTemplateConnection);
                        nifiFlowCache.addConnectionToCache(categoryToReusableTemplateConnection);
                        stopwatch.stop();
                        log.debug("Time to create categoryToReusableTemplateConnection: {} ", stopwatch.elapsed(TimeUnit.MILLISECONDS));
                        stopwatch.reset();
                    }
                }


            }
            totalStopWatch.stop();
            log.debug("Time to connect feed to {} port. ElapsedTime: {} ", port.getInputPortName(), totalStopWatch.elapsed(TimeUnit.MILLISECONDS));
            totalStopWatch.reset();
        }

    }

    public void removeProcessGroup(ProcessGroupDTO processGroupDTO) {
        if (processGroupDTO != null) {
            try {
                //validate if nothing is in the queue then remove it
                Optional<ProcessGroupStatusDTO> statusDTO = restClient.getNiFiRestClient().processGroups().getStatus(processGroupDTO.getId());
                if (statusDTO.isPresent() && propertyDescriptorTransform.getQueuedCount(statusDTO.get()).equalsIgnoreCase("0")) {
                    //get connections linking to this group, delete them
                    Set<ConnectionDTO> connectionDTOs = restClient.getProcessGroupConnections(processGroupDTO.getParentGroupId());
                    if (connectionDTOs == null) {
                        connectionDTOs = new HashSet<>();
                    }
                    Set<ConnectionDTO>
                        versionedConnections =
                        connectionDTOs.stream().filter(connectionDTO -> connectionDTO.getDestination().getGroupId().equalsIgnoreCase(processGroupDTO.getId()) || connectionDTO.getSource().getGroupId()
                            .equalsIgnoreCase(processGroupDTO.getId()))
                            .collect(Collectors.toSet());
                    restClient.deleteProcessGroupAndConnections(processGroupDTO, versionedConnections);
                    log.info("removed the versioned processgroup {} ", processGroupDTO.getName());
                } else {
                    log.info("Unable to remove the versioned processgroup {} ", processGroupDTO.getName());
                }
            } catch (Exception e) {
                log.error("Unable to remove the versioned processgroup {} ", processGroupDTO.getName(), e);
            }
        }
    }

    public void setRestClient(LegacyNifiRestClient restClient) {
        this.restClient = restClient;
    }

    public void setNiFiObjectCache(NiFiObjectCache niFiObjectCache) {
        this.niFiObjectCache = niFiObjectCache;
    }

    public void setNifiFlowCache(NifiFlowCache nifiFlowCache) {
        this.nifiFlowCache = nifiFlowCache;
    }

    public void setPropertyDescriptorTransform(NiFiPropertyDescriptorTransform propertyDescriptorTransform) {
        this.propertyDescriptorTransform = propertyDescriptorTransform;
    }
}
