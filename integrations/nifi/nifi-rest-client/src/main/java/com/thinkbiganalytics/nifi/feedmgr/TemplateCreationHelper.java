package com.thinkbiganalytics.nifi.feedmgr;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.*;
import com.thinkbiganalytics.nifi.rest.support.NifiConnectionUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.rest.JerseyClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.*;
import org.apache.nifi.web.api.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sr186054 on 5/6/16.
 */
public class TemplateCreationHelper {

    private static final Logger log = LoggerFactory.getLogger(TemplateCreationHelper.class);

    public static String REUSABLE_TEMPLATES_PROCESS_GROUP_NAME = "reusable_templates";

    private List<NifiError> errors = new ArrayList<>();

    NifiRestClient restClient;

    private Set<ControllerServiceDTO> snapshotControllerServices;

    private Set<ControllerServiceDTO> newlyCreatedControllerServices;

    Map<String,Integer> controllerServiceEnableAttempts = new ConcurrentHashMap<>();

    private Integer MAX_ENABLE_ATTEMPTS = 5;
    private Long ENABLE_CONTROLLER_SERVICE_WAIT_TIME = 3000L;

    public TemplateCreationHelper(NifiRestClient restClient){
        this.restClient = restClient;
    }

    public FlowSnippetEntity instantiateFlowFromTemplate(String processGroupId, String templateId) throws JerseyClientException {
        return restClient.instantiateFlowFromTemplate(processGroupId,templateId);
    }


    public void snapshotControllerServiceReferences() throws JerseyClientException {
        ControllerServicesEntity controllerServiceEntity = restClient.getControllerServices();
        if (controllerServiceEntity != null) {
            snapshotControllerServices = controllerServiceEntity.getControllerServices();
        }
    }

    public List<NifiError> getErrors() {
        return errors;
    }

    /**
     * Try to see if there are processors that use process groups and then
     * @param processGroupDTO
     */
    public ControllerServicePropertyHolder validatePropertiesWithControllerServices(ProcessGroupDTO processGroupDTO) throws JerseyClientException {
        List<ControllerServiceProperty> controllerServiceProperties = new ArrayList<>();

        Map<String,ProcessorDTO> processors = NifiProcessUtil.getProcessorsMap(processGroupDTO);
        if(processors != null && !processors.isEmpty()){
            for(ProcessorDTO processor: processors.values()){
                List<PropertyDescriptorDTO> propertyDescriptors = Lists.newArrayList(Iterables.filter(processor.getConfig().getDescriptors().values(), new Predicate<PropertyDescriptorDTO>() {
                    @Override
                    public boolean apply(PropertyDescriptorDTO propertyDescriptorDTO) {
                        return StringUtils.isNotBlank(propertyDescriptorDTO.getIdentifiesControllerService());
                    }
                }));
                if(propertyDescriptors != null){
                    for(PropertyDescriptorDTO propertyDescriptor : propertyDescriptors){
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

        if(!controllerServiceProperties.isEmpty()){
            ControllerServicesEntity controllerServicesEntity = restClient.getControllerServices();
            Map<String,ControllerServiceDTO> controllerServices = new HashMap<>();
            for(ControllerServiceDTO controllerServiceDTO : controllerServicesEntity.getControllerServices()){
                controllerServices.put(controllerServiceDTO.getId(), controllerServiceDTO);
            }

            for(ControllerServiceProperty controllerServiceProperty: controllerServiceProperties){
                ControllerServiceDTO controllerServiceDTO = controllerServices.get(controllerServiceProperty.getPropertyValue());
                String message = "The Controller Service assigned to Processor: "+controllerServiceProperty.getProcessorName()+"["+controllerServiceProperty.getProcessorId()+"] - "+controllerServiceProperty.getPropertyName();
                if(controllerServiceDTO == null){
                    controllerServiceProperty.setValid(false);
                    controllerServiceProperty.setValidationMessage(message+" doesn't exist ");
                }
                else if(controllerServiceDTO.getState().equalsIgnoreCase(NifiProcessUtil.SERVICE_STATE.DISABLED.name())) {
                    controllerServiceProperty.setValid(false);
                    controllerServiceProperty.setValidationMessage(message+" is DISABLED. ");
                }
                else {
                    controllerServiceProperty.setValid(true);
                }

                if(!controllerServiceProperty.isValid()) {
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
    public Set<ControllerServiceDTO> identifyNewlyCreatedControllerServiceReferences() throws JerseyClientException {
        Set<ControllerServiceDTO> newServices = new HashSet<>();
        ControllerServicesEntity controllerServiceEntity = restClient.getControllerServices();
        if (controllerServiceEntity != null) {
            if (snapshotControllerServices != null) {
                for (ControllerServiceDTO dto : controllerServiceEntity.getControllerServices()) {
                    if (!snapshotControllerServices.contains(dto)) {
                        newServices.add(dto);
                    }
                }
            } else {
                newServices = controllerServiceEntity.getControllerServices();
            }
        }
        newlyCreatedControllerServices = newServices;
        return newServices;
    }

    private ControllerServiceEntity tryToEnableControllerService(String serviceId, String name){
        try {
            ControllerServiceEntity entity =  restClient.enableControllerService(serviceId);
        return entity;
        } catch (JerseyClientException e) {
            if(e.getCause().getMessage().contains("409")){
                //wait and try again
                Integer attempt = controllerServiceEnableAttempts.get(serviceId);
                if(attempt == null){
                    attempt = 0;
                    controllerServiceEnableAttempts.put(serviceId,attempt);
                }
                attempt++;
                controllerServiceEnableAttempts.put(serviceId,attempt);
                if(attempt <= MAX_ENABLE_ATTEMPTS) {
                    log.info("Error attempting to enable the controller service {},{}.  Attempt Number: {} .  Waiting {} seconds before trying again", serviceId, name, attempt, ENABLE_CONTROLLER_SERVICE_WAIT_TIME / 1000);
                    try {
                        Thread.sleep(ENABLE_CONTROLLER_SERVICE_WAIT_TIME);
                        tryToEnableControllerService(serviceId,name);
                    } catch (InterruptedException e2) {
e.printStackTrace();
                    }
                }else {
                    log.error("Unable to Enable Controller Service for {}, {}.  Max retry attempts of {} exceeded ",name,serviceId,MAX_ENABLE_ATTEMPTS);
                }

            }

       }
        return null;
    }

    public void updateControllerServiceReferences(List<ProcessorDTO> processors) {

        try {
            ControllerServicesEntity controllerServiceEntity = restClient.getControllerServices();
            final Map<String, ControllerServiceDTO> enabledServices = new HashMap<>();
            Map<String, ControllerServiceDTO> allServices = new HashMap<>();
            for (ControllerServiceDTO dto : controllerServiceEntity.getControllerServices()) {
                if (NifiProcessUtil.SERVICE_STATE.ENABLED.name().equals(dto.getState())) {
                    enabledServices.put(dto.getId(), dto);
                }
                allServices.put(dto.getId(), dto);

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
                properties.addAll(NifiPropertyUtil.getPropertiesForProcessor(groupDTO, dto));
                //properties.addAll(NifiPropertyUtil.get.getPropertiesForProcessor(modifiedProperties, dto.getId()));
                //properties.addAll(NifiPropertyUtil.getPropertiesForProcessor(modifiedProperties,dto.getId()));
            }

            for (final NifiProperty property : properties) {
                String controllerService = property.getPropertyDescriptor().getIdentifiesControllerService();
                boolean isRequired = property.getPropertyDescriptor().isRequired();
                if (StringUtils.isNotBlank(controllerService)) {
                    boolean set = false;

                    //if the service is not enabled, but it exists then try to enable that
                    if (!enabledServices.containsKey(property.getValue()) && allServices.containsKey(property.getValue())) {
                        ControllerServiceDTO dto = allServices.get(property.getValue());
                            ControllerServiceEntity entity = tryToEnableControllerService(dto.getId(),dto.getName());
                            if(entity != null && entity.getControllerService() != null && NifiProcessUtil.SERVICE_STATE.ENABLED.name().equals(entity.getControllerService().getState())) {
                                enabledServices.put(entity.getControllerService().getId(),entity.getControllerService());
                            }
                            set = true;

                    }
                    if (!set) {
                        boolean controllerServiceSet = false;
                        String controllerServiceName = "";
                        // match a allowable service and enable it
                        List<PropertyDescriptorDTO.AllowableValueDTO>
                                allowableValueDTOs =
                                property.getPropertyDescriptor().getAllowableValues();
                        //if any of the allowable values are enabled already use that and continue
                        List<PropertyDescriptorDTO.AllowableValueDTO>
                                enabledValues =
                                Lists.newArrayList(Iterables.filter(allowableValueDTOs, new Predicate<PropertyDescriptorDTO.AllowableValueDTO>() {
                                    @Override
                                    public boolean apply(PropertyDescriptorDTO.AllowableValueDTO allowableValueDTO) {
                                        return enabledServices.containsKey(allowableValueDTO.getValue());
                                    }
                                }));
                        if (enabledValues != null && !enabledValues.isEmpty()) {
                            PropertyDescriptorDTO.AllowableValueDTO enabledService = enabledValues.get(0);
                            ControllerServiceDTO dto = enabledServices.get(enabledService.getValue());
                            controllerServiceName = dto.getName();
                            property.setValue(enabledService.getValue());
                            controllerServiceSet = true;
                        } else {
                            //try to enable the service

                            for (PropertyDescriptorDTO.AllowableValueDTO allowableValueDTO : allowableValueDTOs) {
                                ControllerServiceDTO dto = allServices.get(allowableValueDTO.getValue());
                                if (StringUtils.isBlank(controllerServiceName)) {
                                    controllerServiceName = dto.getName();
                                }
                                if (allServices.containsKey(allowableValueDTO.getValue())) {
                                    property.setValue(allowableValueDTO.getValue());
                                        ControllerServiceEntity entity = tryToEnableControllerService(allowableValueDTO.getValue(),controllerServiceName);
                                        if(entity != null && entity.getControllerService() != null && NifiProcessUtil.SERVICE_STATE.ENABLED.name().equals(entity.getControllerService().getState())) {
                                            enabledServices.put(entity.getControllerService().getId(),entity.getControllerService());
                                        }
                                        controllerServiceSet = true;
                                }
                            }
                        }
                        if (controllerServiceSet) {
                            //update the processor
                            restClient.updateProcessorProperty(property.getProcessGroupId(), property.getProcessorId(), property);
                        }
                        if (!controllerServiceSet && (StringUtils.isNotBlank(property.getValue()) || isRequired) ) {
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

        } catch (JerseyClientException e) {
            errors.add(new NifiError(NifiError.SEVERITY.FATAL, "Error trying to identify Controller Services. " + e.getMessage(),
                    NifiProcessGroup.CONTROLLER_SERVICE_CATEGORY));
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
                            return serviceTypes.contains(controllerServiceDTO.getType());
                        }

                    }));
            if (servicesToDelete != null && !servicesToDelete.isEmpty()) {
                try {
                    restClient.deleteControllerServices(servicesToDelete);
                } catch (JerseyClientException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public void versionProcessGroup(ProcessGroupDTO processGroup) throws JerseyClientException {
        restClient.disableAllInputProcessors(processGroup.getId());
        //attempt to stop all processors
        try {
            restClient.stopAllProcessors(processGroup);
        }catch (JerseyClientException e)
        {

        }
        //delete all connections
        ConnectionsEntity connectionsEntity = restClient.getProcessGroupConnections(processGroup.getParentGroupId());
        if(connectionsEntity != null) {
            List<ConnectionDTO> connections =                 NifiConnectionUtil.findConnectionsMatchingSourceGroupId(connectionsEntity.getConnections(), processGroup.getId());

            if(connections != null) {
                for(ConnectionDTO connection: connections){
                    String type = connection.getDestination().getType();
                    if(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name().equalsIgnoreCase(type)){
                        //stop the port
                        try {
                            restClient.stopOutputPort(connection.getParentGroupId(), connection.getDestination().getId());
                        }catch (JerseyClientException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        restClient.deleteConnection(connection);
                    }catch (JerseyClientException e) {
                        e.printStackTrace();
                    }
                }
            }

            connections = NifiConnectionUtil.findConnectionsMatchingDestinationGroupId(connectionsEntity.getConnections(), processGroup.getId());
            if(connections != null) {
                for(ConnectionDTO connection: connections){
                    String type = connection.getSource().getType();
                    if(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name().equalsIgnoreCase(type)){
                        //stop the port
                        try {
                            restClient.stopInputPort(connection.getParentGroupId(), connection.getSource().getId());
                        }catch (JerseyClientException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        restClient.deleteConnection(connection);
                    }catch (JerseyClientException e) {
                        e.printStackTrace();
                    }
                }
            }

        }



        //rename the feedGroup to be name+timestamp
        //TODO change to work with known version passed in (get the rename to current version -1 or something.
        processGroup.setName(processGroup.getName() +" - "+ new Date().getTime());
        ProcessGroupEntity entity = new ProcessGroupEntity();
        entity.setProcessGroup(processGroup);
        restClient.updateProcessGroup(entity);
    }

    public void markProcessorsAsRunning(NifiProcessGroup newProcessGroup) {
        if (newProcessGroup.isSuccess()) {
            try {
                restClient.markProcessorGroupAsRunning(newProcessGroup.getProcessGroupEntity().getProcessGroup());
            } catch (JerseyClientException e) {
                String errorMsg = "Unable to mark feed as " + NifiProcessUtil.PROCESS_STATE.RUNNING + ".";
                newProcessGroup
                        .addError(newProcessGroup.getProcessGroupEntity().getProcessGroup().getId(), "", NifiError.SEVERITY.WARN, errorMsg,
                                "Process State");
                newProcessGroup.setSuccess(false);
            }
        }
    }

    public void markConnectionPortsAsRunning(ProcessGroupEntity feedProcessGroup){
        //1 startAll
        try {
            restClient.startAll(feedProcessGroup.getProcessGroup().getId(),feedProcessGroup.getProcessGroup().getParentGroupId());
        } catch (JerseyClientException e) {
            e.printStackTrace();
        }

        Set<PortDTO> ports = null;
        try {
            ports = restClient.getPortsForProcessGroup(feedProcessGroup.getProcessGroup().getParentGroupId());
        } catch (JerseyClientException e) {
            e.printStackTrace();
        }
        if(ports != null && !ports.isEmpty()) {
            for(PortDTO port: ports){
                port.setState(NifiProcessUtil.PROCESS_STATE.RUNNING.name());
                if(port.getType().equalsIgnoreCase(NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name())) {
                    try {
                        restClient.startInputPort(feedProcessGroup.getProcessGroup().getParentGroupId(),port.getId());
                    } catch (JerseyClientException e) {
                        e.printStackTrace();
                    }
                }
                else if(port.getType().equalsIgnoreCase(NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name())) {
                    try {
                        restClient.startOutputPort(feedProcessGroup.getProcessGroup().getParentGroupId(), port.getId());
                    } catch (JerseyClientException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }

}
