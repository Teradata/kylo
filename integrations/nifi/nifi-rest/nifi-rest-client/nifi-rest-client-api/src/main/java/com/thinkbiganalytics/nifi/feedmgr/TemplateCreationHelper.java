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
import com.google.common.collect.Maps;
import com.thinkbiganalytics.nifi.rest.NiFiObjectCache;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.model.NiFiAllowableValue;
import com.thinkbiganalytics.nifi.rest.model.NiFiPropertyDescriptorTransform;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.model.VersionedProcessGroup;
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
import org.apache.nifi.web.api.dto.ProcessorConfigDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    public static final String REUSABLE_TEMPLATES_PROCESS_GROUP_NAME = "reusable_templates";

    public static final String TEMPORARY_TEMPLATE_INSPECTION_GROUP_NAME = "kylo_temporary_template_inspection";
    /**
     * REST client for NiFi API
     */
    @Nonnull
    private final NiFiRestClient nifiRestClient;
    private LegacyNifiRestClient restClient;
    private NiFiObjectCache nifiObjectCache;
    private List<NifiError> errors = new ArrayList<>();
    private Set<ControllerServiceDTO> snapshotControllerServices;

    private Set<ControllerServiceDTO> snapshottedEnabledControllerServices = new HashSet<>();

    private Map<String, ControllerServiceDTO> mergedControllerServices;

    private Map<String, List<ControllerServiceDTO>> serviceNameMap = new HashMap<>();

    private Map<String, List<ControllerServiceDTO>> enabledServiceNameMap  = new HashMap<>();


    private Set<ControllerServiceDTO> newlyCreatedControllerServices;

    private List<NifiProperty> templateProperties;

    public TemplateCreationHelper(LegacyNifiRestClient restClient) {
        this.restClient = restClient;
        this.nifiRestClient = restClient.getNiFiRestClient();
        this.nifiObjectCache = new NiFiObjectCache();
    }

    public TemplateCreationHelper(LegacyNifiRestClient restClient,NiFiObjectCache nifiObjectCache) {
        this.restClient = restClient;
        this.nifiRestClient = restClient.getNiFiRestClient();
        this.nifiObjectCache = nifiObjectCache;
    }



    public static String getVersionedProcessGroupName(String name) {
        return NifiTemplateNameUtil.getVersionedProcessGroupName(name);
    }

    public static String getVersionedProcessGroupName(String name, String versionIdentifier) {
        return NifiTemplateNameUtil.getVersionedProcessGroupName(name,versionIdentifier);
    }

    public static String parseVersionedProcessGroupName(String name) {
        return NifiTemplateNameUtil.parseVersionedProcessGroupName(name);
    }

    @Nullable
    public static String getKyloVersionIdentifier(String name) {
        return NifiTemplateNameUtil.getKyloVersionIdentifier(name);
    }

    public void setTemplateProperties(List<NifiProperty> templateProperties) {
        this.templateProperties = templateProperties;
    }

    /**
     * Creates an instance of the supplied template under the temporary inspection group inside its own process group
     *
     * @param templateId the template to instantiate
     * @return the process group holding this template
     */
    public ProcessGroupDTO createTemporaryTemplateFlow(@Nonnull final String templateId) {
        ProcessGroupDTO temporaryTemplateInspectionGroup =  nifiObjectCache.getOrCreateTemporaryTemplateInspectionGroup();


        //next create the temp group
        snapshotControllerServiceReferences();
        ProcessGroupDTO tempGroup = nifiRestClient.processGroups().create(temporaryTemplateInspectionGroup.getId(), "template_" + System.currentTimeMillis());
        TemplateInstance instance= instantiateFlowFromTemplate(tempGroup.getId(), templateId);
        FlowSnippetDTO snippet = instance.getFlowSnippetDTO();
        identifyNewlyCreatedControllerServiceReferences(instance);
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
    public TemplateInstance instantiateFlowFromTemplate(@Nonnull final String processGroupId, @Nonnull final String templateId) throws NifiComponentNotFoundException {
        // Instantiate template
        final NiFiRestClient nifiClient = restClient.getNiFiRestClient();
        final FlowSnippetDTO templateFlow = nifiClient.processGroups().instantiateTemplate(processGroupId, templateId);
        TemplateInstance instance = new TemplateInstance(templateFlow);

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
                ControllerServiceDTO newRootService = nifiClient.processGroups().createControllerService("root", rootControllerService);
                final String rootId = newRootService.getId();

                // Map old ID to new ID
                idMap.put(oldId, rootId);
                instance.movedScopedControllerService(groupControllerService,newRootService);
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
        return instance;
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

        mergeControllerServices(null);
        return newServices;
    }

    public Set<ControllerServiceDTO> identifyNewlyCreatedControllerServiceReferences(TemplateInstance templateInstance) {

        newlyCreatedControllerServices = templateInstance.getCreatedServices();
        mergeControllerServices(templateInstance);
        return newlyCreatedControllerServices;

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

    private void mergeControllerServices(TemplateInstance templateInstance) {

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
        java.util.function.Predicate<ControllerServiceDTO> matchingServiceFilter = (cs) -> map.containsKey(cs.getId()) || serviceNameMap.containsKey(cs.getName());

        List<ControllerServiceDTO> matchingControllerServices =  newlyCreatedControllerServices.stream().filter(matchingServiceFilter).collect(Collectors.toList());

        List<ControllerServiceDTO> unmatchedServices = newlyCreatedControllerServices.stream().filter(matchingServiceFilter.negate()).collect(Collectors.toList());

        //if the service has additional propertyDescriptors that identify other services we need to fetch the service by its id.
        if (unmatchedServices != null && !unmatchedServices.isEmpty()) {
          Map<String,ControllerServiceDTO> updatedServices =unmatchedServices.stream().map(serviceToAdd -> {
              //if the service has additional propertyDescriptors that identify other services we need to fetch the service by its id
                if(serviceToAdd.getDescriptors() != null && serviceToAdd.getDescriptors().values().stream().anyMatch(propertyDescriptorDTO -> StringUtils.isNotBlank(propertyDescriptorDTO.getIdentifiesControllerService()))) {
                    try {
                        Optional<ControllerServiceDTO> cs = restClient.getNiFiRestClient().controllerServices().findById(serviceToAdd.getId());
                        if(cs.isPresent()){
                            return cs.get();
                        }
                        else {
                            return serviceToAdd;
                        }
                    }
                    catch(Exception e){
                        return serviceToAdd;
                    }
                }
                else {
                    return serviceToAdd;
                }


        }).collect(Collectors.toMap(service -> service.getId(), service -> service));
                map.putAll(updatedServices);
             //update the core item
            newlyCreatedControllerServices = newlyCreatedControllerServices.stream().map(controllerServiceDTO -> {
                if(map.containsKey(controllerServiceDTO.getId())){
                    return updatedServices.get(controllerServiceDTO.getId());
                }
                else {
                    return controllerServiceDTO;
                }
            }).collect(Collectors.toSet());

        }

        //if match existing services, then delete the new ones
        if (matchingControllerServices != null && !matchingControllerServices.isEmpty()) {
            for (ControllerServiceDTO serviceToDelete : matchingControllerServices) {
                try {
                    if(templateInstance != null ) {
                        templateInstance.addDeletedServiceMapping(serviceToDelete.getId(), serviceNameMap.get(serviceToDelete.getName()));
                    }
                    restClient.deleteControllerService(serviceToDelete.getId());
                } catch (NifiClientRuntimeException e) {
                    log.error("Exception while attempting to mergeControllerServices.  Unable to delete Service {}. {}", serviceToDelete.getId(), e.getMessage());

                }
            }

        }

        mergedControllerServices = map;

        //validate
        //Create a map of the Controller Service Name to list of matching services

        this.serviceNameMap =mergedControllerServices.values().stream()
            .collect(Collectors.groupingBy(cs -> cs.getName()));

        this.enabledServiceNameMap =mergedControllerServices.values().stream()
            .filter(cs -> NifiProcessUtil.SERVICE_STATE.ENABLED.name().equalsIgnoreCase(cs.getState()))
            .collect(Collectors.groupingBy(cs -> cs.getName()));

    }

    private boolean hasMatchingService(Map<String,List<ControllerServiceDTO>> nameMap, String name){
        return nameMap.containsKey(name) && !nameMap.get(name).isEmpty();
    }


    private List<ProcessorDTO> reassignControllerServiceIds(List<ProcessorDTO> processors, TemplateInstance instance) {

        Set<ProcessorDTO> updatedProcessors = new HashSet<>();
        if (processors != null) {
            processors.stream().forEach(processorDTO -> {
                Map<String, String> updatedProcessorProperties = new HashMap<>();
                processorDTO.getConfig().getDescriptors().forEach((k, v) -> {
                    if (v.getIdentifiesControllerService() != null) {

                        boolean idsMatch = getMergedControllerServices().keySet().stream().anyMatch(id -> id.equalsIgnoreCase(processorDTO.getConfig().getProperties().get(k)));
                        if (!idsMatch && templateProperties != null && !templateProperties.isEmpty()) {

                            NifiProperty matchingProperty = templateProperties.stream().filter(
                                p -> p.getKey().equalsIgnoreCase(k) && p.getProcessorName().equalsIgnoreCase(processorDTO.getName()) && v.getIdentifiesControllerService()
                                    .equalsIgnoreCase(p.getPropertyDescriptor().getIdentifiesControllerService())
                            ).findFirst().orElse(null);
                            if (matchingProperty != null && matchingProperty.getPropertyDescriptor() != null && matchingProperty.getPropertyDescriptor().getAllowableValues() != null) {
                                NiFiAllowableValue matchingValue = matchingProperty.getPropertyDescriptor().getAllowableValues().stream()
                                    .filter(niFiAllowableValue -> niFiAllowableValue.getValue().equalsIgnoreCase(matchingProperty.getValue())).findFirst().orElse(null);
                                if (matchingValue != null) {
                                    String name = matchingValue.getDisplayName();
                                    String
                                        validControllerServiceId = hasMatchingService(enabledServiceNameMap,name) ? enabledServiceNameMap.get(name).get(0).getId()
                                                                                                      : hasMatchingService(serviceNameMap, name) ? serviceNameMap.get(name).get(0).getId() : null;

                                    if (StringUtils.isNotBlank(validControllerServiceId) && ( v.isRequired() || !v.isRequired() &&  StringUtils.isNotBlank(processorDTO.getConfig().getProperties().get(k)))) {
                                        processorDTO.getConfig().getProperties().put(k, validControllerServiceId);
                                        updatedProcessorProperties.put(k, validControllerServiceId);
                                        if (!updatedProcessors.contains(processorDTO)) {
                                            updatedProcessors.add(processorDTO);
                                        }
                                    }
                                }
                            }
                        }
                        //if we havent made a match attempt to see if the cs was removed
                        if(!updatedProcessorProperties.containsKey(k) && !idsMatch && instance != null) {
                            String value = processorDTO.getConfig().getProperties().get(k);
                            //find the correct reference from that was removed due to a matching service
                            ControllerServiceDTO controllerServiceDTO = instance.findMatchingControllerServoce(value);
                            if(controllerServiceDTO != null) {
                                    updatedProcessorProperties.put(k, controllerServiceDTO.getId());
                            }
                        }
                    }

                });
                if (!updatedProcessorProperties.isEmpty()) {
                    ProcessorDTO updatedProcessor = new ProcessorDTO();
                    updatedProcessor.setId(processorDTO.getId());
                    updatedProcessor.setConfig(new ProcessorConfigDTO());
                    updatedProcessor.getConfig().setProperties(updatedProcessorProperties);
                    //update the processor

                    ProcessorDTO updated = restClient.updateProcessor(updatedProcessor);
                    updatedProcessors.add(updated);
                }

            });
        }
        //update the data back in the processors list
        if (!updatedProcessors.isEmpty()) {
            Map<String,ProcessorDTO> updatedMap = updatedProcessors.stream().collect(Collectors.toMap(p->p.getId(),p -> p));
          return  processors.stream().map(p ->  updatedMap.containsKey(p.getId()) ? updatedMap.get(p.getId()) : p).collect(Collectors.toList());
        }

        return processors;

    }


    public List<NifiProperty> updateControllerServiceReferences(List<ProcessorDTO> processors, TemplateInstance templateInstance) {
        return updateControllerServiceReferences(processors, null,templateInstance);
    }

    public List<NifiProperty> updateControllerServiceReferences(List<ProcessorDTO> processors) {
        return updateControllerServiceReferences(processors, null,null);
    }

    /**
     * Fix references to the controller services on the processor properties
     *
     * @param processors                  processors to inspect
     * @param controllerServiceProperties property overrides for controller services
     * @return the list of properties that were modified
     */
    public List<NifiProperty> updateControllerServiceReferences(List<ProcessorDTO> processors, Map<String, String> controllerServiceProperties, TemplateInstance instance) {

        try {
          processors =  reassignControllerServiceIds(processors, instance);

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

    public Map<String, ControllerServiceDTO> getMergedControllerServices() {
        return mergedControllerServices == null ? Maps.newHashMap() : mergedControllerServices;
    }

    public Map<String, List<ControllerServiceDTO>> getServiceNameMap() {
        return serviceNameMap;
    }

    public Map<String, List<ControllerServiceDTO>> getEnabledServiceNameMap() {
        return enabledServiceNameMap;
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

            // Pick properties that have a value set
            .filter(property -> StringUtils.isNotBlank(property.getValue()))

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
    private List<ConnectionDTO> deleteInputPortConnections(@Nonnull final ProcessGroupDTO processGroup) throws NifiClientRuntimeException {
        // Get the list of incoming connections coming from some source to this process group
          List<ConnectionDTO> deletedConnections = new ArrayList<>();

        final Set<ConnectionDTO> connectionsEntity = nifiObjectCache.isCacheConnections() ? nifiObjectCache.getConnections(processGroup.getParentGroupId()) : restClient.getProcessGroupConnections(processGroup.getParentGroupId());

        if (connectionsEntity == null) {
            return deletedConnections;
        }

        final List<ConnectionDTO> connections = NifiConnectionUtil.findConnectionsMatchingDestinationGroupId(connectionsEntity, processGroup.getId());
        if (connections == null) {
            return deletedConnections;
        }

        Set<String> removedConnections = new HashSet<>();
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
                removedConnections.add(connection.getId());
                deletedConnections.add(connection);
            } catch (Exception e) {
                log.error("Failed to delete the connection: {}", connection.getId(), e);

                final String source = (connection.getSource() != null) ? connection.getSource().getName() : null;
                final String destination = (connection.getDestination() != null) ? connection.getDestination().getName() : null;
                throw new NifiClientRuntimeException("Error deleting the connection " + connection.getId() + " with source " + source + " and destination " + destination + ".");
            }
        }
        nifiObjectCache.removeConnections(processGroup.getParentGroupId(),removedConnections);
        return deletedConnections;
    }

    /**
     * Version a ProcessGroup renaming it with the name - {timestamp millis}.
     * If {@code removeIfInactive} is true it will not version but just delete it
     *
     * @param processGroup the group to version
     */
    public VersionedProcessGroup versionProcessGroup(ProcessGroupDTO processGroup, String versionIdentifier) {
        log.info("Versioning Process Group {} ", processGroup.getName());
        VersionedProcessGroup versionedProcessGroup = new VersionedProcessGroup();
        versionedProcessGroup.setProcessGroupPriorToVersioning(processGroup);
        versionedProcessGroup.setProcessGroupName(processGroup.getName());


       List<ProcessorDTO> inputProcessorsPriorToDisabling = restClient.disableAllInputProcessors(processGroup.getId());

       versionedProcessGroup.setInputProcessorsPriorToDisabling(inputProcessorsPriorToDisabling);

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
          List<ConnectionDTO> deletedConnections=  deleteInputPortConnections(processGroup);
          versionedProcessGroup.setDeletedInputPortConnections(deletedConnections);

        } catch (NifiClientRuntimeException e) {
            log.error("Error trying to delete input port connections for Process Group {} while creating a new version. ", processGroup.getName(), e);
            getErrors().add(new NifiError(NifiError.SEVERITY.FATAL, "The input port connections to the process group " + processGroup.getName() + " could not be deleted. Please delete them manually "
                                                                    + "in NiFi and try again."));
        }

        String versionedProcessGroupName = getVersionedProcessGroupName(processGroup.getName(),versionIdentifier);
        versionedProcessGroup.setVersionedProcessGroupName(versionedProcessGroupName);

        //rename the feedGroup to be name+timestamp
        processGroup.setName(versionedProcessGroupName);
        restClient.updateProcessGroup(processGroup);
        log.info("Renamed ProcessGroup to  {}, ", processGroup.getName());
        versionedProcessGroup.setVersionedProcessGroup(processGroup);

        return versionedProcessGroup;
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

    public void startProcessGroupAndParentInputPorts(ProcessGroupDTO entity) {
        restClient.startProcessGroupAndParentInputPorts(entity);
    }


    public void markConnectionPortsAsRunning(ProcessGroupDTO entity) {

        restClient.markConnectionPortsAsRunning(entity);
    }
}
