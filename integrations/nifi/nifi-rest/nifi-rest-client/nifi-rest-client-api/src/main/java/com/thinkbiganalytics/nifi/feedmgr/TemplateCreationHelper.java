package com.thinkbiganalytics.nifi.feedmgr;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NiFiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiClientRuntimeException;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.model.ControllerServiceProperty;
import com.thinkbiganalytics.nifi.rest.model.ControllerServicePropertyHolder;
import com.thinkbiganalytics.nifi.rest.model.NiFiAllowableValue;
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
import org.apache.nifi.web.api.dto.PropertyDescriptorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Created by sr186054 on 5/6/16.
 */
public class TemplateCreationHelper {

    private static final Logger log = LoggerFactory.getLogger(TemplateCreationHelper.class);

    public static String REUSABLE_TEMPLATES_PROCESS_GROUP_NAME = "reusable_templates";

    private List<NifiError> errors = new ArrayList<>();

    LegacyNifiRestClient restClient;

    private Set<ControllerServiceDTO> snapshotControllerServices;

    private Set<ControllerServiceDTO> snapshottedEnabledControllerServices = new HashSet<>();

    private Map<String, ControllerServiceDTO> mergedControllerServices;

    private Set<ControllerServiceDTO> newlyCreatedControllerServices;

    Map<String, Integer> controllerServiceEnableAttempts = new ConcurrentHashMap<>();

    private Integer MAX_ENABLE_ATTEMPTS = 5;
    private Long ENABLE_CONTROLLER_SERVICE_WAIT_TIME = 2000L;

    public TemplateCreationHelper(LegacyNifiRestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Instantiates the specified template in the specified process group.
     *
     * <p>Controller services that are created under the specified process group will be moved to the root process group. This side-effect may be removed in the future.</p>
     *
     * @param processGroupId the process group id
     * @param templateId the template id
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
     * Try to see if there are processors that use process groups and then
     */
    public ControllerServicePropertyHolder validatePropertiesWithControllerServices(ProcessGroupDTO processGroupDTO) throws NifiClientRuntimeException {
        List<ControllerServiceProperty> controllerServiceProperties = new ArrayList<>();

        Map<String, ProcessorDTO> processors = NifiProcessUtil.getProcessorsMap(processGroupDTO);
        if (processors != null && !processors.isEmpty()) {
            for (ProcessorDTO processor : processors.values()) {
                List<PropertyDescriptorDTO> propertyDescriptors = Lists.newArrayList(Iterables.filter(processor.getConfig().getDescriptors().values(), new Predicate<PropertyDescriptorDTO>() {
                    @Override
                    public boolean apply(PropertyDescriptorDTO propertyDescriptorDTO) {
                        return StringUtils.isNotBlank(propertyDescriptorDTO.getIdentifiesControllerService());
                    }
                }));
                if (propertyDescriptors != null) {
                    for (PropertyDescriptorDTO propertyDescriptor : propertyDescriptors) {
                        String value = processor.getConfig().getProperties().get(propertyDescriptor.getName());
                        ControllerServiceProperty controllerServiceProperty = new ControllerServiceProperty();
                        controllerServiceProperty.setProcessorId(processor.getId());
                        controllerServiceProperty.setProcessorGroupId(processor.getParentGroupId());
                        controllerServiceProperty.setProcessorName(processor.getName());
                        controllerServiceProperty.setPropertyValue(value);
                        controllerServiceProperty.setPropertyName(propertyDescriptor.getName());
                        controllerServiceProperties.add(controllerServiceProperty);
                    }
                }
            }
        }

        if (!controllerServiceProperties.isEmpty()) {
            Set<ControllerServiceDTO> controllerServicesEntity = restClient.getControllerServices();
            Map<String, ControllerServiceDTO> controllerServices = new HashMap<>();
            for (ControllerServiceDTO controllerServiceDTO : controllerServicesEntity) {
                controllerServices.put(controllerServiceDTO.getId(), controllerServiceDTO);
            }

            for (ControllerServiceProperty controllerServiceProperty : controllerServiceProperties) {
                ControllerServiceDTO controllerServiceDTO = controllerServices.get(controllerServiceProperty.getPropertyValue());
                String
                    message =
                    "The Controller Service assigned to Processor: " + controllerServiceProperty.getProcessorName() + "[" + controllerServiceProperty.getProcessorId() + "] - "
                    + controllerServiceProperty.getPropertyName();
                if (controllerServiceDTO == null) {
                    controllerServiceProperty.setValid(false);
                    controllerServiceProperty.setValidationMessage(message + " doesn't exist ");
                } else if (controllerServiceDTO.getState().equalsIgnoreCase(NifiProcessUtil.SERVICE_STATE.DISABLED.name())) {
                    controllerServiceProperty.setValid(false);
                    controllerServiceProperty.setValidationMessage(message + " is DISABLED. ");
                } else {
                    controllerServiceProperty.setValid(true);
                }

                if (!controllerServiceProperty.isValid()) {
                    errors.add(new NifiError(NifiError.SEVERITY.FATAL,
                                             controllerServiceProperty.getValidationMessage(),
                                             NifiProcessGroup.CONTROLLER_SERVICE_CATEGORY));

                }


            }

        }
        return new ControllerServicePropertyHolder(controllerServiceProperties);


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

    private ControllerServiceDTO tryToEnableControllerService(String serviceId, String name, Map<String, String> properties) {
        try {
            ControllerServiceDTO entity = restClient.enableControllerServiceAndSetProperties(serviceId, properties);
            return entity;
        } catch (Exception e) {
            NifiClientRuntimeException clientRuntimeException = null;
            if(e instanceof NifiClientRuntimeException){
                clientRuntimeException = (NifiClientRuntimeException) e;
            }
            else {
                clientRuntimeException = new NifiClientRuntimeException(e);
            }
            if (clientRuntimeException.is409Error()) {
                //wait and try again
                Integer attempt = controllerServiceEnableAttempts.get(serviceId);
                if (attempt == null) {
                    attempt = 0;
                    controllerServiceEnableAttempts.put(serviceId, attempt);
                }
                attempt++;
                controllerServiceEnableAttempts.put(serviceId, attempt);
                if (attempt <= MAX_ENABLE_ATTEMPTS) {
                    log.info("Error attempting to enable the controller service {},{}.  Attempt Number: {} .  Waiting {} seconds before trying again", serviceId, name, attempt,
                             ENABLE_CONTROLLER_SERVICE_WAIT_TIME / 1000);
                    try {
                        Thread.sleep(ENABLE_CONTROLLER_SERVICE_WAIT_TIME);
                        tryToEnableControllerService(serviceId, name, properties);
                    } catch (InterruptedException e2) {

                    }
                } else {
                    log.error("Unable to Enable Controller Service for {}, {}.  Max retry attempts of {} exceeded ", name, serviceId, MAX_ENABLE_ATTEMPTS);
                }

            }

        }
        return null;
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

    private void reassignControllerServiceProperties() {

    }

    public void updateControllerServiceReferences(List<ProcessorDTO> processors) {
        updateControllerServiceReferences(processors, null);
    }

    public void updateControllerServiceReferences(List<ProcessorDTO> processors, Map<String, String> controllerServiceProperties) {

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

            enableServices(controllerServiceProperties, enabledServices, allServices, properties);

        } catch (NifiClientRuntimeException e) {
            errors.add(new NifiError(NifiError.SEVERITY.FATAL, "Error trying to identify Controller Services. " + e.getMessage(),
                                     NifiProcessGroup.CONTROLLER_SERVICE_CATEGORY));
        }
    }

    private void enableServices(Map<String, String> controllerServiceProperties,
                                final Map<String, ControllerServiceDTO> enabledServices,
                                Map<String, ControllerServiceDTO> allServices,
                                List<NifiProperty> properties) {
        for (final NifiProperty property : properties) {
            String controllerService = property.getPropertyDescriptor().getIdentifiesControllerService();
            boolean isRequired = property.getPropertyDescriptor().isRequired();
            if (StringUtils.isNotBlank(controllerService)) {
                boolean set = false;

                //if the service is not enabled, but it exists then try to enable that
                if (!enabledServices.containsKey(property.getValue()) && allServices.containsKey(property.getValue())) {
                    ControllerServiceDTO dto = allServices.get(property.getValue());

                    //if service depends on other services lets enable those upstream services first
                    List<NifiProperty> serviceProperties = NifiPropertyUtil.getPropertiesForService(dto, restClient.getPropertyDescriptorTransform());
                    enableServices(controllerServiceProperties, enabledServices, allServices, serviceProperties);

                    ControllerServiceDTO entity = tryToEnableControllerService(dto.getId(), dto.getName(), controllerServiceProperties);
                    if (entity != null && NifiProcessUtil.SERVICE_STATE.ENABLED.name().equals(entity.getState())) {
                        enabledServices.put(entity.getId(), entity);
                        set = true;
                    }
                }
                if(enabledServices.containsKey(property.getValue())){
                    set = true;
                }

                if (!set) {
                    boolean controllerServiceSet = false;
                    String controllerServiceName = "";
                    // match a allowable service and enable it
                    List<NiFiAllowableValue> allowableValueDTOs = property.getPropertyDescriptor().getAllowableValues();
                    //if any of the allowable values are enabled already use that and continue
                    List<NiFiAllowableValue> enabledValues = allowableValueDTOs.stream()
                            .filter(allowableValueDTO -> enabledServices.containsKey(allowableValueDTO.getValue()) || enabledServices.containsKey(allowableValueDTO.getDisplayName()))
                            .collect(Collectors.toList());
                    if (enabledValues != null && !enabledValues.isEmpty()) {
                        NiFiAllowableValue enabledService = enabledValues.get(0);
                        ControllerServiceDTO dto = enabledServices.get(enabledService.getValue());
                        if (dto == null) {
                            dto = enabledServices.get(enabledService.getDisplayName());
                        }
                        controllerServiceName = dto.getName();
                        String previousValue = property.getValue();
                        property.setValue(dto.getId());
                        if (StringUtils.isBlank(previousValue) || !previousValue.equalsIgnoreCase(dto.getId())) {
                            log.info("About to assign Controller Service {} ({}) to property {} on processor {} ({}). ", dto.getName(), dto.getId(), property.getKey(), property.getProcessorName(),
                                     property.getProcessorId());
                            //update it in nifi
                            restClient.updateProcessorProperty(property.getProcessGroupId(), property.getProcessorId(), property);
                            log.info("Finished Assigning Controller Service {} ({}) to property {} on processor {} ({}). ", dto.getName(), dto.getId(), property.getKey(),
                                     property.getProcessorName(), property.getProcessorId());


                        }
                        controllerServiceSet = true;
                    } else {
                        //try to enable the service
                        //match the service by Name...
                        for (NiFiAllowableValue allowableValueDTO : allowableValueDTOs) {
                            ControllerServiceDTO dto = allServices.get(allowableValueDTO.getValue());
                            if (dto != null) {
                                if (StringUtils.isBlank(controllerServiceName)) {
                                    controllerServiceName = dto.getName();
                                }
                                property.setValue(allowableValueDTO.getValue());

                                //if service depends on other services lets enable those upstream services first
                                List<NifiProperty> serviceProperties = NifiPropertyUtil.getPropertiesForService(dto, restClient.getPropertyDescriptorTransform());
                                enableServices(controllerServiceProperties, enabledServices, allServices, serviceProperties);

                                ControllerServiceDTO entity = tryToEnableControllerService(allowableValueDTO.getValue(), controllerServiceName, controllerServiceProperties);
                                if (entity != null && NifiProcessUtil.SERVICE_STATE.ENABLED.name().equals(entity.getState())) {
                                    enabledServices.put(entity.getId(), entity);
                                    controllerServiceSet = true;
                                } else {
                                    controllerServiceSet = false;
                                }

                            }

                        }
                    }
                    if (controllerServiceSet) {
                        //update the processor
                        restClient.updateProcessorProperty(property.getProcessGroupId(), property.getProcessorId(), property);
                    }
                    if (!controllerServiceSet && (StringUtils.isNotBlank(property.getValue()) || isRequired)) {
                        errors.add(new NifiError(NifiError.SEVERITY.FATAL,
                                                 "Error trying to enable Controller Service " + controllerServiceName
                                                 + " on referencing Processor: " + property.getProcessorName() + " and field " + property
                                                         .getKey()
                                                 + ". Please go to Nifi and configure and enable this Service.",
                                                 NifiProcessGroup.CONTROLLER_SERVICE_CATEGORY));
                    }

                }
            }
        }
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
                             + ".  It might be wise to login to NIFI and verify there are not extra controller services", e);
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

    public static String getVersionedProcessGroupName(String name) {
        return NifiTemplateNameUtil.getVersionedProcessGroupName(name);
    }

    public static String parseVersionedProcessGroupName(String name) {
        return NifiTemplateNameUtil.parseVersionedProcessGroupName(name);
    }

    public static boolean isVersionedProcessGroup(String name) {
        return NifiTemplateNameUtil.isVersionedProcessGroup(name);
    }

    public void versionProcessGroup(ProcessGroupDTO processGroup) {
        log.info("Versioning Process Group {} ", processGroup.getName());

        restClient.disableAllInputProcessors(processGroup.getId());
        log.info("Disabled Inputs for {} ", processGroup.getName());
        //attempt to stop all processors
        try {
            restClient.stopInputs(processGroup.getId());
            log.error("Stopped Input Ports for {}, ", processGroup.getName());
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

        //rename the feedGroup to be name+timestamp
        //TODO change to work with known version passed in (get the rename to current version -1 or something.
        processGroup.setName(getVersionedProcessGroupName(processGroup.getName()));
        restClient.updateProcessGroup(processGroup);
        log.info("Renamed ProcessGroup to  {}, ", processGroup.getName());
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
