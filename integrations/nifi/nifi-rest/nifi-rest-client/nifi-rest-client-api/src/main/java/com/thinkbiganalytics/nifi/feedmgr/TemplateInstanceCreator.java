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

import com.google.common.collect.Lists;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.layout.AlignProcessGroupComponents;
import com.thinkbiganalytics.nifi.rest.model.NifiError;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiProcessUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Class used to create templates in NiFi
 */
public class TemplateInstanceCreator {

    private static final Logger log = LoggerFactory.getLogger(TemplateInstanceCreator.class);

    private String templateId;
    private LegacyNifiRestClient restClient;

    private boolean createReusableFlow;

    private Map<String, Object> staticConfigPropertyMap;

    private Map<String, String> staticConfigPropertyStringMap;

    private ReusableTemplateCreationCallback creationCallback;

    public TemplateInstanceCreator(LegacyNifiRestClient restClient, String templateId, Map<String, Object> staticConfigPropertyMap, boolean createReusableFlow,
                                   ReusableTemplateCreationCallback creationCallback) {
        this.restClient = restClient;
        this.templateId = templateId;
        this.createReusableFlow = createReusableFlow;
        this.staticConfigPropertyMap = staticConfigPropertyMap;
        if (staticConfigPropertyMap != null) {
            //transform the object map to the String map
            staticConfigPropertyStringMap = staticConfigPropertyMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() != null ? e.getValue().toString() : null));
        }
        if (staticConfigPropertyStringMap == null) {
            staticConfigPropertyStringMap = new HashMap<>();
        }
        this.creationCallback = creationCallback;
    }

    public boolean isCreateReusableFlow() {
        return createReusableFlow;
    }

    private void ensureInputPortsForReuseableTemplate(String processGroupId) {
        ProcessGroupDTO template = restClient.getProcessGroup(processGroupId, false, false);
        String categoryId = template.getParentGroupId();
        restClient.createReusableTemplateInputPort(categoryId, processGroupId);
    }


    public NifiProcessGroup createTemplate() throws TemplateCreationException {
        try {

            NifiProcessGroup newProcessGroup = null;
            TemplateDTO template = restClient.getTemplateById(templateId);

            if (template != null) {

                TemplateCreationHelper templateCreationHelper = new TemplateCreationHelper(this.restClient);
                String processGroupId = null;

                ProcessGroupDTO group = null;
                if (isCreateReusableFlow()) {
                    log.info("Creating a new Reusable flow instance for template: {} ", template.getName());
                    //1 get/create the parent "reusable_templates" processgroup
                    ProcessGroupDTO reusableParentGroup = restClient.getProcessGroupByName("root", TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME);
                    if (reusableParentGroup == null) {
                        reusableParentGroup = restClient.createProcessGroup("root", TemplateCreationHelper.REUSABLE_TEMPLATES_PROCESS_GROUP_NAME);
                    }
                    ProcessGroupDTO thisGroup = restClient.getProcessGroupByName(reusableParentGroup.getId(), template.getName());
                    if (thisGroup != null) {
                        //version the group
                        log.info("A previous Process group of with this name {} was found.  Versioning it before continuing", thisGroup.getName());
                        templateCreationHelper.versionProcessGroup(thisGroup);

                    }
                    group = restClient.createProcessGroup(reusableParentGroup.getId(), template.getName());
                } else {
                    String tmpName = template.getName() + "_" + System.currentTimeMillis();
                    group = restClient.createProcessGroup(tmpName);
                    log.info("Creating a temporary process group with name {} for template {} ", tmpName, template.getName());
                }
                processGroupId = group.getId();
                if (StringUtils.isNotBlank(processGroupId)) {
                    //snapshot the existing controller services
                    templateCreationHelper.snapshotControllerServiceReferences();
                    log.info("Successfully Snapshot of controller services");
                    //create the flow from the template
                    templateCreationHelper.instantiateFlowFromTemplate(processGroupId, templateId);
                    log.info("Successfully created the temp flow");

                    if (this.createReusableFlow) {
                        ensureInputPortsForReuseableTemplate(processGroupId);
                        log.info("Reusable flow, input ports created successfully.");
                    }

                    //mark the new services that were created as a result of creating the new flow from the template
                    templateCreationHelper.identifyNewlyCreatedControllerServiceReferences();

                    ProcessGroupDTO entity = restClient.getProcessGroup(processGroupId, true, true);

                    //replace static properties and inject values into the flow

                    List<NifiProperty> processorProperties = NifiPropertyUtil.getProperties(entity, restClient.getPropertyDescriptorTransform());
                    if (processorProperties != null) {
                        boolean didReplace = false;
                        for (NifiProperty property : processorProperties) {
                            boolean replaced = ConfigurationPropertyReplacer.resolveStaticConfigurationProperty(property, staticConfigPropertyMap);
                            if (replaced) {
                                //update the properties that are replaced
                                restClient.updateProcessorProperty(property.getProcessGroupId(), property.getProcessorId(), property);
                                didReplace = true;
                            }
                        }
                        //if we replaced any properties, refetch to get the latest data
                        if (didReplace) {
                            entity = restClient.getProcessGroup(processGroupId, true, true);
                        }
                    }

                    //identify the various processors (first level initial processors)
                    List<ProcessorDTO> inputProcessors = NifiProcessUtil.getInputProcessors(entity);

                    ProcessorDTO input = null;
                    List<ProcessorDTO> nonInputProcessors = NifiProcessUtil.getNonInputProcessors(entity);

                    //if the input is null attempt to get the first input available on the template
                    if (input == null && inputProcessors != null && !inputProcessors.isEmpty()) {
                        input = inputProcessors.get(0);
                    }

                    log.info("Attempt to update/validate controller services for template.");
                    //update any references to the controller services and try to assign the value to an enabled service if it is not already
                    boolean updatedControllerServices = false;
                    if (input != null) {
                        log.info("attempt to update controllerservices on {} input processor ", input.getName());
                        List<NifiProperty> updatedProperties = templateCreationHelper.updateControllerServiceReferences(Lists.newArrayList(inputProcessors), staticConfigPropertyStringMap);
                        updatedControllerServices = !updatedProperties.isEmpty();
                    }
                    log.info("attempt to update controllerservices on {} processors ", (nonInputProcessors != null ? nonInputProcessors.size() : 0));
                    List<NifiProperty> updatedProperties = templateCreationHelper.updateControllerServiceReferences(nonInputProcessors, staticConfigPropertyStringMap);
                    if(!updatedControllerServices){
                        updatedControllerServices = !updatedProperties.isEmpty();
                    }
                    log.info("Controller service validation complete");
                    //refetch processors for updated errors
                    entity = restClient.getProcessGroup(processGroupId, true, true);
                    nonInputProcessors = NifiProcessUtil.getNonInputProcessors(entity);

                    inputProcessors = NifiProcessUtil.getInputProcessors(entity);
                    if(inputProcessors != null && !inputProcessors.isEmpty()) {
                        input = inputProcessors.get(0);
                    }


                    ///make the input/output ports in the category group as running
                    if (isCreateReusableFlow()) {
                        log.info("Reusable flow, attempt to mark the connection ports as running.");
                        templateCreationHelper.markConnectionPortsAsRunning(entity);
                        log.info("Reusable flow.  Successfully marked the ports as running.");
                    }

                    newProcessGroup = new NifiProcessGroup(entity, input, nonInputProcessors);

                    if (isCreateReusableFlow()) {
                        //call listeners notify of before mark as running  processing
                        if (creationCallback != null) {
                            try {
                                creationCallback.beforeMarkAsRunning(template.getName(), entity);
                            } catch (Exception e) {
                                log.error("Error calling callback beforeMarkAsRunning ", e);
                            }
                        }
                        log.info("Reusable flow, attempt to mark the Processors as running.");
                        templateCreationHelper.markProcessorsAsRunning(newProcessGroup);
                        log.info("Reusable flow.  Successfully marked the Processors as running.");
                        //align items
                        AlignProcessGroupComponents alignProcessGroupComponents = new AlignProcessGroupComponents(restClient.getNiFiRestClient(), entity.getParentGroupId());
                        alignProcessGroupComponents.autoLayout();
                    }

                    templateCreationHelper.cleanupControllerServices();
                    log.info("Controller service cleanup complete");
                    List<NifiError> errors = templateCreationHelper.getErrors();
                    //add any global errors to the object
                    if (errors != null && !errors.isEmpty()) {
                        for (NifiError error : errors) {
                            newProcessGroup.addError(error);
                        }
                    }

                    newProcessGroup.setSuccess(!newProcessGroup.hasFatalErrors());

                    log.info("Finished importing template Errors found.  Success: {}, {} {}", newProcessGroup.isSuccess(), (errors != null ? errors.size() : 0),
                             (errors != null ? " - " + StringUtils.join(errors) : ""));

                    return newProcessGroup;

                }
            }
        } catch (final Exception e) {
            if (log.isDebugEnabled() && e instanceof WebApplicationException) {
                final Response response = ((WebApplicationException) e).getResponse();
                log.debug("NiFi server returned error: {}", response.readEntity(String.class), e);
            }
            throw new TemplateCreationException("Unable to create the template for the Id of [" + templateId + "]. " + e.getMessage(), e);
        }

        return null;
    }
}
