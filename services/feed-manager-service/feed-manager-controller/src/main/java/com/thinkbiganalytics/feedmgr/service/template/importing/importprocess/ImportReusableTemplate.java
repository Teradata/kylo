package com.thinkbiganalytics.feedmgr.service.template.importing.importprocess;

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

import com.thinkbiganalytics.feedmgr.nifi.PropertyExpressionResolver;
import com.thinkbiganalytics.feedmgr.nifi.TemplateConnectionUtil;
import com.thinkbiganalytics.feedmgr.nifi.cache.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.rest.ImportComponent;
import com.thinkbiganalytics.feedmgr.rest.ImportSection;
import com.thinkbiganalytics.feedmgr.rest.model.ImportComponentOption;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.RemoteProcessGroupInputPort;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.rest.model.TemplateRemoteInputPortConnections;
import com.thinkbiganalytics.feedmgr.rest.model.UploadProgressMessage;
import com.thinkbiganalytics.feedmgr.service.UploadProgressService;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateCache;
import com.thinkbiganalytics.feedmgr.service.template.RegisteredTemplateService;
import com.thinkbiganalytics.feedmgr.service.template.RemoteInputPortService;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.NiFiTemplateImport;
import com.thinkbiganalytics.nifi.feedmgr.ReusableTemplateCreationCallback;
import com.thinkbiganalytics.nifi.feedmgr.TemplateCreationHelper;
import com.thinkbiganalytics.nifi.rest.NiFiObjectCache;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.model.NiFiClusterSummary;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.model.VersionedProcessGroup;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiFlowUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectableDTO;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.apache.nifi.web.api.dto.flow.FlowDTO;
import org.apache.nifi.web.api.dto.flow.ProcessGroupFlowDTO;
import org.apache.nifi.web.api.dto.status.ProcessGroupStatusDTO;
import org.apache.nifi.web.api.dto.status.ProcessGroupStatusSnapshotDTO;
import org.apache.nifi.web.api.entity.RemoteProcessGroupStatusSnapshotEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Created by sr186054 on 12/11/17.
 */
public class ImportReusableTemplate extends AbstractImportTemplateRoutine implements ImportTemplateRoutine {

    private static final Logger log = LoggerFactory.getLogger(ImportReusableTemplate.class);

    @Inject
    private AccessController accessController;
    @Inject
    private LegacyNifiRestClient nifiRestClient;
    @Inject
    private UploadProgressService uploadProgressService;
    @Inject
    private RegisteredTemplateService registeredTemplateService;
    @Inject
    private TemplateConnectionUtil templateConnectionUtil;
    @Inject
    private RegisteredTemplateCache registeredTemplateCache;
    @Inject
    private PropertyExpressionResolver propertyExpressionResolver;

    @Inject
    private RemoteInputPortService remoteInputPortService;

    @Inject
    private NifiFlowCache nifiFlowCache;

    @Inject
    private NiFiObjectCache niFiObjectCache;

    private ProcessGroupFlowDTO reusableTemplateFlow;


    /**
     * Connections to/from this template
     */
    private List<ConnectionDTO> connections = new ArrayList<>();

    private Map<String, PortDTO> templateInputPorts;

    /**
     * Map of input ports that are/will become RemoteInputPorts on the parent NiFi Flow canvas
     */
    private Map<String, RemoteProcessGroupInputPort> remoteProcessGroupInputPortMap;

    private ProcessGroupDTO reusableTemplateCategoryProcessGroup;

    private ItemsCreated itemsCreated;

    private Boolean clustered = null;

    private Boolean remoteProcessGroupEnabled = null;


    private Optional<TemplateRemoteInputPortConnections> existingRemoteProcessInputPortInformation;




    public ImportReusableTemplate(ImportTemplate importTemplate, ImportTemplateOptions importOptions) {
        super(importTemplate, importOptions);
    }


    public ImportReusableTemplate(String fileName, byte[] xmlFile, ImportTemplateOptions importOptions) {
        super(fileName, xmlFile, importOptions);
    }

    private boolean markExistingRemoteInputPorts(ImportComponentOption remoteProcessGroupOption, Map<String, RemoteProcessGroupInputPort>
        remoteProcessGroupInputPortMap, Map<String, PortDTO> thisTemplatePorts, boolean isNew) {

        String rootProcessGroupId = templateConnectionUtil.getRootProcessGroup().getId();
        //This will get or create the reusable_template process group
        String reusableTemplateProcessGroupId = templateConnectionUtil.getReusableTemplateProcessGroupId();

        //Select the userSupplied ports from the whole list
        Map<String, RemoteProcessGroupInputPort>
            userSuppliedRemoteInputPorts =
            remoteProcessGroupOption.getRemoteProcessGroupInputPortsForTemplate(importTemplate.getTemplateName()).stream()
                .collect(Collectors.toMap(inputPort -> inputPort.getInputPortName(), inputPort -> inputPort));

        Optional<TemplateRemoteInputPortConnections> existingRemoteProcessInputPortInformation = getExistingRemoteProcessInputPortInformation();

        if (existingRemoteProcessInputPortInformation.isPresent()) {
            //mark the items in the this.remoteProcessGroupInputPortMap as 'existing' if they are already in NiFi, and 'selected' if the user has selected them
            existingRemoteProcessInputPortInformation.get().getExistingRemoteConnectionsToTemplate().stream().filter(conn -> conn.getDestination().getType().equalsIgnoreCase(NifiConstants.INPUT_PORT)
                                                                                                                             && conn.getDestination().getGroupId()
                                                                                                                                 .equalsIgnoreCase(reusableTemplateProcessGroupId)
                                                                                                                             && conn.getSource().getGroupId().equalsIgnoreCase(rootProcessGroupId)
                                                                                                                             && conn.getSource().getType().equalsIgnoreCase(NifiConstants.INPUT_PORT)
                                                                                                                             && thisTemplatePorts.containsKey(conn.getDestination().getName()))
                .map(conn -> thisTemplatePorts.get(conn.getDestination().getName()))
                .filter(p -> remoteProcessGroupInputPortMap.containsKey(p.getName()))
                .map(port -> remoteProcessGroupInputPortMap.get(port.getName()))
                .forEach(remoteProcessGroupInputPort -> {
                    remoteProcessGroupInputPort.setSelected(isNew || (userSuppliedRemoteInputPorts.containsKey(remoteProcessGroupInputPort.getInputPortName()) && userSuppliedRemoteInputPorts
                        .get(remoteProcessGroupInputPort.getInputPortName()).isSelected()));
                    remoteProcessGroupInputPort.setExisting(true);
                    RemoteProcessGroupInputPort userSuppliedPort = userSuppliedRemoteInputPorts.get(remoteProcessGroupInputPort.getInputPortName());
                    if (userSuppliedPort != null) {
                        userSuppliedPort.setExisting(true);
                    }
                });

        }

        remoteProcessGroupInputPortMap.values().stream().filter(inputPort -> userSuppliedRemoteInputPorts.containsKey(inputPort.getInputPortName())).forEach(inputPort -> inputPort.setSelected(true));

        //warn if not existing, but has the same name as an already existing input port in the root process group
        Set<String> rootInputPorts = nifiRestClient.getNiFiRestClient().processGroups().getInputPorts(rootProcessGroupId).stream().map(inputPort -> inputPort.getName()).collect(Collectors.toSet());
        Set<String>
            portAlreadyExists =
            remoteProcessGroupInputPortMap.values().stream()
                .filter(remoteProcessGroupInputPort -> !remoteProcessGroupInputPort.isExisting() && rootInputPorts.contains(remoteProcessGroupInputPort.getInputPortName()))
                .map(remoteProcessGroupInputPort -> remoteProcessGroupInputPort.getInputPortName()).collect(
                Collectors.toSet());
        if (!portAlreadyExists.isEmpty()) {
            importTemplate.getTemplateResults()
                .addError(NifiError.SEVERITY.FATAL, " The input port names " + portAlreadyExists.stream().collect(Collectors.joining(",")) + " already exists as a remote port for another template.",
                          "");
            remoteProcessGroupOption.getErrorMessages().add(
                "The input port names " + portAlreadyExists.stream().collect(Collectors.joining(",")) + " already exists as a remote port for another template.");
            importTemplate.setSuccess(false);
            importTemplate.setValid(false);
            importTemplate.setRemoteProcessGroupInputPortsNeeded(true);
            return false;
        }

        return true;

    }

    private ProcessGroupDTO getReusableTemplateCategoryProcessGroup() {

        if (reusableTemplateCategoryProcessGroup == null) {
            //Reusable_template ports for this template
            String reusableTemplateProcessGroupId = templateConnectionUtil.getReusableTemplateProcessGroupId();
            Optional<ProcessGroupDTO> reusableTemplateProcessGroup = nifiRestClient.getNiFiRestClient().processGroups().findById(reusableTemplateProcessGroupId, false, true);
            this.reusableTemplateCategoryProcessGroup = reusableTemplateProcessGroup.get();
        }
        return reusableTemplateCategoryProcessGroup;
    }

    private boolean isClustered() {
        if (clustered == null) {
            NiFiClusterSummary clusterSummary = nifiRestClient.getNiFiRestClient().clusterSummary();
            clustered = clusterSummary.getClustered();
        }
        return clustered != null ? clustered : false;
    }

    private boolean isRemoteProcessGroupsEnabled(){
        if(remoteProcessGroupEnabled == null){
            remoteProcessGroupEnabled = registeredTemplateService.isRemoteProcessGroupsEnabled();
        }
        return remoteProcessGroupEnabled;
    }

    /**
     * Validates the user has supplied some input ports to be created as remote ports
     *
     * @param remoteProcessGroupOption the user supplied option and details for remote process group input port processing
     * @return true if valid, false if not
     */
    public boolean validateRemoteInputPorts(ImportComponentOption remoteProcessGroupOption) {
        //find list of input ports that have been created already (connected to this same reusable template)
        //1) find input ports on parent nifi canvas that connect to the reusable template with this same name
        //2) add these as 'selected' to the list
        //3) if some of those dont appear in the new list add as warning (these will be removed)
        boolean valid = true;
        if (!isRemoteProcessGroupsEnabled()) {
            return true;
        }
        return validateRemoteInputPorts(importTemplate.getTemplateResults().getProcessGroupEntity().getContents().getInputPorts());
    }

    public ItemsCreated getItemsCreated() {
        if (itemsCreated == null) {
            itemsCreated = new ItemsCreated();
        }
        return itemsCreated;
    }

    public boolean importTemplate() {
        boolean validReusableTemplate = importIntoNiFiAndCreateInstance();
        templateConnectionUtil.ensureReusableTemplateProcessGroup();

        //Check and set the Remote PRocess group settings.
        //use this later to determine if we need to create NiFi Flow input ports connected to this template
        ImportComponentOption remoteProcessGroupOption = importTemplateOptions.findImportComponentOption(ImportComponent.REMOTE_INPUT_PORT);
        if (validReusableTemplate && remoteProcessGroupOption.isShouldImport()) {
            validReusableTemplate &= validateRemoteInputPorts(remoteProcessGroupOption);
        }

        if (validReusableTemplate) {
            validReusableTemplate = connectAndValidate();
        }
        if (validReusableTemplate) {
            validReusableTemplate = validateInstance();
        }

        if (validReusableTemplate) {
            nifiRestClient.markConnectionPortsAsRunning(importTemplate.getTemplateResults().getProcessGroupEntity());
            //remove previous template
            cleanup();
        } else {
            rollback();
        }

        return validReusableTemplate;
    }

    @Override
    public boolean connectAndValidate() {
        return connect();
    }

    @Override
    public NiFiTemplateImport importIntoNiFi(ImportTemplate template, ImportTemplateOptions importOptions) {
        return super.importIntoNiFi(template, importOptions);
    }

    private boolean validateRemoteInputPorts(Set<PortDTO> inputPorts) {
        ImportComponentOption remoteProcessGroupOption = importTemplateOptions.findImportComponentOption(ImportComponent.REMOTE_INPUT_PORT);
        boolean valid = true;
        if (remoteProcessGroupOption.isShouldImport()) {
            //This templates input ports as a map by name
            this.templateInputPorts = inputPorts.stream().collect(Collectors.toMap(p -> p.getName(), v -> v));

            //set the map of input ports in this template as potential Remote Input port candidates.
            this.remoteProcessGroupInputPortMap =
                this.templateInputPorts.values().stream().map(p -> new RemoteProcessGroupInputPort(importTemplate.getTemplateName(), p.getName()))
                    .collect(Collectors.toMap(p -> p.getInputPortName(), p -> p));

            if (!remoteProcessGroupOption.isUserAcknowledged()) {

                //present back to the user the list of input ports to select
                importTemplate.setRemoteProcessGroupInputPortsNeeded(true);

                //WARN if the remoteProcessGroupInputPortMap has names that are not in the 'thisTemplatePorts'
                List<String> invalidPorts = remoteProcessGroupInputPortMap.keySet().stream().filter(name -> !this.templateInputPorts.keySet().contains(name)).collect(Collectors.toList());
                if (!invalidPorts.isEmpty()) {
                    //the following ports (invalidPorts) will be deleted from the as they no longer exist for this template.
                    //Any remote Process group ports created for them will also be deleted
                    importTemplate.getTemplateResults().addError(NifiError.SEVERITY.WARN, " Missing 'remote process group ' input ports  ", "");
                    remoteProcessGroupOption.getErrorMessages().add(
                        " The following 'remote process group ' input ports are no longer part of this template. " + invalidPorts.stream().collect(Collectors.joining(","))
                        + ". Are you sure you want to continue?  They will be deleted. ");
                }
                importTemplate.setRemoteProcessGroupInputPortNames(new ArrayList<>(remoteProcessGroupInputPortMap.values()));
                importTemplate.setSuccess(false);
                importTemplate.setValid(false);
                markExistingRemoteInputPorts(remoteProcessGroupOption, remoteProcessGroupInputPortMap, this.templateInputPorts, true);
                valid = false;
            } else {
                //user has already supplied some ports... validate the ports exist for this template
                //warn if the user supplied input port selections that dont exist for this template
                Set<String>
                    nonExistentPortNames =
                    remoteProcessGroupOption.getRemoteProcessGroupInputPortsForTemplate(importTemplate.getTemplateName()).stream()
                        .filter(r -> !this.templateInputPorts.keySet().contains(r.getInputPortName())).map(r -> r.getInputPortName()).collect(
                        Collectors.toSet());
                if (!nonExistentPortNames.isEmpty()) {
                    importTemplate.getTemplateResults().addError(NifiError.SEVERITY.FATAL, " Invalid input port names supplied", "");
                    remoteProcessGroupOption.getErrorMessages().add(
                        "The following input ports you supplied as remote ports dont existing in this template: " + nonExistentPortNames.stream().collect(Collectors.joining(",")) + ".");
                    importTemplate.setRemoteProcessGroupInputPortsNeeded(true);
                    valid = false;
                }
                valid &= markExistingRemoteInputPorts(remoteProcessGroupOption, remoteProcessGroupInputPortMap, this.templateInputPorts, false);
                importTemplate.setSuccess(valid);
                importTemplate.setValid(valid);
            }
        }
        return valid;
    }

    @Override
    public NifiProcessGroup create(NiFiTemplateImport niFiTemplateImport, UploadProgressMessage importStatusMessage) {

        TemplateDTO dto = niFiTemplateImport.getDto();
        String templateName = importTemplate.getTemplateName();
        String fileName = importTemplate.getFileName();
        importStatusMessage.update("Creating reusable flow instance for " + templateName);
        log.info("Creating a Reusable flow template instance in Nifi. Template: {} for file {}", templateName, fileName);
        Map<String, Object> configProperties = propertyExpressionResolver.getStaticConfigProperties();


        NifiFlowCacheReusableTemplateCreationCallback reusableTemplateCreationCallback = new NifiFlowCacheReusableTemplateCreationCallback();

        List<NifiProperty> templateProperties = importTemplate.getTemplateToImport() != null ? importTemplate.getTemplateToImport().getProperties() : Collections.emptyList();

        NifiProcessGroup
            newTemplateInstance = null;
        //check to see if we have acknowledged to import as remote input ports
        ImportComponentOption remoteProcessGroupOption = importTemplateOptions.findImportComponentOption(ImportComponent.REMOTE_INPUT_PORT);
        if (remoteProcessGroupOption.isShouldImport() && !remoteProcessGroupOption.isUserAcknowledged()) {
            //gather the ports by creating a temp flow
            TemplateCreationHelper templateCreationHelper = new TemplateCreationHelper(this.nifiRestClient, niFiObjectCache);
            ProcessGroupDTO flow = templateCreationHelper.createTemporaryTemplateFlow(dto.getId());
            boolean valid = validateRemoteInputPorts(flow.getContents().getInputPorts());
            newTemplateInstance = new NifiProcessGroup();
            newTemplateInstance.setSuccess(false);
        }
        else {
            newTemplateInstance =
                nifiRestClient.createNewTemplateInstance(dto.getId(), templateProperties, configProperties, true, reusableTemplateCreationCallback, importTemplate.getVersionIdentifier());
            if (newTemplateInstance.getVersionedProcessGroup() != null && StringUtils.isNotBlank(newTemplateInstance.getVersionedProcessGroup().getVersionedProcessGroupName())) {
                uploadProgressService
                    .addUploadStatus(importTemplateOptions.getUploadKey(),
                                     "Versioned off previous flow with the name: " + newTemplateInstance.getVersionedProcessGroup().getVersionedProcessGroupName(),
                                     true, true);
            }
        }
        importTemplate.setTemplateResults(newTemplateInstance);
        return newTemplateInstance;

    }

    private boolean startConnectablePorts(ConnectableDTO connectableDTO) {
        boolean valid = true;
        if (!connectableDTO.isRunning()) {
            try {
                if (connectableDTO.getType().equals(NifiConstants.INPUT_PORT)) {
                    nifiRestClient.startInputPort(connectableDTO.getGroupId(), connectableDTO.getId());
                    uploadProgressService
                        .addUploadStatus(importTemplateOptions.getUploadKey(), "Started the input port '" + connectableDTO.getName() + "' for the template " + importTemplate.getTemplateName(), true,
                                         true);
                } else if (connectableDTO.getType().equals(NifiConstants.OUTPUT_PORT)) {
                    nifiRestClient.startOutputPort(connectableDTO.getGroupId(), connectableDTO.getId());
                    uploadProgressService
                        .addUploadStatus(importTemplateOptions.getUploadKey(), "Started the output port '" + connectableDTO.getName() + "' for the template " + importTemplate.getTemplateName(), true,
                                         true);
                }
            } catch (Exception e) {
                valid = false;
                uploadProgressService.addUploadStatus(importTemplateOptions.getUploadKey(),
                                                      "Unable to start the " + (connectableDTO.getType().equals(NifiConstants.INPUT_PORT) ? "input" : " output") + " port '" + connectableDTO.getName()
                                                      + "' for the template " + importTemplate.getTemplateName(), true, false);
                importTemplate.getTemplateResults().addError(NifiError.SEVERITY.FATAL,
                                                             "Unable to start the " + (connectableDTO.getType().equals(NifiConstants.INPUT_PORT) ? "input" : " output") + " port '" + connectableDTO
                                                                 .getName() + "'", "Input/Output Port");
            }
        }
        return valid;
    }

    public boolean validateInstance() {
        boolean valid = importTemplate.getTemplateResults().isSuccess() && importTemplate.isValid() && !importTemplate.isReusableFlowOutputPortConnectionsNeeded();
        importTemplate.setSuccess(valid);
        if (importTemplate.isReusableFlowOutputPortConnectionsNeeded()) {
            uploadProgressService
                .addUploadStatus(importTemplateOptions.getUploadKey(), "Additional Port Connection information is necessary for the template " + importTemplate.getTemplateName(), true, false);
        }
        uploadProgressService.completeSection(importTemplateOptions, ImportSection.Section.IMPORT_REUSABLE_TEMPLATE);
        //if valid start output ports

        if (valid) {
            //start all the connections we created
            List<ConnectionDTO> createdConnections = getConnections();
            if (!createdConnections.isEmpty()) {
                UploadProgressMessage
                    message =
                    uploadProgressService
                        .addUploadStatus(importTemplateOptions.getUploadKey(), "Verify and start all input/output port connections for the template " + importTemplate.getTemplateName());
                getConnections().stream().forEach(connectionDTO -> {
                    //verify the Source is started
                    ConnectableDTO connectableDTO = connectionDTO.getSource();
                    boolean started = startConnectablePorts(connectableDTO);
                    //verify the Destination is started
                    connectableDTO = connectionDTO.getDestination();
                    started &= startConnectablePorts(connectableDTO);

                    if (!started) {
                        importTemplate.setSuccess(false);
                    }
                });
                message.complete(importTemplate.isSuccess());
            }
        }
        return valid;
    }

    private boolean connect() {
        NifiProcessGroup processGroup = this.newTemplateInstance;
        UploadProgressMessage
            importStatusMessage =
            uploadProgressService.addUploadStatus(importTemplateOptions.getUploadKey(), "Connecting and validating components for " + importTemplate.getTemplateName());
        connectReusableFlow(importStatusMessage, processGroup);
        recreateOutputPortConnections(importStatusMessage, processGroup);
        boolean valid = validateOutputPortConnections(processGroup);
        importStatusMessage.update("Connected and validated components for " + importTemplate.getTemplateName(), valid);

        //create any remote process group ports and connect them on the main NiFi canvas
        if (isRemoteProcessGroupsEnabled()) {
            ImportComponentOption remoteProcessGroupOption = importTemplateOptions.findImportComponentOption(ImportComponent.REMOTE_INPUT_PORT);
            if (remoteProcessGroupOption.isUserAcknowledged() && remoteProcessGroupOption.isShouldImport()) {
                if (remoteProcessGroupOption.getRemoteProcessGroupInputPortsForTemplate(importTemplate.getTemplateName()).stream().anyMatch(inputPort -> inputPort.isSelected())) {
                    UploadProgressMessage
                        remoteInputPortsMessage =
                        uploadProgressService.addUploadStatus(importTemplateOptions.getUploadKey(),
                                                              "Creating remote input port connections for " + remoteProcessGroupOption
                                                                  .getRemoteProcessGroupInputPortsForTemplate(importTemplate.getTemplateName()).stream()
                                                                  .filter(inputPort -> inputPort.isSelected()).map(inputPort -> inputPort.getInputPortName()).collect(Collectors.joining(",")));

                    valid &= createRemoteInputPorts(remoteInputPortsMessage);
                }
            }

            if (remoteProcessGroupOption.isShouldImport() && remoteProcessGroupOption.isUserAcknowledged()) {
                // identify if the user wished to remove any input ports.
                valid = removeConnectionsAndInputs(remoteProcessGroupOption);
            }
        }

        return valid && newTemplateInstance.isSuccess();
    }


    private Optional<TemplateRemoteInputPortConnections> getExistingRemoteProcessInputPortInformation() {
        return templateConnectionUtil.getRemoteInputPortsForReusableTemplate(importTemplate.getTemplateName());
    }

    private boolean removeRemoteInputPorts(ImportComponentOption remoteProcessGroupOption, Set<String> inputPortNamesToRemove) {

        Optional<TemplateRemoteInputPortConnections> connections = getExistingRemoteProcessInputPortInformation();
        RemoteInputPortService.RemoteInportPortRemovalData removalData = new RemoteInputPortService.RemoteInportPortRemovalData(inputPortNamesToRemove);
        removalData.setConnectionsToRemove(connections.isPresent() ? connections.get() : null);
        removalData.setTemplateName(importTemplate.getTemplateName());
        remoteInputPortService.removeRemoteInputPorts(removalData);
        removalData.getProgressErrorMessages().stream().forEach(msg -> {
            UploadProgressMessage
                importStatusMessage =
                uploadProgressService.addUploadStatus(importTemplateOptions.getUploadKey(),
                                                      msg, true, false);
            importTemplate.setValid(false);
            importTemplate.setSuccess(false);
        });
        removalData.getSevereErrorMessages().stream().forEach(msg -> {
            importTemplate.setValid(false);
            importTemplate.setSuccess(false);
            remoteProcessGroupOption.getErrorMessages().add(msg);
        });
        if (removalData.isSuccess()) {
            UploadProgressMessage
                importStatusMessage =
                uploadProgressService.addUploadStatus(importTemplateOptions.getUploadKey(), removalData.getSuccessMessage(), true, true);

        }
        getItemsCreated().addDeletedRemoteInputPorts(removalData.getDeletedPorts());
        getItemsCreated().addDeletedRemoteInputPortConnections(removalData.getDeletedConnections());
        return removalData.isSuccess();
    }

    private boolean removeConnectionsAndInputs(ImportComponentOption remoteProcessGroupOption) {
        Optional<TemplateRemoteInputPortConnections> existingRemoteProcessInputPortInformation = getExistingRemoteProcessInputPortInformation();
        if (existingRemoteProcessInputPortInformation.isPresent()) {

            //find the input ports that are 'existing' but not 'selected' .  These should be deleted
            Set<String> inputPortNamesToRemove = remoteProcessGroupInputPortMap.values().stream()
                .filter((remoteInputPort -> !remoteInputPort.isSelected()
                                            && existingRemoteProcessInputPortInformation.get().getExistingRemoteInputPortNames().contains(remoteInputPort.getInputPortName())))
                .map(remoteInputPort -> remoteInputPort.getInputPortName())
                .collect(Collectors.toSet());
            return removeRemoteInputPorts(remoteProcessGroupOption, inputPortNamesToRemove);

        }

        return true;
    }

    private static class RemoteProcessGroupStatusHelper {

        public static Stream<RemoteProcessGroupStatusSnapshotEntity> flattened(ProcessGroupStatusSnapshotDTO processGroupStatusSnapshots) {

            Stream<RemoteProcessGroupStatusSnapshotEntity> s = processGroupStatusSnapshots.getRemoteProcessGroupStatusSnapshots().stream();

            return Stream.concat(
                s,
                processGroupStatusSnapshots.getProcessGroupStatusSnapshots().stream().flatMap(snap -> flattened(snap.getProcessGroupStatusSnapshot())));
        }

    }

    private boolean createRemoteInputPorts(UploadProgressMessage
                                               remoteInputPortsMessage) {
        ImportComponentOption remoteProcessGroupOption = importTemplateOptions.findImportComponentOption(ImportComponent.REMOTE_INPUT_PORT);
        String rootProcessGroupId = templateConnectionUtil.getRootProcessGroup().getId();
        String reusableTemplateProcessGroupId = templateConnectionUtil.getReusableTemplateProcessGroupId();

        Map<String, PortDTO> reusableTemplateCategoryPorts = getReusableTemplateCategoryProcessGroup().getContents().getInputPorts().stream().collect(Collectors.toMap(p -> p.getName(), p -> p));

        StringBuffer connectedStr = new StringBuffer("");

        remoteProcessGroupOption.getRemoteProcessGroupInputPortsForTemplate(importTemplate.getTemplateName()).stream().filter(r -> r.isSelected() && !r.isExisting()).forEach(r -> {

            //check/create the port at the parent canvas
            PortDTO portDTO = new PortDTO();
            portDTO.setName(r.getInputPortName());
            portDTO.setType(NifiConstants.INPUT_PORT);
            portDTO.setState(NifiProcessUtil.PROCESS_STATE.STOPPED.name());

            PortDTO newInputPort = nifiRestClient.getNiFiRestClient().processGroups().createInputPort(rootProcessGroupId, portDTO);
            getItemsCreated().addCreatedRemoteInputPort(newInputPort);
            PortDTO reusableTemplatePort = reusableTemplateCategoryPorts.get(r.getInputPortName());

            //connect this to the Reusable Template input port with the same name
            ConnectableDTO source = new ConnectableDTO();
            source.setGroupId(rootProcessGroupId);
            source.setId(newInputPort.getId());
            source.setName(newInputPort.getName());
            source.setType(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name());
            ConnectableDTO dest = new ConnectableDTO();
            dest.setGroupId(reusableTemplateProcessGroupId);
            dest.setName(r.getInputPortName());
            dest.setId(reusableTemplatePort.getId());
            dest.setType(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name());
            ConnectionDTO connectionDTO = nifiRestClient.getNiFiRestClient().processGroups().createConnection(rootProcessGroupId, source, dest);
            getItemsCreated().addCreatedRemoteInputPortConnection(connectionDTO);
            if (connectedStr.length() != 0) {
                connectedStr.append(",");
            } else {
                connectedStr.append("Created ");
            }


            //enable the input port
            try {
                newInputPort.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
                nifiRestClient.getNiFiRestClient().ports().updateInputPort(newInputPort.getParentGroupId(), newInputPort);
            }catch (Exception e) {

            }
            connectedStr.append(r.getInputPortName());

            remoteInputPortsMessage.update(connectedStr.toString());

        });
        if (connectedStr.length() != 0) {
            connectedStr.append(" as remote input ports");
        }
        if (connectedStr.length() > 0) {
            remoteInputPortsMessage.update(connectedStr.toString(), true);
        } else {
            remoteInputPortsMessage.complete(true);
        }
        return true;
    }


    public boolean rollback() {
        rollbackTemplateImportInNifi();
        rollbackCreatedItems();
        return true;
    }

    /**
     * Rollback and delete any created items
     * restore any items that were deleted
     */
    private void rollbackCreatedItems() {
        getItemsCreated().getCreatedRemoteInputPortConnections().stream().forEach(connection -> nifiRestClient.deleteConnection(connection, false));
        getItemsCreated().getCreatedRemoteInputPorts().stream().forEach(portDTO -> nifiRestClient.getNiFiRestClient().ports().deleteInputPort(portDTO.getId()));

        Map<String, String> oldToNewPortIdMap = new HashMap<>();
        getItemsCreated().getDeletedRemoteInputPorts().stream().forEach(portDTO -> {
            PortDTO createdPort = nifiRestClient.getNiFiRestClient().processGroups().createInputPort(portDTO.getParentGroupId(), portDTO);
            oldToNewPortIdMap.put(portDTO.getId(), createdPort.getId());
        });
        //find matching connection

        Map<String, String> newReusableTemplatePortNameToId = new HashMap<>();

        String reusableTemplateProcessGroupId = templateConnectionUtil.getReusableTemplateProcessGroupId();
        if (reusableTemplateProcessGroupId != null) {
            ProcessGroupFlowDTO reusableTemplateFlow = nifiRestClient.getNiFiRestClient().processGroups().flow(reusableTemplateProcessGroupId);
            String templateProcessGroupId = reusableTemplateFlow.getFlow().getProcessGroups().stream().filter(e -> e.getComponent().getName().equalsIgnoreCase(this.importTemplate.getTemplateName()))
                .map(e -> e.getComponent().getId()).findFirst().orElse(null);
            if (templateProcessGroupId != null) {
                reusableTemplateFlow.getFlow().getConnections().stream().filter(connectionEntity ->
                                                                                    connectionEntity.getComponent().getDestination().getGroupId().equalsIgnoreCase(templateProcessGroupId)
                                                                                    && connectionEntity.getComponent().getSource().getType().equalsIgnoreCase(NifiConstants.INPUT_PORT)
                ).forEach(connectionEntity -> {
                    newReusableTemplatePortNameToId.put(connectionEntity.getComponent().getSource().getName(), connectionEntity.getComponent().getSource().getId());
                });
            }
        }

        String rootProcessGroupId = templateConnectionUtil.getRootProcessGroup().getId();
        getItemsCreated().getDeletedRemoteInputPortConnections().stream().forEach(connectionDTO -> {
            String newId = oldToNewPortIdMap.get(connectionDTO.getSource().getId());
            connectionDTO.getSource().setId(newId);
            String newDestId = newReusableTemplatePortNameToId.get(connectionDTO.getSource().getName());
            connectionDTO.getDestination().setId(newDestId);
            ConnectionDTO restoredConnection = nifiRestClient.getNiFiRestClient().processGroups().createConnection(rootProcessGroupId, connectionDTO.getSource(), connectionDTO.getDestination());

        });

    }


    public void cleanup() {
        if (newTemplateInstance != null) {
            String templateName = importTemplate.getTemplateName();
            VersionedProcessGroup versionedProcessGroup = newTemplateInstance.getVersionedProcessGroup();
            if (versionedProcessGroup != null) {
                //ensure we have contents in the versionedProcess group
                if (versionedProcessGroup.getVersionedProcessGroup().getContents() == null) {
                    FlowDTO flowDTO = nifiRestClient.getNiFiRestClient().processGroups().flow(versionedProcessGroup.getVersionedProcessGroup().getId()).getFlow();
                    if (flowDTO != null) {
                        versionedProcessGroup.getVersionedProcessGroup().setContents(NifiFlowUtil.flowToFlowSnippet(flowDTO));
                    }
                }
                //importTemplate.getTemplateResults().getVersionedProcessGroup().getVersionedProcessGroup()
                uploadProgressService
                    .addUploadStatus(importTemplateOptions.getUploadKey(), "The Reusable template " + templateName + " is valid.  Attempting to clean up and remove the previous instance ", true,
                                     true);

                //if the versioned group doesnt have anything in queue delete it
                Optional<ProcessGroupStatusDTO> status = nifiRestClient.getNiFiRestClient().processGroups().getStatus(versionedProcessGroup.getVersionedProcessGroup().getId());
                if (!status.isPresent() || (status.isPresent() && status.get().getAggregateSnapshot().getFlowFilesQueued() > 0)) {
                    uploadProgressService.addUploadStatus(importTemplateOptions.getUploadKey(),
                                                          "Warning! The previous version of this template '" + templateName + "', now located in NiFi under the process group '" + versionedProcessGroup
                                                              .getVersionedProcessGroupName() + "' still has items in queue.  Unable to delete it. ", true, true);
                    //if the versioned group still had items in queue then wire it back up to the newly created group
                    //?? are they still wired up to the output ports
                    log.info("Unable to delete versioned group '{}'  Items are still in queue", versionedProcessGroup.getVersionedProcessGroupName());
                    //stop the inputs and mark the rest as running
                    versionedProcessGroup.getVersionedProcessGroup().getContents().getInputPorts().stream().forEach(portDTO -> {
                        nifiRestClient.stopInputPort(versionedProcessGroup.getVersionedProcessGroup().getId(), portDTO.getId());
                    });

                    //start the output ports
                    versionedProcessGroup.getVersionedProcessGroup().getContents().getOutputPorts().stream().forEach(portDTO -> {
                        nifiRestClient.startOutputPort(versionedProcessGroup.getVersionedProcessGroup().getId(), portDTO.getId());
                    });
                } else {
                    //delete the versioned group
                    try {
                        nifiRestClient.removeProcessGroup(importTemplate.getTemplateResults().getVersionedProcessGroup().getVersionedProcessGroup().getId(),
                                                          importTemplate.getTemplateResults().getVersionedProcessGroup().getVersionedProcessGroup().getParentGroupId());
                    } catch (NifiClientRuntimeException e) {
                        log.error("Template has sucessfully imported, but Kylo is unable to remove versioned process group '{}' with id '{}'.  You will need to go into NiFi and manually remove it.",
                                  versionedProcessGroup.getVersionedProcessGroupName(), versionedProcessGroup.getVersionedProcessGroup().getId());
                        uploadProgressService.addUploadStatus(importTemplateOptions.getUploadKey(),
                                                              "Warning! Unable to remove the previous version of this template, process group: '" + versionedProcessGroup.getVersionedProcessGroupName()
                                                              + "' with id of " + versionedProcessGroup.getVersionedProcessGroup().getId() + ".  You will need to manually delete it from NiFi. ", true,
                                                              false);
                    }
                }
            }
        }
    }

    private Set<ConnectionDTO> getRootProcessGroupConnections() {
        String rootProcessGroupId = templateConnectionUtil.getRootProcessGroup().getId();
        Set<ConnectionDTO> rootConnections = nifiRestClient.getNiFiRestClient().processGroups().getConnections(rootProcessGroupId);
        return rootConnections;
    }


    /**
     * Get and cache the Reusable template process group
     */
    private Optional<ProcessGroupFlowDTO> getReusableTemplatesProcessGroup() {
        if (reusableTemplateFlow == null) {
            String reusableTemplateProcessGroupId = templateConnectionUtil.getReusableTemplateProcessGroupId();
            if (reusableTemplateProcessGroupId != null) {
                try {
                    reusableTemplateFlow = nifiRestClient.getNiFiRestClient().processGroups().flow(reusableTemplateProcessGroupId);
                } catch (Exception e) {
                    log.error("Unable to get Reusable Process Group flow from cache.  Attempted to reset the cache ", e.getMessage());
                    templateConnectionUtil.resetReusableTemplateProcessGroupCache();
                }
            }
        }
        if (reusableTemplateFlow == null) {
            return Optional.empty();
        }
        return Optional.of(reusableTemplateFlow);
    }

    private Set<PortDTO> getReusableTemplateInputPorts() {
        if (getReusableTemplatesProcessGroup().isPresent()) {
            return getReusableTemplatesProcessGroup().get().getFlow().getInputPorts().stream().map(portEntity -> portEntity.getComponent()).collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }

    }

    private Set<ConnectionDTO> getReusableTemplateConnections() {
        if (getReusableTemplatesProcessGroup().isPresent()) {
            return getReusableTemplatesProcessGroup().get().getFlow().getConnections().stream().map(connectionEntity -> connectionEntity.getComponent()).collect(
                Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }

    private Optional<ProcessGroupDTO> findReusableTemplateProcessGroup(String groupId) {

        if (getReusableTemplatesProcessGroup().isPresent()) {
            return getReusableTemplatesProcessGroup().get().getFlow().getProcessGroups().stream().map(processGroupEntity -> processGroupEntity.getComponent())
                .filter(processGroupDTO -> processGroupDTO.getId().equals(groupId))
                .findFirst();
        } else {
            return Optional.empty();
        }
    }


    @Nullable
    private String getReusableTemplatesProcessGroupId() {
        if (getReusableTemplatesProcessGroup().isPresent()) {
            return getReusableTemplatesProcessGroup().get().getId();
        } else {
            return null;
        }
    }


    /**
     * For the given outputPort that is connected to another reusable template, find the associated 'input port' connection to the destination of the output port.
     */
    @Nullable
    private ConnectionDTO findReusableTemplateInputPortConnectionForOutputPort(PortDTO outputPort) {
        ConnectionDTO reusableTemplateInputPortConnection = null;
        //attempt to prefill it with the previous connection if it existed
        //1 find the connection going from this output port to the other process group under the 'reusableTemplates'
        if (getReusableTemplatesProcessGroup().isPresent()) {
            ConnectionDTO otherConnection = getReusableTemplatesProcessGroup().get().getFlow().getConnections().stream().map(connectionEntity -> connectionEntity.getComponent())
                .filter(conn -> conn.getSource().getName().equalsIgnoreCase(outputPort.getName())).findFirst().orElse(null);
            if (otherConnection != null) {
                //2 find the connection whose destination is the destination of 'otherConnection' and whose source is an 'INPUT_PORT' residing under the 'reusableTemplate' process group
                reusableTemplateInputPortConnection = getReusableTemplatesProcessGroup().get().getFlow().getConnections().stream().map(connectionEntity -> connectionEntity.getComponent())
                    .filter(conn -> conn.getDestination().getId().equals(otherConnection.getDestination().getId()) && conn.getSource().getType().equals(NifiConstants.INPUT_PORT)
                                    && conn.getSource().getGroupId().equalsIgnoreCase(getReusableTemplatesProcessGroupId())).findFirst().orElse(null);
            }
        }
        return reusableTemplateInputPortConnection;
    }


    private void recreateOutputPortConnections(UploadProgressMessage importStatusMessage, NifiProcessGroup newTemplateInstance) {
        VersionedProcessGroup versionedProcessGroup = newTemplateInstance.getVersionedProcessGroup();
        String templateName = importTemplate.getTemplateName();
        //Recreate any output port connections that existed before that were connecting into this template
        //Source == output port in some other group
        //Dest == input port in versioned off process group
        if (versionedProcessGroup != null) {
            String reusableTemplateProcessGroupId = getReusableTemplatesProcessGroupId();
            if (reusableTemplateProcessGroupId != null) {
                for (ConnectionDTO connectionDTO : versionedProcessGroup.getDeletedInputPortConnections()) {
                    if (connectionDTO.getSource().getType().equals(NifiConstants.OUTPUT_PORT)) {
                        //connect
                        PortDTO
                            destPort =
                            newTemplateInstance.getProcessGroupEntity().getContents().getInputPorts().stream().filter(
                                portDTO -> portDTO.getName().equalsIgnoreCase(connectionDTO.getDestination().getName()) && connectionDTO.getDestination().getGroupId()
                                    .equalsIgnoreCase(newTemplateInstance.getVersionedProcessGroup().getProcessGroupPriorToVersioning().getId())).findFirst().orElse(null);
                        if (destPort != null) {
                            //make the connection now from the output port to the 'connectionToUse' destination
                            ConnectableDTO source = NifiConnectionUtil.asNewConnectable(connectionDTO.getSource());
                            ConnectableDTO dest = NifiConnectionUtil.asConnectable(destPort);
                            ConnectionDTO newConnection = nifiRestClient.getNiFiRestClient().processGroups().createConnection(reusableTemplateProcessGroupId, source, dest);
                            connections.add(newConnection);
                            //possibly store the ports too?

                            log.info("Reconnected output port {} ({}) to this new process group input port:  {} {{}) ", source.getName(), source.getId(), dest.getName(), dest.getId());
                        } else {
                            //ERROR cant recreate previous connections that were going into this reusable template
                            String
                                msg =
                                "Unable to recreate the connection for template: " + templateName
                                + " that was previously connected to this template prior to the update. The following connection is missing:  Connecting ['" + connectionDTO.getSource().getName()
                                + "' to '" + connectionDTO.getDestination().getName() + "'].";
                            log.error(msg);
                            importTemplate.getTemplateResults().addError(NifiError.SEVERITY.FATAL, msg, "");
                            importStatusMessage.update(
                                "Unable to establish prior connection for reusable template: " + templateName + ".  Connection:  ['" + connectionDTO.getSource().getName() + "' to '" + connectionDTO
                                    .getDestination().getName() + "']", false);
                            break;
                        }
                    }
                }
            }
        }
    }


    /**
     * Restore the previous Template back to Nifi
     */
    private void rollbackTemplateImportInNifi() {

        UploadProgressMessage rollbackMessage = restoreOldTemplateXml();

        //If we are working with a reusable flow we need to recreate the old one
        if (importTemplate.getTemplateResults() != null && importTemplate.getTemplateResults().isReusableFlowInstance()) {
            UploadProgressMessage
                progressMessage =
                uploadProgressService.addUploadStatus(importTemplate.getImportOptions().getUploadKey(), "Attempting to restore old instance for: " + importTemplate.getTemplateName());

            VersionedProcessGroup versionedProcessGroup = null;

            if (importTemplate.getTemplateResults().getVersionedProcessGroup() != null) {
                versionedProcessGroup = importTemplate.getTemplateResults().getVersionedProcessGroup();
            }
            // rename the one we created to a temp name
            ProcessGroupDTO groupDTO = nifiRestClient.getNiFiRestClient().processGroups().findById(importTemplate.getTemplateResults().getProcessGroupEntity().getId(), false, false).orElse(null);
            if (groupDTO != null) {
                String tmpName = groupDTO.getName() + "- " + System.currentTimeMillis();
                groupDTO.setName(tmpName);
                nifiRestClient.getNiFiRestClient().processGroups().update(groupDTO);
                log.info("Rollback Template: {}.  Renamed template instance that was just created to a temporary name of {}.  This will get deleted later. ", importTemplate.getTemplateName(),
                         tmpName);
            }
            if (versionedProcessGroup != null) {
                progressMessage.update("Rollback Status: Attempting to initialize and verify prior template instance for " + importTemplate.getTemplateName());
                //rename the versioned one back
                ProcessGroupDTO
                    oldProcessGroup =
                    nifiRestClient.getNiFiRestClient().processGroups().findById(versionedProcessGroup.getProcessGroupPriorToVersioning().getId(), true, true).orElse(null);
                if (oldProcessGroup != null) {
                    oldProcessGroup.setName(versionedProcessGroup.getProcessGroupName());
                    nifiRestClient.getNiFiRestClient().processGroups().update(oldProcessGroup);
                    progressMessage
                        .update("Rollback Status: Renamed template process group " + versionedProcessGroup.getVersionedProcessGroupName() + " back to " + versionedProcessGroup.getProcessGroupName());
                }

                //add back in the connections
                List<ConnectionDTO> createdConnections = new ArrayList<>();
                List<ConnectionDTO> connections = versionedProcessGroup.getDeletedInputPortConnections();
                if (connections != null) {
                    connections.stream().forEach(connectionDTO -> {
                        createdConnections
                            .add(nifiRestClient.getNiFiRestClient().processGroups().createConnection(connectionDTO.getParentGroupId(), connectionDTO.getSource(), connectionDTO.getDestination()));
                    });
                    uploadProgressService.addUploadStatus(importTemplate.getImportOptions().getUploadKey(), "Rollback Status: Recreated " + createdConnections.size() + " connections ", true, true);
                }

                List<ProcessorDTO> inputs = versionedProcessGroup.getInputProcessorsPriorToDisabling();
                if (inputs != null) {
                    //update the state
                    progressMessage.update("Rollback Status: Marking the process group " + versionedProcessGroup.getProcessGroupName() + " as running");
                }
            }
            if (groupDTO != null) {
                progressMessage.update("Rollback Status: Removing invalid template instance process group:  " + groupDTO.getName());
                //delete the new one
                try {
                    nifiRestClient.removeProcessGroup(groupDTO.getId(), groupDTO.getParentGroupId());
                } catch (Exception e) {
                    log.error("Error trying to remove invalid template instance {}", groupDTO.getName(), e);
                }

                Optional<ProcessGroupDTO> deletedGroup = nifiRestClient.getNiFiRestClient().processGroups().findById(groupDTO.getId(), false, false);
                if (deletedGroup.isPresent()) {
                    progressMessage.update("Rollback Status: Failure", false);
                    rollbackMessage.update(
                        "Rollback Unsuccessful!!  The invalid group " + deletedGroup.get().getName() + " still exists.  You will need to login to NiFi and verify your reusable templates are correct!",
                        false);
                } else {
                    String message = "Rollback Status: Success.";
                    if (versionedProcessGroup != null) {
                        message += " Restored '" + versionedProcessGroup.getVersionedProcessGroupName() + "' back to '" + importTemplate.getTemplateName() + "'";
                    }
                    progressMessage.update(message, true);
                    rollbackMessage.update("Rollback Successful!", true);
                }
            }


        } else {
            rollbackMessage.update("Rollback Successful!", true);
        }

    }

    private boolean validateOutputPortConnections(NifiProcessGroup
                                                      newTemplateInstance) {
        //Validate port connections
        newTemplateInstance.getProcessGroupEntity().getContents().getOutputPorts().stream().forEach(portDTO -> {
            if (portDTO.getValidationErrors() != null && !portDTO.getValidationErrors().isEmpty()) {
                importTemplate.setReusableFlowOutputPortConnectionsNeeded(true);
            }
            ReusableTemplateConnectionInfo connectionInfo = new ReusableTemplateConnectionInfo();
            connectionInfo.setFeedOutputPortName(portDTO.getName());
            //attempt to prefill it with the previous connection if it existed
            ConnectionDTO reusableTemplateInputPortConnection = findReusableTemplateInputPortConnectionForOutputPort(portDTO);
            if (reusableTemplateInputPortConnection != null) {
                connectionInfo.setInputPortDisplayName(reusableTemplateInputPortConnection.getSource().getName());
                connectionInfo.setReusableTemplateInputPortName(reusableTemplateInputPortConnection.getSource().getName());
                String processGroupName = findReusableTemplateProcessGroup(reusableTemplateInputPortConnection.getDestination().getGroupId())
                    .map(processGroupDTO -> processGroupDTO.getName()).orElse(null);
                connectionInfo.setReusableTemplateProcessGroupName(processGroupName);
            }
            importTemplate.addReusableTemplateConnection(connectionInfo);
        });
        return importTemplate.isSuccess() && importTemplate.isValid() && !importTemplate.isReusableFlowOutputPortConnectionsNeeded();
    }

    private ProcessGroupDTO connectReusableFlow(UploadProgressMessage importStatusMessage, NifiProcessGroup newTemplateInstance) {
        String templateName = this.importTemplate.getTemplateName();
        ImportComponentOption componentOption = importTemplateOptions.findImportComponentOption(ImportComponent.TEMPLATE_CONNECTION_INFORMATION);
        ProcessGroupDTO processGroupDTO = newTemplateInstance.getProcessGroupEntity();
        Set<PortDTO> inputPorts = getReusableTemplateInputPorts();
        Set<ConnectionDTO> reusableTemplateConnections = getReusableTemplateConnections();

        if (componentOption != null && !componentOption.getConnectionInfo().isEmpty()) {
            //connect the output port to the input port
            //we are connecting a reusable template back to another reusable template

            //follow the input port destination connection to its internal process group port
            Set<ConnectionDTO> newConnections = new HashSet<>();
            String reusableTemplateProcessGroupId = getReusableTemplatesProcessGroupId();
            if (reusableTemplateProcessGroupId != null) {
                for (ReusableTemplateConnectionInfo connectionInfo : componentOption.getConnectionInfo()) {
                    String reusableTemplateInputPortName = connectionInfo.getReusableTemplateInputPortName();
                    //find the portdto matching this name in the reusable template group

                    /**
                     * The connection coming from the 'reusableTemplateInputPortName' to the next input port
                     */
                    Optional<ConnectionDTO> connectionToUse = Optional.empty();

                    /**
                     * The port that matches the 'reusableTemplateInputPortName connection destination
                     */
                    Optional<PortDTO> sourcePort = Optional.empty();

                    connectionToUse = inputPorts.stream().filter(portDTO -> portDTO.getName().equalsIgnoreCase(reusableTemplateInputPortName))
                        .findFirst()
                        .flatMap(portToInspect ->
                                     reusableTemplateConnections.stream()
                                         .filter(connectionDTO -> connectionDTO.getDestination().getType().equalsIgnoreCase(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name())
                                                                  && connectionDTO.getSource().getId().equalsIgnoreCase(portToInspect.getId())).findFirst()
                        );

                    if (connectionToUse.isPresent()) {
                        sourcePort =
                            newTemplateInstance.getProcessGroupEntity().getContents().getOutputPorts().stream()
                                .filter(portDTO -> portDTO.getName().equalsIgnoreCase(connectionInfo.getFeedOutputPortName())).findFirst();

                    }

                    if (sourcePort.isPresent()) {
                        //make the connection now from the output port to the 'connectionToUse' destination
                        ConnectableDTO source = NifiConnectionUtil.asConnectable(sourcePort.get());
                        ConnectableDTO dest = NifiConnectionUtil.asNewConnectable(connectionToUse.get().getDestination());
                        ConnectionDTO newConnection = nifiRestClient.getNiFiRestClient().processGroups().createConnection(reusableTemplateProcessGroupId, source, dest);
                        newConnections.add(newConnection);
                        connections.add(newConnection);
                        log.info("Connected the output port {} ({}) to another reusable template input port: {} {{}).  The public reusable template port name is: {} ", source.getName(),
                                 source.getId(),
                                 dest.getName(), dest.getId(), reusableTemplateInputPortName);
                        uploadProgressService.addUploadStatus(importTemplateOptions.getUploadKey(),
                                                              "Connected this template '" + templateName + "' with '" + reusableTemplateInputPortName + "' connected to '" + sourcePort.get().getName()
                                                              + "'", true, true);
                    } else {
                        //    log.error("Unable to find a connection to connect the reusable template together.  Please verify the Input Port named '{}' under the 'reusable_templates' group has a connection going to another input port",reusableTemplateInputPortName);
                        //    importTemplate.getTemplateResults().addError(NifiError.SEVERITY.FATAL, "Unable to connect the reusable template to the designated input port: '" + reusableTemplateInputPortName + "'. Please verify the Input Port named '" + reusableTemplateInputPortName + "' under the 'reusable_templates' group has a connection going to another input port.  You may need to re-import the template with this input port. ", "");
                        //   importStatusMessage.update("Unable to connect the reusable template to the designated input port: "+reusableTemplateInputPortName, false);
                        //  uploadProgressService.addUploadStatus(importTemplateOptions.getUploadKey(), "Unable to connect this template '"+templateName+"' with '"+reusableTemplateInputPortName,true,false);
                        //   break;
                    }
                }
            }

            if (!newConnections.isEmpty()) {
                //requery for the ports to check validity again
                processGroupDTO = nifiRestClient.getNiFiRestClient().processGroups().findById(newTemplateInstance.getProcessGroupEntity().getId(), false, true).orElse(processGroupDTO);
                //reset it back to the newTemplateInstance
                newTemplateInstance.updateProcessGroupContent(processGroupDTO);
            }
        }
        return processGroupDTO;
    }

    public List<ConnectionDTO> getConnections() {
        return connections != null ? connections : Collections.emptyList();
    }

    public class NifiFlowCacheReusableTemplateCreationCallback implements ReusableTemplateCreationCallback {

        public NifiFlowCacheReusableTemplateCreationCallback() {
        }

        /**
         * Update the NiFi Flow Cache with the new processors information
         *
         * @param templateName    the name of the template
         * @param processGroupDTO the group where this template resides (under the reusable_templates) group
         */
        @Override
        public void beforeMarkAsRunning(String templateName, ProcessGroupDTO processGroupDTO) {
            //update the cache
            Collection<ProcessorDTO> processors = NifiProcessUtil.getProcessors(processGroupDTO);
            nifiFlowCache.updateProcessorIdNames(templateName, processors);
            nifiFlowCache.updateConnectionMap(templateName, NifiConnectionUtil.getAllConnections(processGroupDTO));
        }

    }


    /**
     * Class to track additional items created outside of the template import
     */
    private class ItemsCreated {

        private List<PortDTO> createdRemoteInputPorts;
        private List<ConnectionDTO> createdRemoteInputPortConnections;

        private List<PortDTO> deletedRemoteInputPorts;
        private List<ConnectionDTO> deletedRemoteInputPortConnections;

        public List<PortDTO> getCreatedRemoteInputPorts() {
            if (createdRemoteInputPorts == null) {
                createdRemoteInputPorts = new ArrayList<>();
            }
            return createdRemoteInputPorts;
        }

        public List<ConnectionDTO> getCreatedRemoteInputPortConnections() {
            if (createdRemoteInputPortConnections == null) {
                createdRemoteInputPortConnections = new ArrayList<>();
            }
            return createdRemoteInputPortConnections;
        }

        public List<PortDTO> getDeletedRemoteInputPorts() {
            if (deletedRemoteInputPorts == null) {
                deletedRemoteInputPorts = new ArrayList<>();
            }
            return deletedRemoteInputPorts;
        }

        public List<ConnectionDTO> getDeletedRemoteInputPortConnections() {
            if (deletedRemoteInputPortConnections == null) {
                deletedRemoteInputPortConnections = new ArrayList<>();
            }
            return deletedRemoteInputPortConnections;
        }

        public void addCreatedRemoteInputPort(PortDTO portDTO) {
            getCreatedRemoteInputPorts().add(portDTO);
        }

        public void addCreatedRemoteInputPortConnection(ConnectionDTO connectionDTO) {
            getCreatedRemoteInputPortConnections().add(connectionDTO);
        }

        public void addDeletedRemoteInputPort(PortDTO portDTO) {
            getDeletedRemoteInputPorts().add(portDTO);
        }

        public void addDeletedRemoteInputPorts(List<PortDTO> ports) {
            getDeletedRemoteInputPorts().addAll(ports);
        }

        public void addDeletedRemoteInputPortConnections(List<ConnectionDTO> connections) {
            getDeletedRemoteInputPortConnections().addAll(connections);
        }

        public void addDeletedRemoteInputPortConnection(ConnectionDTO connectionDTO) {
            getDeletedRemoteInputPortConnections().add(connectionDTO);
        }


    }


}





