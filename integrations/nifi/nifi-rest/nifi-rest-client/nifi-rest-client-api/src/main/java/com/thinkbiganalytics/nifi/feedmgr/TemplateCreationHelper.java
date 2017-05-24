package com.thinkbiganalytics.nifi.feedmgr;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import com.google.common.base.Predicate;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.model.NiFiAllowableValue;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiTemplateNameUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ConnectionDTO;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.FlowSnippetDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;

/**
 * Helper class used to create templates in NiFi
 * This class is used with the {@link TemplateInstanceCreator}
 */
public class TemplateCreationHelper {

    public static final String REUSABLE_TEMPLATES_CATEGORY_NAME = "Reusable Templates";
    private static final Logger log = LoggerFactory.getLogger(TemplateCreationHelper.class);
    public static String REUSABLE_TEMPLATES_PROCESS_GROUP_NAME = "reusable_templates";

    public static String TEMPORARY_TEMPLATE_INSPECTION_GROUP_NAME = "kylo_temporary_template_inspection";
    /**
     * REST client for NiFi API
     */
    @Nonnull
    private final NiFiRestClient nifiRestClient;
    LegacyNifiRestClient restClient;
    private List<NifiError> errors = new ArrayList<>();
    private Set<ControllerServiceDTO> snapshotControllerServices;

    private Set<ControllerServiceDTO> snapshottedEnabledControllerServices = new HashSet<>();

    private Map<String, ControllerServiceDTO> mergedControllerServices;

    private Set<ControllerServiceDTO> newlyCreatedControllerServices;

    public TemplateCreationHelper(LegacyNifiRestClient restClient) {
        this.restClient = restClient;
        this.nifiRestClient = restClient.getNiFiRestClient();
    }


    public static String getVersionedProcessGroupName(String name) {
        return NifiTemplateNameUtil.getVersionedProcessGroupName(name);
    }

    public static String parseVersionedProcessGroupName(String name) {
        return NifiTemplateNameUtil.parseVersionedProcessGroupName(name);
    }


    /**
     * Creates an instance of the supplied template under the temporary inspection group inside its own process group
     *
     * @param templateId the template to instantiate
     * @return the process group holding this template
     */
    public ProcessGroupDTO createTemporaryTemplateFlow(@Nonnull final String templateId) {
        ProcessGroupDTO temporaryTemplateInspectionGroup = null;
        //first get the parent temp group
        Optional<ProcessGroupDTO> group = nifiRestClient.processGroups().findByName("root", TEMPORARY_TEMPLATE_INSPECTION_GROUP_NAME, false, false);
        if (!group.isPresent()) {
            temporaryTemplateInspectionGroup = nifiRestClient.processGroups().create("root", TEMPORARY_TEMPLATE_INSPECTION_GROUP_NAME);
        } else {
            temporaryTemplateInspectionGroup = group.get();
        }

        //next create the temp group
        snapshotControllerServiceReferences();
        ProcessGroupDTO tempGroup = nifiRestClient.processGroups().create(temporaryTemplateInspectionGroup.getId(), "template_" + System.currentTimeMillis());
        FlowSnippetDTO snippet = instantiateFlowFromTemplate(tempGroup.getId(), templateId);
        identifyNewlyCreatedControllerServiceReferences();
        tempGroup.setContents(snippet);

        //now delete it
        nifiRestClient.processGroups().delete(tempGroup);
        cleanupControllerServices();

        return tempGroup;
    }

    /**
     * Instantiates the specified template in the specified process group.
     *
     * <p>Controller services that are created under the specified process group will be moved to the root process group. This side-effect may be removed in the future.</p>
     *
     * @param processGroupId the process group id
     * @param templateId     the template id
     * @return the instantiated flow
     * @throws NifiComponentNotFoundException if the process group or template does not exist
     */
    @Nonnull
    public FlowSnippetDTO instantiateFlowFromTemplate(@Nonnull final String processGroupId, @Nonnull final String templateId) throws NifiComponentNotFoundException {
        // Instantiate template
        final NiFiRestClient nifiClient = restClient.getNiFiRestClient();
        final FlowSnippetDTO templateFlow = nifiClient.processGroups().instantiateTemplate(processGroupId, templateId);

        // Move controller services to root process group (NiFi >= v1.0)
        final Set<ControllerServiceDTO> groupControllerServices = nifiClient.processGroups().getControllerServices(processGroupId);
        final Map<String, String> idMap = new HashMap<>(groupControllerServices.size());

        groupControllerServices.stream()
            .filter(controllerService -> controllerService.getParentGroupId().equals(processGroupId))
            .forEach(groupControllerService -> {
                // Delete scoped service
                final String oldId = groupControllerService.getId();
                nifiClient.controllerServices().delete(groupControllerService.getId());

                // Create root service
                final ControllerServiceDTO rootControllerService = new ControllerServiceDTO();
                rootControllerService.setComments(groupControllerService.getComments());
                rootControllerService.setName(groupControllerService.getName());
                rootControllerService.setType(groupControllerService.getType());
                final String rootId = nifiClient.processGroups().createControllerService("root", rootControllerService).getId();

                // Map old ID to new ID
                idMap.put(oldId, rootId);
            });

        // Set properties on root controller services
        groupControllerServices.stream()
            .filter(controllerService -> controllerService.getParentGroupId().equals(processGroupId))
            .forEach(groupControllerService -> {
                final Map<String, String> properties = groupControllerService.getProperties();
                groupControllerService.getDescriptors().values().stream()
                    .filter(descriptor -> StringUtils.isNotBlank(descriptor.getIdentifiesControllerService()))
                    .forEach(descriptor -> {
                        final String name = descriptor.getName();
                        final String oldId = properties.get(name);
                        properties.put(name, idMap.get(oldId));
                    });

                final ControllerServiceDTO rootControllerService = new ControllerServiceDTO();
                rootControllerService.setId(idMap.get(groupControllerService.getId()));
                rootControllerService.setProperties(properties);
                nifiClient.controllerServices().update(rootControllerService);
            });

        // Return flow
        return templateFlow;
    }

    public void snapshotControllerServiceReferences() throws TemplateCreationException {
        Set<ControllerServiceDTO> controllerServiceEntity = restClient.getControllerServices();
        if (controllerServiceEntity != null) {
            snapshotControllerServices = controllerServiceEntity;
            for (ControllerServiceDTO serviceDTO : controllerServiceEntity) {
                if (serviceDTO.getState().equals(NifiProcessUtil.SERVICE_STATE.ENABLED.name())) {
                    snapshottedEnabledControllerServices.add(serviceDTO);
                }
            }
        }
    }

    public List<NifiError> getErrors() {
        return errors;
    }

    /**
     * Compare the services in Nifi with the ones from the snapshot and return any that are not in the snapshot
     */
    public Set<ControllerServiceDTO> identifyNewlyCreatedControllerServiceReferences() {
        Set<ControllerServiceDTO> newServices = new HashSet<>();
        Set<ControllerServiceDTO> controllerServiceEntity = restClient.getControllerServices();
        if (controllerServiceEntity != null) {
            if (snapshotControllerServices != null) {
                for (ControllerServiceDTO dto : controllerServiceEntity) {
                    if (!snapshotControllerServices.contains(dto)) {
                        newServices.add(dto);
                    }
                }
            } else {
                newServices = controllerServiceEntity;
            }
        }
        newlyCreatedControllerServices = newServices;

        mergeControllerServices();
        return newServices;
    }

    /**
     * Tries to enable the specified controller service.
     *
     * @param controllerService the controller service to enable
     * @param properties        property overrides for the controller service
     * @param enabledServices   map of enabled controller service ids and names to DTOs
     * @param allServices       map of all controller service ids to
     * @return the enabled controller service
     * @throws NifiComponentNotFoundException if the controller service does not exist
     * @throws WebApplicationException        if the controller service cannot be enabled
     */
    @Nonnull
    private ControllerServiceDTO tryToEnableControllerService(@Nonnull final ControllerServiceDTO controllerService, @Nullable final Map<String, String> properties,
                                                              @Nonnull final Map<String, ControllerServiceDTO> enabledServices, @Nonnull final Map<String, ControllerServiceDTO> allServices) {
        // Check if already enabled
        if ("ENABLED".equals(controllerService.getState())) {
            return controllerService;
        }

        // Fix controller service references
        final NiFiPropertyDescriptorTransform propertyDescriptorTransform = restClient.getPropertyDescriptorTransform();
        final List<NifiProperty> changedProperties = fixControllerServiceReferences(properties, enabledServices, allServices,
                                                                                    NifiPropertyUtil.getPropertiesForService(controllerService, propertyDescriptorTransform));
        if (!changedProperties.isEmpty()) {
            changedProperties.forEach(property -> {
                controllerService.getProperties().put(property.getKey(), property.getValue());
            });
            nifiRestClient.controllerServices().update(controllerService);
        }

        // Enable controller service
        return restClient.enableControllerServiceAndSetProperties(controllerService.getId(), properties);
    }

    private void mergeControllerServices() {

        final Map<String, ControllerServiceDTO> map = new HashMap<String, ControllerServiceDTO>();
        final Map<String, List<ControllerServiceDTO>> serviceNameMap = new HashMap<>();
        //first use the snapshotted servies as a baseline
        for (ControllerServiceDTO serviceDTO : snapshotControllerServices) {
            map.put(serviceDTO.getId(), serviceDTO);
            if (!serviceNameMap.containsKey(serviceDTO.getName())) {
                serviceNameMap.put(serviceDTO.getName(), new ArrayList<ControllerServiceDTO>());
            }
            serviceNameMap.get(serviceDTO.getName()).add(serviceDTO);
        }
        //now try to merge in the newly created services if they exist by ID or name then reference the existing one, otherwise add them to the map
        List<ControllerServiceDTO> matchingControllerServices = Lists.newArrayList(Iterables.filter(newlyCreatedControllerServices, new Predicate<ControllerServiceDTO>() {
            @Override
            public boolean apply(ControllerServiceDTO controllerServiceDTO) {
                return map.containsKey(controllerServiceDTO.getId()) || serviceNameMap.containsKey(controllerServiceDTO.getName());
            }
        }));
        //add any others not matched to the map to return
        List<ControllerServiceDTO> unmatchedServices = Lists.newArrayList(Iterables.filter(newlyCreatedControllerServices, new Predicate<ControllerServiceDTO>() {
            @Override
            public boolean apply(ControllerServiceDTO controllerServiceDTO) {
                return !map.containsKey(controllerServiceDTO.getId()) && !serviceNameMap.containsKey(controllerServiceDTO.getName());
            }
        }));

        if (unmatchedServices != null && !unmatchedServices.isEmpty()) {
            for (ControllerServiceDTO serviceToAdd : unmatchedServices) {
                map.put(serviceToAdd.getId(), serviceToAdd);
            }
        }

        //if match existing services, then delete the new ones
        if (matchingControllerServices != null && !matchingControllerServices.isEmpty()) {
            for (ControllerServiceDTO serviceToDelete : matchingControllerServices) {

                try {
                    restClient.deleteControllerService(serviceToDelete.getId());
                } catch (NifiClientRuntimeException e) {
                    log.error("Exception while attempting to mergeControllerServices.  Unable to delete Service {}. {}", serviceToDelete.getId(), e.getMessage());

                }
            }
        }

        mergedControllerServices = map;
    }

    public List<NifiProperty> updateControllerServiceReferences(List<ProcessorDTO> processors) {
        return updateControllerServiceReferences(processors, null);
    }

    /**
     * Fix references to the controller services on the processor properties
     *
     * @param processors                  processors to inspect
     * @param controllerServiceProperties property overrides for controller services
     * @return the list of properties that were modified
     */
    public List<NifiProperty> updateControllerServiceReferences(List<ProcessorDTO> processors, Map<String, String> controllerServiceProperties) {

        try {
            //merge the snapshotted services with the newly created ones and update respective processors in the newly created flow
            final Map<String, ControllerServiceDTO> enabledServices = new HashMap<>();
            Map<String, ControllerServiceDTO> allServices = mergedControllerServices;
            for (ControllerServiceDTO dto : allServices.values()) {
                if (NifiProcessUtil.SERVICE_STATE.ENABLED.name().equals(dto.getState())) {
                    enabledServices.put(dto.getId(), dto);
                    enabledServices.put(dto.getName(), dto);
                }
            }
            List<NifiProperty> properties = new ArrayList<>();
            Map<String, ProcessGroupDTO> processGroupDTOMap = new HashMap<>();

            for (ProcessorDTO dto : processors) {
                ProcessGroupDTO groupDTO = processGroupDTOMap.get(dto.getParentGroupId());
                if (groupDTO == null) {
                    //we can create a tmp group dto here as all we need is the id
                    groupDTO = new ProcessGroupDTO();
                    groupDTO.setId(dto.getParentGroupId());
                    groupDTO.setName(dto.getParentGroupId());
                    processGroupDTOMap.put(dto.getParentGroupId(), groupDTO);
                }
                properties.addAll(NifiPropertyUtil.getPropertiesForProcessor(groupDTO, dto, restClient.getPropertyDescriptorTransform()));
            }

            List<NifiProperty> updatedProperties = fixControllerServiceReferences(controllerServiceProperties, enabledServices, allServices, properties);
            updatedProperties
                .forEach(property -> restClient.updateProcessorProperty(property.getProcessGroupId(), property.getProcessorId(), property));
            return updatedProperties;

        } catch (NifiClientRuntimeException e) {
            errors.add(new NifiError(NifiError.SEVERITY.FATAL, "Error trying to identify Controller Services. " + e.getMessage(),
                                     NifiProcessGroup.CONTROLLER_SERVICE_CATEGORY));
        }
        return Collections.emptyList();
    }

    /**
     * Enables the controller services for the specified properties or changes the property value to an enabled service.
     *
     * @param controllerServiceProperties property overrides for controller services
     * @param enabledServices             map of enabled controller service ids and names to DTOs
     * @param allServices                 map of all controller service ids to DTOs
     * @param properties                  the processor properties to update
     * @return the list of properties that were modified
     */
    @Nonnull
    private List<NifiProperty> fixControllerServiceReferences(@Nullable final Map<String, String> controllerServiceProperties, @Nonnull final Map<String, ControllerServiceDTO> enabledServices,
                                                              @Nonnull final Map<String, ControllerServiceDTO> allServices, @Nonnull final List<NifiProperty> properties) {
        return properties.stream()

            // Pick properties that reference a controller service
            .filter(property -> StringUtils.isNotBlank(property.getPropertyDescriptor().getIdentifiesControllerService()))

            // Pick properties that reference a disabled or unknown controller service
            .filter(property -> !enabledServices.containsKey(property.getValue()))

            // Find a controller service
            .filter(property -> {
                final Optional<ControllerServiceDTO> controllerService = findControllerServiceForProperty(controllerServiceProperties, enabledServices, allServices, property);
                if (controllerService.isPresent()) {
                    if (!controllerService.get().getId().equals(property.getValue())) {
                        property.setValue(controllerService.get().getId());
                        return true;
                    }
                } else if (property.getPropertyDescriptor().isRequired()) {
                    final String message = "Unable to find a valid controller service for the '" + property.getKey() + "' property of the '" + property.getProcessorName() + "' " + "processor.";
                    errors.add(new NifiError(NifiError.SEVERITY.FATAL, message, NifiProcessGroup.CONTROLLER_SERVICE_CATEGORY));
                }
                return false;
            })
            .collect(Collectors.toList());
    }

    /**
     * Finds and enables a controller service for the specified processor property.
     *
     * @param controllerServiceProperties property overrides for controller services
     * @param enabledServices             map of enabled controller service ids and names to DTOs
     * @param allServices                 map of all controller service ids to DTOs
     * @param property                    the processor properties to update
     * @return the matching controller service
     */
    private Optional<ControllerServiceDTO> findControllerServiceForProperty(@Nullable final Map<String, String> controllerServiceProperties,
                                                                            @Nonnull final Map<String, ControllerServiceDTO> enabledServices,
                                                                            @Nonnull final Map<String, ControllerServiceDTO> allServices, @Nonnull final NifiProperty property) {
        return property.getPropertyDescriptor().getAllowableValues().stream()

            // Pick values that exist
            .filter(allowableValue -> allServices.containsKey(allowableValue.getValue()))

            // Sort allowed values by priority
            .sorted((a, b) -> {
                final String propertyValue = property.getValue();
                final String value1 = a.getValue();
                final String value2 = b.getValue();
                return ComparisonChain.start()
                    // 1. Matches property value
                    .compareTrueFirst(value1.equals(propertyValue), value2.equals(propertyValue))
                    // 2. Service is enabled
                    .compareTrueFirst(enabledServices.containsKey(value1), enabledServices.containsKey(value2))
                    // 3. Similar service is enabled
                    .compareTrueFirst(enabledServices.containsKey(a.getDisplayName()), enabledServices.containsKey(b.getDisplayName()))
                    .result();
            })

            // Map to controller service DTO
            .map(NiFiAllowableValue::getValue)
            .map(allServices::get)

            // Try to enable controller service
            .filter(controllerService -> {
                try {
                    tryToEnableControllerService(controllerService, controllerServiceProperties, enabledServices, allServices);
                    return true;
                } catch (final Exception e) {
                    log.error("Failed to enable controller service [id:{},name:{}]: {}", controllerService.getId(), controllerService.getName(), e.toString(), e);
                    return false;
                }
            })

            // Return first enabled controller service
            .findFirst();
    }

    public void cleanupControllerServices() {
        //only delete the services that were created if none of them with that type existed in the system before
        // only keep them if they are the first of their kind
        if (snapshotControllerServices != null && !snapshotControllerServices.isEmpty()) {
            final Set<String> serviceTypes = new HashSet<>();
            for (ControllerServiceDTO dto : snapshotControllerServices) {
                serviceTypes.add(dto.getType());
            }

            List<ControllerServiceDTO>
                servicesToDelete =
                Lists.newArrayList(Iterables.filter(newlyCreatedControllerServices, new Predicate<ControllerServiceDTO>() {
                    @Override
                    public boolean apply(ControllerServiceDTO controllerServiceDTO) {
                        return serviceTypes.contains(controllerServiceDTO.getType()) && (controllerServiceDTO.getReferencingComponents() == null
                                                                                         || controllerServiceDTO.getReferencingComponents().size() == 0);
                    }

                }));
            if (servicesToDelete != null && !servicesToDelete.isEmpty()) {
                try {
                    restClient.deleteControllerServices(servicesToDelete);
                } catch (Exception e) {
                    log.info("error attempting to cleanup controller services while trying to delete Services: " + e.getMessage()
                             + ".  It might be wise to login to NIFI and verify there are not extra controller services");
                    getErrors().add(new NifiError(NifiError.SEVERITY.INFO, "There is an error attempting to remove the controller service :" + e.getMessage()));
                }
            }
        }
    }

    /**
     * Deletes the input port connections to the specified process group.
     *
     * <p>When versioning we want to delete only the input port connections. Keep output port connections in place as they may still have data running through them that should flow through the
     * system.</p>
     *
     * @param processGroup the process group with input port connections
     * @throws NifiClientRuntimeException if a connection cannot be deleted
     */
    private void deleteInputPortConnections(@Nonnull final ProcessGroupDTO processGroup) throws NifiClientRuntimeException {
        // Get the list of incoming connections coming from some source to this process group
        final Set<ConnectionDTO> connectionsEntity = restClient.getProcessGroupConnections(processGroup.getParentGroupId());
        if (connectionsEntity == null) {
            return;
        }

        final List<ConnectionDTO> connections = NifiConnectionUtil.findConnectionsMatchingDestinationGroupId(connectionsEntity, processGroup.getId());
        if (connections == null) {
            return;
        }

        // Delete the connections
        for (ConnectionDTO connection : connections) {
            final String type = connection.getSource().getType();
            log.info("Found connection {} matching source type {} and destination group {}.", connection.getId(), type, connection.getDestination().getId());

            // Stop the port
            if (NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name().equalsIgnoreCase(type)) {
                try {
                    restClient.stopInputPort(connection.getSource().getGroupId(), connection.getSource().getId());
                    log.info("Stopped input port {} for connection: {} ", connection.getSource().getId(), connection.getId());
                } catch (Exception e) {
                    log.error("Failed to stop input port for connection: {}", connection.getId(), e);
                    throw new NifiClientRuntimeException("Error stopping the input port " + connection.getSource().getId() + " for connection " + connection.getId() + " prior to deleting the "
                                                         + "connection.");
                }
            }

            // Delete the connection
            try {
                restClient.deleteConnection(connection, false);
            } catch (Exception e) {
                log.error("Failed to delete the connection: {}", connection.getId(), e);

                final String source = (connection.getSource() != null) ? connection.getSource().getName() : null;
                final String destination = (connection.getDestination() != null) ? connection.getDestination().getName() : null;
                throw new NifiClientRuntimeException("Error deleting the connection " + connection.getId() + " with source " + source + " and destination " + destination + ".");
            }
        }
    }

    /**
     * Version a ProcessGroup renaming it with the name - {timestamp millis}.
     * If {@code removeIfInactive} is true it will not version but just delete it
     *
     * @param processGroup the group to version
     */
    public ProcessGroupDTO versionProcessGroup(ProcessGroupDTO processGroup) {
        log.info("Versioning Process Group {} ", processGroup.getName());

        restClient.disableAllInputProcessors(processGroup.getId());
        log.info("Disabled Inputs for {} ", processGroup.getName());
        //attempt to stop all processors
        try {
            restClient.stopInputs(processGroup.getId());
            log.info("Stopped Input Ports for {}, ", processGroup.getName());
        } catch (Exception e) {
            log.error("Error trying to stop Input Ports for {} while creating a new version ", processGroup.getName());
        }
        //delete input connections
        try {
            deleteInputPortConnections(processGroup);

        } catch (NifiClientRuntimeException e) {
            log.error("Error trying to delete input port connections for Process Group {} while creating a new version. ", processGroup.getName(), e);
            getErrors().add(new NifiError(NifiError.SEVERITY.FATAL, "The input port connections to the process group " + processGroup.getName() + " could not be deleted. Please delete them manually "
                                                                    + "in NiFi and try again."));
        }

        String versionedProcessGroupName = getVersionedProcessGroupName(processGroup.getName());

        //rename the feedGroup to be name+timestamp
        processGroup.setName(versionedProcessGroupName);
        restClient.updateProcessGroup(processGroup);
        log.info("Renamed ProcessGroup to  {}, ", processGroup.getName());

        return processGroup;
    }

    public void markProcessorsAsRunning(NifiProcessGroup newProcessGroup) {
        if (newProcessGroup.isSuccess()) {
            try {
                restClient.markProcessorGroupAsRunning(newProcessGroup.getProcessGroupEntity());
            } catch (NifiClientRuntimeException e) {
                String errorMsg = "Unable to mark feed as " + NifiProcessUtil.PROCESS_STATE.RUNNING + ".";
                newProcessGroup
                    .addError(newProcessGroup.getProcessGroupEntity().getId(), "", NifiError.SEVERITY.WARN, errorMsg,
                              "Process State");
                newProcessGroup.setSuccess(false);
            }
        }
    }

    public void markConnectionPortsAsRunning(ProcessGroupDTO entity) {

        restClient.markConnectionPortsAsRunning(entity);
    }
}
