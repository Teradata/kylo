package com.thinkbiganalytics.nifi.feedmgr;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.rest.JerseyClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ControllerServiceDTO;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.PropertyDescriptorDTO;
import org.apache.nifi.web.api.entity.ControllerServiceEntity;
import org.apache.nifi.web.api.entity.ControllerServicesEntity;
import org.apache.nifi.web.api.entity.FlowSnippetEntity;

import java.util.*;

/**
 * Created by sr186054 on 5/6/16.
 */
public class TemplateCreationHelper {

    private List<NifiError> errors = new ArrayList<>();

    NifiRestClient restClient;

    private Set<ControllerServiceDTO> snapshotControllerServices;

    private Set<ControllerServiceDTO> newlyCreatedControllerServices;

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
                if (StringUtils.isNotBlank(controllerService)) {
                    boolean set = false;

                    //if the service is not enabled, but it exists then try to enable that
                    if (!enabledServices.containsKey(property.getValue()) && allServices.containsKey(property.getValue())) {
                        ControllerServiceDTO dto = allServices.get(property.getValue());
                        try {
                            restClient.enableControllerService(dto.getId());
                            set = true;
                        } catch (JerseyClientException e) {
                            //errors.add(new NifiError(NifiError.SEVERITY.WARN,"Error trying to enable Controller Service " + dto.getName() +" on referencing Processor: " + property.getProcessorName() + " and field " + property.getKey() + ". Please go to Nifi and configure and enable this Service before creating this feed.", "Controller Services"));

                        }
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
                                    try {
                                        ControllerServiceEntity
                                                entity = restClient.enableControllerService(allowableValueDTO.getValue());
                                        if (entity != null && NifiProcessUtil.SERVICE_STATE.ENABLED.name()
                                                .equalsIgnoreCase(entity.getControllerService().getState())) {
                                            controllerServiceSet = true;
                                            break;
                                        }
                                    } catch (JerseyClientException e) {
                                        //errors will be handled downstream

                                    }
                                }
                            }
                        }
                        if (controllerServiceSet) {
                            //update the processor
                            restClient.updateProcessorProperty(property.getProcessGroupId(), property.getProcessorId(), property);
                        }
                        if (!controllerServiceSet) {
                            errors.add(new NifiError(NifiError.SEVERITY.WARN,
                                    "Error trying to enable Controller Service " + controllerServiceName
                                            + " on referencing Processor: " + property.getProcessorName() + " and field " + property
                                            .getKey()
                                            + ". Please go to Nifi and configure and enable this Service before creating this feed.",
                                    "Controller Services"));
                        }

                    }
                }
            }

        } catch (JerseyClientException e) {
            errors.add(new NifiError(NifiError.SEVERITY.FATAL, "Error trying to identify Controller Services. " + e.getMessage(),
                    "Controller Services"));
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
}
