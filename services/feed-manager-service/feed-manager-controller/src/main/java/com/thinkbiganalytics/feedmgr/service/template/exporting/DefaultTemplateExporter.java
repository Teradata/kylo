package com.thinkbiganalytics.feedmgr.service.template.exporting;
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
import com.thinkbiganalytics.feedmgr.rest.model.PortDTOWithGroupInfo;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplateRequest;
import com.thinkbiganalytics.feedmgr.rest.model.RemoteProcessGroupInputPort;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.rest.model.TemplateRemoteInputPortConnections;
import com.thinkbiganalytics.feedmgr.rest.support.SystemNamingService;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.template.export.ExportTemplate;
import com.thinkbiganalytics.metadata.api.template.export.TemplateExporter;
import com.thinkbiganalytics.metadata.api.template.security.TemplateAccessControl;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiRemoteProcessGroupUtil;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.dto.flow.ProcessGroupFlowDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

/**
 * Created by sr186054 on 12/13/17.
 */
public class DefaultTemplateExporter implements TemplateExporter {

    @Inject
    private AccessController accessController;

    @Inject
    private RegisteredTemplateService registeredTemplateService;

    @Inject
    private LegacyNifiRestClient nifiRestClient;

    @Inject
    private TemplateConnectionUtil templateConnectionUtil;

    private Boolean remoteProcessGroupEnabled = null;

    /**
     * Check to ensure the user just has the EXPORT_FEEDS functional access permission before exporting
     *
     * @param templateId the registered template id
     * @return the exported template
     */
    public ExportTemplate exportTemplateForFeedExport(String templateId) {
        this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EXPORT_FEEDS);
        registeredTemplateService.checkTemplatePermission(templateId, TemplateAccessControl.ACCESS_TEMPLATE);
        return export(templateId);
    }

    /**
     * Check to ensure the user has the EXPORT_TEMPLATES functional access permission before exporting
     *
     * @param templateId the registered template id
     * @return the exported template
     */
    @Override
    public ExportTemplate exportTemplate(String templateId) {
        this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EXPORT_TEMPLATES);
        registeredTemplateService.checkTemplatePermission(templateId, TemplateAccessControl.EXPORT);
        return export(templateId);
    }

    private boolean isRemoteProcessGroupsEnabled() {
        if (remoteProcessGroupEnabled == null) {
            remoteProcessGroupEnabled = registeredTemplateService.isRemoteProcessGroupsEnabled();
        }
        return remoteProcessGroupEnabled;
    }

    List<ReusableTemplateConnectionInfo> getRemoteProcessGroupConnectionInfo(TemplateDTO templateDTO) {
        if (templateDTO != null && templateDTO.getSnippet() != null && templateDTO.getSnippet().getRemoteProcessGroups() != null) {
            return NifiRemoteProcessGroupUtil.remoteProcessGroupDtos(templateDTO).stream().map(rpg -> {
                ReusableTemplateConnectionInfo connectionInfo = new ReusableTemplateConnectionInfo();
                String connectedInputPort = rpg.getContents().getInputPorts().stream()
                    .filter(port -> port.isConnected())
                    .map(port -> port.getName()).findFirst().orElse(null);
                connectionInfo.setReusableTemplateInputPortName(connectedInputPort);
                connectionInfo.setInputPortDisplayName(connectedInputPort);
                return connectionInfo;
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private ExportTemplate export(String templateId) {
        RegisteredTemplate
            template =
            registeredTemplateService.findRegisteredTemplate(new RegisteredTemplateRequest.Builder().templateId(templateId).nifiTemplateId(templateId).includeSensitiveProperties(true).build());
        if (template != null) {
            List<String> connectingReusableTemplates = new ArrayList<>();
            Set<String> connectedTemplateIds = new HashSet<>();
            Set<ReusableTemplateConnectionInfo> outputPortConnectionMetadata = new HashSet<>();

            Set<RemoteProcessGroupInputPort> templateRemoteInputPorts = new HashSet<>();
            List<ReusableTemplateConnectionInfo> reusableTemplateConnectionInfos = null;

            if (template.usesReusableTemplate()) {
                reusableTemplateConnectionInfos = template.getReusableTemplateConnections();
            }

            List<ReusableTemplateConnectionInfo> remoteProcessGroupConnectionInfo = getRemoteProcessGroupConnectionInfo(template.getNifiTemplate());
            if (reusableTemplateConnectionInfos != null) {
                reusableTemplateConnectionInfos.addAll(remoteProcessGroupConnectionInfo);
            } else {
                reusableTemplateConnectionInfos = remoteProcessGroupConnectionInfo;
            }

            if (reusableTemplateConnectionInfos != null && !reusableTemplateConnectionInfos.isEmpty()) {
                ProcessGroupFlowDTO reusableTemplateFlow = templateConnectionUtil.getReusableTemplateCategoryProcessGroupFlow();

                Map<String, PortDTOWithGroupInfo>
                    reusableTemplatePorts =
                    templateConnectionUtil.getReusableFeedInputPorts(reusableTemplateFlow).stream().collect(Collectors.toMap(port -> port.getName(), port -> port));
                reusableTemplateConnectionInfos.stream().filter(connectionInfo -> StringUtils.isBlank(connectionInfo.getReusableTemplateProcessGroupName())).forEach(connectionInfo -> {
                    PortDTOWithGroupInfo port = reusableTemplatePorts.get(connectionInfo.getReusableTemplateInputPortName());
                    if (port != null) {
                        connectionInfo.setReusableTemplateProcessGroupName(port.getDestinationProcessGroupName());
                    }
                });

                //Get flow information for the 'reusable_templates' process group in NiFi
                if (reusableTemplateFlow != null) {

                    gatherConnectedReusableTemplates(connectingReusableTemplates, connectedTemplateIds, outputPortConnectionMetadata, reusableTemplateConnectionInfos, reusableTemplateFlow);
                }

                //Only gather remote input ports on the reusable templates if enabled
                if (isRemoteProcessGroupsEnabled()) {
                    //for all the reusable templates used gather any that have remote input ports
                    reusableTemplateConnectionInfos.stream().forEach(connectionInfo -> {
                        Set<RemoteProcessGroupInputPort>
                            remoteProcessGroupInputPorts =
                            findReusableTemplateRemoteInputPorts(reusableTemplateFlow, connectionInfo.getReusableTemplateProcessGroupName());
                        templateRemoteInputPorts.addAll(remoteProcessGroupInputPorts);
                    });
                }

            }

            String templateXml = null;
            try {
                if (template != null) {
                    try {
                        templateXml = nifiRestClient.getTemplateXml(template.getNifiTemplateId());
                    } catch (NifiClientRuntimeException e) {
                        TemplateDTO templateDTO = nifiRestClient.getTemplateByName(template.getTemplateName());
                        if (templateDTO != null) {
                            templateXml = nifiRestClient.getTemplateXml(templateDTO.getId());
                        }
                    }
                }
            } catch (Exception e) {
                throw new UnsupportedOperationException("Unable to find Nifi Template for " + templateId);
            }

            //create a zip file with the template and xml
            byte[] zipFile = zip(template, templateXml, connectingReusableTemplates, outputPortConnectionMetadata, templateRemoteInputPorts);

            return new ExportTemplate(SystemNamingService.generateSystemName(template.getTemplateName()) + ".template.zip", zipFile);

        } else {
            throw new UnsupportedOperationException("Unable to find Template for " + templateId);
        }
    }

    public Set<RemoteProcessGroupInputPort> findReusableTemplateRemoteInputPorts(ProcessGroupFlowDTO reusableTemplateFlow, String templateName) {
        Optional<TemplateRemoteInputPortConnections> remoteInputPortConnections = templateConnectionUtil.getRemoteInputPortsForReusableTemplate(reusableTemplateFlow, templateName);
        if (remoteInputPortConnections.isPresent()) {
            return remoteInputPortConnections.get().getExistingRemoteInputPortNames().stream().map(name -> {
                RemoteProcessGroupInputPort port = new RemoteProcessGroupInputPort(templateName, name);
                port.setSelected(true);
                return port;
            })
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private void gatherConnectedReusableTemplates(List<String> connectingReusableTemplates, Set<String> connectedTemplateIds,
                                                  Set<ReusableTemplateConnectionInfo> connectingOutputPortConnectionMetadata, List<ReusableTemplateConnectionInfo> reusableTemplateConnectionInfos,
                                                  ProcessGroupFlowDTO reusableTemplateFlow) {
        for (ReusableTemplateConnectionInfo reusableTemplateConnectionInfo : reusableTemplateConnectionInfos) {
            String inputName = reusableTemplateConnectionInfo.getReusableTemplateInputPortName();
            //find the process group instance in the 'reusable_templates' group in NiFi for this input port
            Optional<ProcessGroupDTO> processGroupDTO =
                reusableTemplateFlow.getFlow().getConnections().stream()
                    .map(connectionEntity -> connectionEntity.getComponent())
                    .filter(connectionDTO -> connectionDTO.getSource().getName().equals(inputName) && connectionDTO.getSource().getType().equals(NifiConstants.INPUT_PORT))
                    .flatMap(connectionDTO -> reusableTemplateFlow.getFlow().getProcessGroups()
                        .stream()
                        .map(processGroupEntity -> processGroupEntity.getComponent())
                        .filter(groupDTO -> groupDTO.getId().equals(connectionDTO.getDestination().getGroupId()))).findFirst();
            if (processGroupDTO.isPresent()) {
                //walk the output ports to find any other connecting templates
                List<ReusableTemplateConnectionInfo> outputPortConnectionMetadata = new ArrayList<>();

                List<ConnectionDTO>
                    outputPortConnections =
                    reusableTemplateFlow.getFlow().getConnections().stream().map(connectionEntity -> connectionEntity.getComponent())
                        .filter(connectionDTO -> connectionDTO.getSource().getGroupId().equals(processGroupDTO.get().getId()) && connectionDTO.getSource().getType().equals(NifiConstants.OUTPUT_PORT))
                        .collect(
                            Collectors.toList());
                for (ConnectionDTO outputConnection : outputPortConnections) {
                    //walk these and get their templates

                    //first get the connection metadata info needed
                    //1 find the reusable template input port for this connected template
                    Optional<ConnectionDTO>
                        inputPortToProcessGroupConnection =
                        reusableTemplateFlow.getFlow().getConnections().stream().map(connectionEntity -> connectionEntity.getComponent()).filter(
                            connectionDTO -> connectionDTO.getDestination().getId().equals(outputConnection.getDestination().getId()) && connectionDTO.getSource().getType()
                                .equals(NifiConstants.INPUT_PORT)).findFirst();
                    if (inputPortToProcessGroupConnection.isPresent()) {
                        ReusableTemplateConnectionInfo connectionInfo = new ReusableTemplateConnectionInfo();
                        connectionInfo.setFeedOutputPortName(outputConnection.getSource().getName());
                        connectionInfo.setReusableTemplateInputPortName(inputPortToProcessGroupConnection.get().getSource().getName());
                        connectionInfo.setInputPortDisplayName(inputPortToProcessGroupConnection.get().getSource().getName());

                        String processGroupName = reusableTemplateFlow.getFlow().getProcessGroups().stream()
                            .filter(processGroupEntity -> processGroupEntity.getComponent().getId().equals(inputPortToProcessGroupConnection.get().getDestination().getGroupId()))
                            .map(processGroupEntity -> processGroupEntity.getComponent().getName())
                            .findFirst().orElse(null);
                        connectionInfo.setReusableTemplateProcessGroupName(processGroupName);
                        outputPortConnectionMetadata.add(connectionInfo);
                        //recursively walk these flows and gather other output port connections
                        connectingOutputPortConnectionMetadata.add(connectionInfo);
                    }
                    //also add in the process group if it doesnt connect

                }
                if (!outputPortConnectionMetadata.isEmpty()) {
                    gatherConnectedReusableTemplates(connectingReusableTemplates, connectedTemplateIds, connectingOutputPortConnectionMetadata, outputPortConnectionMetadata, reusableTemplateFlow);
                }

            }

            //find the template that has the input port name
            Map<String, String> map = nifiRestClient.getTemplatesAsXmlMatchingInputPortName(inputName, reusableTemplateConnectionInfo.getReusableTemplateProcessGroupName());
            if (map != null && !map.isEmpty()) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String portTemplateId = entry.getKey();
                    if (!connectedTemplateIds.contains(portTemplateId)) {
                        connectedTemplateIds.add(portTemplateId);
                        connectingReusableTemplates.add(entry.getValue());
                    }
                }
            }
        }
    }

    private byte[] zip(RegisteredTemplate template, String nifiTemplateXml, List<String> reusableTemplateXmls, Set<ReusableTemplateConnectionInfo> outputPortMetadata,
                       Set<RemoteProcessGroupInputPort> reusableTemplateRemoteInputPorts) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            ZipEntry entry = new ZipEntry(ImportTemplate.NIFI_TEMPLATE_XML_FILE);
            zos.putNextEntry(entry);
            zos.write(nifiTemplateXml.getBytes());
            zos.closeEntry();
            int reusableTemplateNumber = 0;
            for (String reusableTemplateXml : reusableTemplateXmls) {
                entry = new ZipEntry(String.format("%s_%s.xml", ImportTemplate.NIFI_CONNECTING_REUSABLE_TEMPLATE_XML_FILE, reusableTemplateNumber++));
                zos.putNextEntry(entry);
                zos.write(reusableTemplateXml.getBytes());
                zos.closeEntry();
            }
            entry = new ZipEntry(ImportTemplate.TEMPLATE_JSON_FILE);
            zos.putNextEntry(entry);
            String json = ObjectMapperSerializer.serialize(template);
            zos.write(json.getBytes());
            zos.closeEntry();

            if (outputPortMetadata != null && !outputPortMetadata.isEmpty()) {
                entry = new ZipEntry(ImportTemplate.REUSABLE_TEMPLATE_OUTPUT_CONNECTION_FILE);
                zos.putNextEntry(entry);
                json = ObjectMapperSerializer.serialize(outputPortMetadata);
                zos.write(json.getBytes());
                zos.closeEntry();
            }

            if (reusableTemplateRemoteInputPorts != null && !reusableTemplateRemoteInputPorts.isEmpty()) {
                entry = new ZipEntry(ImportTemplate.REUSABLE_TEMPLATE_REMOTE_INPUT_PORT_JSON_FILE);
                zos.putNextEntry(entry);
                json = ObjectMapperSerializer.serialize(reusableTemplateRemoteInputPorts);
                zos.write(json.getBytes());
                zos.closeEntry();
            }

        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return baos.toByteArray();
    }

}
