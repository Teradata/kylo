package com.thinkbiganalytics.feedmgr.service.template;

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

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplateRequest;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.template.TemplateModelTransform.TEMPLATE_TRANSFORMATION_TYPE;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.template.security.TemplateAccessControl;
import com.thinkbiganalytics.nifi.rest.client.LegacyNifiRestClient;
import com.thinkbiganalytics.nifi.rest.client.NifiComponentNotFoundException;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiFeedConstants;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.nifi.rest.support.NifiTemplateUtil;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.Action;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.nifi.web.api.dto.ProcessorDTO;
import org.apache.nifi.web.api.dto.TemplateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AccessControlException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

/**
 *
 */
public class RegisteredTemplateService {

    private static final Logger log = LoggerFactory.getLogger(RegisteredTemplateService.class);

    @Inject
    FeedManagerTemplateProvider templateProvider;

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    TemplateModelTransform templateModelTransform;

    @Inject
    private AccessController accessController;

    @Inject
    private LegacyNifiRestClient nifiRestClient;


    @Inject
    private RegisteredTemplateUtil registeredTemplateUtil;

    @Inject
    private NiFiTemplateCache niFiTemplateCache;

    @Inject
    private RegisteredTemplateCache registeredTemplateCache;

    /**
     * Checks the current security context has been granted permission to perform the specified action(s)
     * on the template with the specified ID.  If the template does not exist then no check is made.
     *
     * @param id     the template ID
     * @param action an action to check
     * @param more   any additional actions to check
     * @return true if the template existed, otherwise false
     * @throws AccessControlException thrown if the template exists and the action(s) checked are not permitted
     */
    public boolean checkTemplatePermission(final String id, final Action action, final Action... more) {
        if (accessController.isEntityAccessControlled()) {
            return metadataAccess.read(() -> {
                final FeedManagerTemplate.ID domainId = templateProvider.resolveId(id);
                final FeedManagerTemplate domainTemplate = templateProvider.findById(domainId);

                if (domainTemplate != null) {
                    domainTemplate.getAllowedActions().checkPermission(action, more);
                    return true;
                } else {
                    return false;
                }
            });
        } else {
            return true;
        }
    }

    /**
     * Checks the current security context has been granted permission to perform the specified action(s)
     * on the template with the specified ID.  If the template does not exist then no check is made.
     *
     * @param id     the template ID
     * @param action an action to check
     * @param more   any additional actions to check
     * @return true if the template existed and the check passed, otherwise false
     */
    public boolean hasTemplatePermission(final String id, final Action action, final Action... more) {
        if (accessController.isEntityAccessControlled()) {
            return metadataAccess.read(() -> {
                final FeedManagerTemplate.ID domainId = templateProvider.resolveId(id);
                final FeedManagerTemplate domainTemplate = templateProvider.findById(domainId);
                return domainTemplate != null && domainTemplate.getAllowedActions().hasPermission(action, more);
            });
        } else {
            return true;
        }
    }

    /**
     * Gets a Registered Template or returns null if not found by various means passed in via the request object
     *
     * @param registeredTemplateRequest a request to get a registered template
     * @return the RegisteredTemplate or null if not found
     */
    public RegisteredTemplate findRegisteredTemplate(RegisteredTemplateRequest registeredTemplateRequest) {

        String templateId = registeredTemplateRequest.getTemplateId();
        String templateName = registeredTemplateRequest.getTemplateName();

        //if we are looking for a given template as a request from a Feed, we need to query and access it via a service account.
        //otherwise we will access it as the user
        Principal[] principals = null;
        if (registeredTemplateRequest.isFeedEdit()) {
            principals = new Principal[1];
            principals[0] = MetadataAccess.SERVICE;

        } else {
            principals = new Principal[0];
        }
        //The default transformation type will not include sensitive property values.
        //if requested as a template or feed edit, it will include the encrypted sensitive property values
        TEMPLATE_TRANSFORMATION_TYPE transformationType = TEMPLATE_TRANSFORMATION_TYPE.WITH_FEED_NAMES;
        if (registeredTemplateRequest.isTemplateEdit() || registeredTemplateRequest.isFeedEdit() || registeredTemplateRequest.isIncludeSensitiveProperties()) {
            transformationType = TEMPLATE_TRANSFORMATION_TYPE.WITH_SENSITIVE_DATA;
        }

        RegisteredTemplate registeredTemplate = findRegisteredTemplateById(templateId, transformationType, principals);
        //if it is null check to see if the template exists in nifi and is already registered
        if (registeredTemplate == null) {
            //  log.info("Attempt to get Template with id {}, returned null.  This id must be one registed in Nifi... attempt to query Nifi for this template ", templateId);
            registeredTemplate = findRegisteredTemplateByNiFiIdentifier(templateId, transformationType, principals);
        }
        if (registeredTemplate == null) {
            //attempt to look by name
            registeredTemplate = findRegisteredTemplateByName(templateName, transformationType, principals);

        }
        if (registeredTemplate != null) {
            if (registeredTemplateRequest.isIncludeAllProperties()) {
                registeredTemplate = mergeRegisteredTemplateProperties(registeredTemplate, registeredTemplateRequest);
                registeredTemplate.initializeProcessors();
                ensureRegisteredTemplateInputProcessors(registeredTemplate);
            }
            if (NifiPropertyUtil.containsPropertiesForProcessorMatchingType(registeredTemplate.getProperties(), NifiFeedConstants.TRIGGER_FEED_PROCESSOR_CLASS)) {
                registeredTemplate.setAllowPreconditions(true);
            } else {
                registeredTemplate.setAllowPreconditions(false);
            }
        }
        return registeredTemplate;
    }

    /**
     * Find a template by the Kylo Id
     *
     * @param templateId The Kylo {@link RegisteredTemplate#id}
     * @return the RegisteredTemplate matching the id or null if not found
     */
    public RegisteredTemplate findRegisteredTemplateById(final String templateId) {
        return findRegisteredTemplateById(templateId, null);
    }

    /**
     * Find a template by the Kylo Id
     *
     * @param templateId The Kylo {@link RegisteredTemplate#id}
     * @param principals list or principals required to access
     * @return the RegisteredTemplate matching the id or null if not found
     */
    private RegisteredTemplate findRegisteredTemplateById(final String templateId, TEMPLATE_TRANSFORMATION_TYPE transformationType, Principal... principals) {
        if (StringUtils.isBlank(templateId)) {
            return null;
        } else {
            return metadataAccess.read(() -> {
                this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_TEMPLATES);

                RegisteredTemplate registeredTemplate = null;
                FeedManagerTemplate.ID domainId = templateProvider.resolveId(templateId);
                FeedManagerTemplate domainTemplate = templateProvider.findById(domainId);
                if (domainTemplate != null) {
                    //transform it
                    registeredTemplate = templateModelTransform.getTransformationFunction(transformationType).apply(domainTemplate);
                }
                if (registeredTemplate != null) {
                    registeredTemplate.initializeProcessors();
                    ensureNifiTemplate(registeredTemplate);
                }

                return registeredTemplate;
            }, principals);
        }

    }

    /**
     * Find a template by the nifi id
     *
     * @param nifiTemplateId the nifi id
     * @return the RegisteredTemplate matching the passed in nifiTemplateId, or null if not found
     */
    public RegisteredTemplate findRegisteredTemplateByNiFiIdentifier(final String nifiTemplateId) {
        return findRegisteredTemplateByNiFiIdentifier(nifiTemplateId, null);
    }


    /**
     * Find a template by the nifi id
     *
     * @param nifiTemplateId the nifi id
     * @param principals     list of principals required to access
     * @return the RegisteredTemplate matching the passed in nifiTemplateId, or null if not found
     */
    private RegisteredTemplate findRegisteredTemplateByNiFiIdentifier(final String nifiTemplateId, TEMPLATE_TRANSFORMATION_TYPE transformationType, Principal... principals) {
        if (StringUtils.isBlank(nifiTemplateId)) {
            return null;
        }
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_TEMPLATES);

            RegisteredTemplate registeredTemplate = null;
            FeedManagerTemplate template = templateProvider.findByNifiTemplateId(nifiTemplateId);
            if (template != null) {
                registeredTemplate = templateModelTransform.getTransformationFunction(transformationType).apply(template);
            }
            return registeredTemplate;
        }, principals);


    }

    /**
     * Find a template by the name
     *
     * @param templateName the name of the template
     * @return the RegisteredTemplate matching the passed in name or null if not found
     */
    public RegisteredTemplate findRegisteredTemplateByName(final String templateName) {
        return findRegisteredTemplateByName(templateName, null);
    }

    /**
     * Find a template by the name
     *
     * @param templateName the name of the template
     * @param principals   list of principals required to access
     * @return the RegisteredTemplate matching the passed in name or null if not found
     */
    public RegisteredTemplate findRegisteredTemplateByName(final String templateName, TEMPLATE_TRANSFORMATION_TYPE transformationType, Principal... principals) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_TEMPLATES);

            RegisteredTemplate registeredTemplate = null;
            FeedManagerTemplate template = templateProvider.findByName(templateName);
            if (template != null) {
                registeredTemplate = templateModelTransform.getTransformationFunction(transformationType).apply(template);
            }
            return registeredTemplate;
        }, principals);


    }

    /**
     * Return a registered template object that is populated for use with updating in Kylo
     *
     * @param registeredTemplateRequest the request to get a registered template
     * @return a RegisteredTemplate object mapping either one already defined in Kylo, or a new template that maps to one in NiFi
     */
    public RegisteredTemplate getRegisteredTemplateForUpdate(RegisteredTemplateRequest registeredTemplateRequest) {

        if (registeredTemplateRequest.isTemplateEdit()) {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_TEMPLATES);
        }

        RegisteredTemplate registeredTemplate = null;
        //attempt to find the template as a Service
        RegisteredTemplateRequest serviceLevelRequest = new RegisteredTemplateRequest(registeredTemplateRequest);
        //editing a feed will run as a service account
        serviceLevelRequest.setFeedEdit(true);
        RegisteredTemplate template = findRegisteredTemplate(serviceLevelRequest);
        boolean canEdit = true;
        if (template != null && StringUtils.isNotBlank(template.getId()) && registeredTemplateRequest.isTemplateEdit()) {
            canEdit = checkTemplatePermission(template.getId(), TemplateAccessControl.EDIT_TEMPLATE);
        }
        if (canEdit) {
            registeredTemplate = template;
            if (registeredTemplate == null) {
                registeredTemplate = nifiTemplateToRegisteredTemplate(registeredTemplateRequest.getNifiTemplateId());
            }
            if (registeredTemplate == null) {
                //throw exception
            } else {
                if (StringUtils.isBlank(registeredTemplate.getId()) && template != null && StringUtils.isNotBlank(template.getId())) {
                    registeredTemplate.setId(template.getId());
                }
                Set<PortDTO> ports = null;
                // fetch ports for this template
                try {
                    if (registeredTemplate.getNifiTemplate() != null) {
                        ports = nifiRestClient.getPortsForTemplate(registeredTemplate.getNifiTemplate());
                    } else {
                        ports = nifiRestClient.getPortsForTemplate(registeredTemplate.getNifiTemplateId());
                    }
                } catch (NifiComponentNotFoundException notFoundException) {
                    syncNiFiTemplateId(registeredTemplate);
                    ports = nifiRestClient.getPortsForTemplate(registeredTemplate.getNifiTemplateId());
                }
                if (ports == null) {
                    ports = new HashSet<>();
                }
                List<PortDTO>
                    outputPorts =
                    ports.stream().filter(portDTO -> portDTO != null && NifiConstants.NIFI_PORT_TYPE.OUTPUT_PORT.name().equalsIgnoreCase(portDTO.getType())).collect(Collectors.toList());

                List<PortDTO>
                    inputPorts =
                    ports.stream().filter(portDTO -> portDTO != null && NifiConstants.NIFI_PORT_TYPE.INPUT_PORT.name().equalsIgnoreCase(portDTO.getType())).collect(Collectors.toList());
                registeredTemplate.setReusableTemplate(inputPorts != null && !inputPorts.isEmpty());
                List<ReusableTemplateConnectionInfo> reusableTemplateConnectionInfos = registeredTemplate.getReusableTemplateConnections();
                List<ReusableTemplateConnectionInfo> updatedConnectionInfo = new ArrayList<>();

                for (final PortDTO port : outputPorts) {

                    ReusableTemplateConnectionInfo reusableTemplateConnectionInfo = null;
                    if (reusableTemplateConnectionInfos != null && !reusableTemplateConnectionInfos.isEmpty()) {
                        reusableTemplateConnectionInfo = Iterables.tryFind(reusableTemplateConnectionInfos,
                                                                           reusableTemplateConnectionInfo1 -> reusableTemplateConnectionInfo1
                                                                               .getFeedOutputPortName()
                                                                               .equalsIgnoreCase(port.getName())).orNull();
                    }
                    if (reusableTemplateConnectionInfo == null) {
                        reusableTemplateConnectionInfo = new ReusableTemplateConnectionInfo();
                        reusableTemplateConnectionInfo.setFeedOutputPortName(port.getName());
                    }
                    updatedConnectionInfo.add(reusableTemplateConnectionInfo);

                }

                registeredTemplate.setReusableTemplateConnections(updatedConnectionInfo);
                registeredTemplate.initializeProcessors();
                ensureRegisteredTemplateInputProcessors(registeredTemplate);

            }
        }

        return registeredTemplate;
    }


    public List<RegisteredTemplate> getRegisteredTemplates() {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.ACCESS_TEMPLATES);

            List<RegisteredTemplate> registeredTemplates = null;
            List<FeedManagerTemplate> templates = templateProvider.findAll();
            if (templates != null) {
                templates.stream().filter(t -> t.getNifiTemplateId() == null).forEach(t -> {
                    ensureNifiTemplateId(t);
                });

                registeredTemplates = templateModelTransform.domainToRegisteredTemplateWithFeedNames(templates);

            }
            return registeredTemplates;
        });

    }


    /**
     * this will return a RegisteredTemplate object for a given NiFi template Id.
     * This is to be used for new templates that are going to be registered with Kylo.
     * Callers of this method should ensure that a template of this id is not already registered
     *
     * @param nifiTemplateId the nifi template identifier
     * @return a RegisteredTemplate object of null if not found in NiFi
     */
    private RegisteredTemplate nifiTemplateToRegisteredTemplate(String nifiTemplateId) {

        RegisteredTemplate registeredTemplate = null;
        List<NifiProperty> properties = new ArrayList<>();
        TemplateDTO nifiTemplate = nifiRestClient.getTemplateById(nifiTemplateId);
        if (nifiTemplate != null) {
            registeredTemplate = new RegisteredTemplate();
            registeredTemplate.setNifiTemplateId(nifiTemplateId);

            properties = niFiTemplateCache.getTemplateProperties(nifiTemplate, true, null);
            registeredTemplate.setNifiTemplate(nifiTemplate);
            registeredTemplate.setTemplateName(nifiTemplate.getName());
            registeredTemplate.setProperties(properties);
        }

        return registeredTemplate;

    }


    /**
     * Merge all saved properties on the RegisteredTemplate along with the properties in the NiFi template
     * The resulting object will have the {@link RegisteredTemplate#properties} updated so they are in sync with NiFi.
     *
     * @return a RegisteredTemplate that has the properties updated with those in NiFi
     */
    public RegisteredTemplate mergeRegisteredTemplateProperties(RegisteredTemplate registeredTemplate, RegisteredTemplateRequest registeredTemplateRequest) {
        if (registeredTemplate != null) {
            log.info("Merging properties for template {} ({})", registeredTemplate.getTemplateName(), registeredTemplate.getId());
            List<NifiProperty> properties = null;
            int matchCount = 0;

            //get the nifi template associated with this one that is registered
            TemplateDTO templateDTO = registeredTemplate.getNifiTemplate();
            if (templateDTO == null) {
                templateDTO = ensureNifiTemplate(registeredTemplate);
            }

            if (templateDTO != null) {
                registeredTemplate.setNifiTemplate(templateDTO);
                properties = niFiTemplateCache.getTemplateProperties(templateDTO, registeredTemplateRequest.isIncludePropertyDescriptors(), registeredTemplate);
                //first attempt to match the properties by the processorid and processor name
                //     NifiPropertyUtil
                //         .matchAndSetPropertyByIdKey(properties, registeredTemplate.getProperties(), NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_ALL_PROPERTIES);
            }

         /*   if (properties != null) {
                //match the properties to the processors by the processor name
                NifiPropertyUtil.matchAndSetPropertyByProcessorName(properties, registeredTemplate.getProperties(),
                                                                    NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.UPDATE_ALL_PROPERTIES);
            }
            */
            if (templateDTO != null && !templateDTO.getId().equalsIgnoreCase(registeredTemplate.getNifiTemplateId())) {
                syncNiFiTemplateId(registeredTemplate);

            }
            if (properties == null) {
                properties = new ArrayList<>();
            }
            //merge with the registered properties

            RegisteredTemplate copy = new RegisteredTemplate(registeredTemplate);
            copy.setProperties(properties);
            copy.setNifiTemplate(registeredTemplate.getNifiTemplate());

            registeredTemplate = copy;

        } else {
            log.info("Unable to merge Registered Template.  It is null");
        }
        return registeredTemplate;
    }


    public FeedMetadata mergeTemplatePropertiesWithFeed(FeedMetadata feedMetadata) {
        //gets the feed data and then gets the latest template associated with that feed and merges the properties into the feed

        RegisteredTemplate
            registeredTemplate =
            findRegisteredTemplate(
                new RegisteredTemplateRequest.Builder().templateId(feedMetadata.getTemplateId()).isFeedEdit(true).nifiTemplateId(feedMetadata.getTemplateId()).includeAllProperties(true).build());
        if (registeredTemplate != null) {
            feedMetadata.setTemplateId(registeredTemplate.getId());

            List<NifiProperty> templateProperties =registeredTemplate.getProperties().stream().map(nifiProperty -> new NifiProperty(nifiProperty)).collect(Collectors.toList());
            NifiPropertyUtil
                .matchAndSetPropertyByProcessorName(templateProperties, feedMetadata.getProperties(), NifiPropertyUtil.PROPERTY_MATCH_AND_UPDATE_MODE.FEED_DETAILS_MATCH_TEMPLATE);

            //detect template properties that dont match the feed.properties from the registeredtemplate
            ensureFeedPropertiesExistInTemplate(feedMetadata, templateProperties);
            feedMetadata.setProperties(templateProperties);
            registeredTemplate.setProperties(templateProperties);
            registeredTemplate.initializeProcessors();
            feedMetadata.setRegisteredTemplate(registeredTemplate);

        }

        return feedMetadata;

    }


    /**
     * If a Template changes the Processor Names the Feed Properties will no longer be associated to the correct processors
     * This will match any feed properties to a whose processor name has changed in
     * the template to the template processor/property based upon the template processor type.
     */
    private void ensureFeedPropertiesExistInTemplate(FeedMetadata feed, List<NifiProperty> templateProperties) {
        Set<String> templateProcessors = templateProperties.stream().map(property -> property.getProcessorName()).collect(Collectors.toSet());

        //Store the template Properties
        Map<String, String> templateProcessorIdProcessorNameMap = new HashMap<>();
        Map<String, String> templateProcessorTypeProcessorIdMap = new HashMap<>();
        templateProperties.stream().filter(property -> !templateProcessorIdProcessorNameMap.containsKey(property.getProcessorId())).forEach(property1 -> {
            templateProcessorIdProcessorNameMap.put(property1.getProcessorId(), property1.getProcessorName());
            templateProcessorTypeProcessorIdMap.put(property1.getProcessorType(), property1.getProcessorId());
        });

        Map<String, Map<String, NifiProperty>> templatePropertiesByProcessorIdMap = new HashMap<>();
        templateProperties.stream().forEach(property -> {
            templatePropertiesByProcessorIdMap.computeIfAbsent(property.getProcessorId(), key -> new HashMap<String, NifiProperty>()).put(property.getKey(), property);
        });

        //store the Feed Properties
        Map<String, String> processorIdProcessorTypeMap = new HashMap<>();
        feed.getProperties().stream().filter(property -> !processorIdProcessorTypeMap.containsKey(property.getProcessorId())).forEach(property1 -> {
            processorIdProcessorTypeMap.put(property1.getProcessorId(), property1.getProcessorType());
        });

        feed.getProperties().stream().filter(property -> !templateProcessors.contains(property.getProcessorName())).forEach(property -> {
            //if the property doesn't match the template but the type matches try to merge in the feed properties overwriting the template ones
            String processorType = processorIdProcessorTypeMap.get(property.getProcessorId());
            if (processorType != null) {
                String templateProcessorId = templateProcessorTypeProcessorIdMap.get(processorType);

                if (templateProcessorId != null && templateProcessorIdProcessorNameMap.containsKey(templateProcessorId)) {
                    NifiProperty templateProperty = templatePropertiesByProcessorIdMap.get(templateProcessorId).get(property.getKey());
                    if (templateProperty != null) {
                        //replace it from the collection with a copy
                        NifiProperty copy = new NifiProperty(templateProperty);
                        copy.setValue(property.getValue());
                        copy.setRenderType(property.getRenderType());
                        copy.setRenderOptions(property.getRenderOptions());
                        templateProperties.remove(templateProperty);
                        templateProperties.add(copy);
                    }
                }
            }
        });


    }


    /**
     * Return the NiFi {@link TemplateDTO} object fully populated and sets this to the incoming {@link RegisteredTemplate#nifiTemplate}
     * If at first looking at the {@link RegisteredTemplate#nifiTemplateId} it is unable to find the template it will then fallback and attempt to find the template by its name
     *
     * @param registeredTemplate a registered template object
     * @return the NiFi template
     */
    private TemplateDTO ensureNifiTemplate(RegisteredTemplate registeredTemplate) {
        return niFiTemplateCache.ensureNifiTemplate(registeredTemplate);
    }


    /**
     * Ensures that the {@code RegisteredTemplate#inputProcessors} list is populated not only with the processors which were defined as having user inputs, but also those that done require any input
     */
    public void ensureRegisteredTemplateInputProcessors(RegisteredTemplate registeredTemplate) {
        registeredTemplate.initializeInputProcessors();
        List<RegisteredTemplate.Processor> nifiProcessors = getInputProcessorsInNifTemplate(registeredTemplate);
        if (nifiProcessors == null) {
            nifiProcessors = Collections.emptyList();
        }
        List<RegisteredTemplate.Processor> validInputProcessors = nifiProcessors.stream().filter(RegisteredTemplate.isValidInputProcessor()).collect(Collectors.toList());

        //add in any processors not in the map
        validInputProcessors.stream().forEach(processor -> {

            boolean match = registeredTemplate.getInputProcessors().stream().anyMatch(
                registeredProcessor -> registeredProcessor.getId().equals(processor.getId()) || (registeredProcessor.getType().equals(processor.getType()) && registeredProcessor.getName()
                    .equals(processor.getName())));
            if (!match) {
                log.info("Adding Processor {} to registered ", processor.getName());
                registeredTemplate.getInputProcessors().add(processor);
            }

        });

    }


    /**
     * Ensure that the NiFi template Ids are correct and match our metadata for the Template Name
     *
     * @param template a registered template
     * @return the updated template with the {@link RegisteredTemplate#nifiTemplateId} correctly matching NiFi
     */
    public RegisteredTemplate syncNiFiTemplateId(RegisteredTemplate template) {
        String oldId = template.getNifiTemplateId();
        if (oldId == null) {
            oldId = "";
        }
        String nifiTemplateId = nifiTemplateIdForTemplateName(template.getTemplateName());
        if (nifiTemplateId != null && !oldId.equalsIgnoreCase(nifiTemplateId)) {
            template.setNifiTemplateId(nifiTemplateId);
            return metadataAccess.commit(() -> {
                RegisteredTemplate t = findRegisteredTemplateById(template.getId());
                t.setNifiTemplateId(nifiTemplateId);
                return saveTemplate(t);
            }, MetadataAccess.ADMIN);
        }

        return template;

    }

    private FeedManagerTemplate ensureNifiTemplateId(FeedManagerTemplate feedManagerTemplate) {
        if (feedManagerTemplate.getNifiTemplateId() == null) {
            String nifiTemplateId = nifiTemplateIdForTemplateName(feedManagerTemplate.getName());
            feedManagerTemplate.setNifiTemplateId(nifiTemplateId);
        }
        return feedManagerTemplate;
    }


    public RegisteredTemplate saveRegisteredTemplate(final RegisteredTemplate registeredTemplate) {

        return saveRegisteredTemplate(registeredTemplate, true);
    }

    private RegisteredTemplate saveRegisteredTemplate(final RegisteredTemplate registeredTemplate, boolean reorder) {
        List<String> templateOrder = registeredTemplate.getTemplateOrder();
        registeredTemplate.setUpdated(false);
        RegisteredTemplate savedTemplate = metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_TEMPLATES);
            return saveTemplate(registeredTemplate);
        });

        //order it
        if (reorder) {
            if (StringUtils.isBlank(registeredTemplate.getId())) {
                templateOrder = templateOrder.stream().map(template -> {
                    if ("NEW".equals(template)) {
                        return savedTemplate.getId();
                    } else {
                        return template;
                    }
                }).collect(Collectors.toList());
            }

            orderTemplates(templateOrder, Sets.newHashSet(savedTemplate.getId()));
        }

        return savedTemplate;

    }


    /**
     * This needs to be wrapped in a MetadataAccess transaction
     *
     * @param registeredTemplate the template to save
     * @return the saved template
     */
    private RegisteredTemplate saveTemplate(RegisteredTemplate registeredTemplate) {

        //ensure that the incoming template name doesnt already exist.
        //if so remove and replace with this one
        RegisteredTemplate template = findRegisteredTemplate(RegisteredTemplateRequest.requestByTemplateName(registeredTemplate.getTemplateName()));
        if (registeredTemplate.getId() == null && template != null) {
            registeredTemplate.setId(template.getId());
        }
        if (template != null && !template.getId().equalsIgnoreCase(registeredTemplate.getId())) {
            //Warning cant save.. duplicate Name
            log.error("Unable to save template {}.  There is already a template with this name registered in the system", registeredTemplate.getTemplateName());
            return null;
        } else {
            if (StringUtils.isNotBlank(registeredTemplate.getId())) {
                checkTemplatePermission(registeredTemplate.getId(), TemplateAccessControl.EDIT_TEMPLATE);
            }
            log.info("About to save Registered Template {} ({}), nifi template Id of {} ", registeredTemplate.getTemplateName(), registeredTemplate.getId(),
                     registeredTemplate.getNifiTemplateId());
            ensureRegisteredTemplateInputProcessors(registeredTemplate);

            FeedManagerTemplate domain = templateModelTransform.REGISTERED_TEMPLATE_TO_DOMAIN.apply(registeredTemplate);
            ensureNifiTemplateId(domain);
            if (domain != null) {
                log.info("Domain Object is {} ({}), nifi template Id of {}", domain.getName(), domain.getId(), domain.getNifiTemplateId());
            }
            domain = templateProvider.update(domain);
            //query it back to display to the ui
            domain = templateProvider.findById(domain.getId());
            RegisteredTemplate updatedTemplate = templateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(domain);
            updatedTemplate.setUpdated(true);

            registeredTemplateCache.invalidateProcessors(updatedTemplate.getId());
            niFiTemplateCache.updateSelectedProperties(registeredTemplate);
            return updatedTemplate;
        }
    }


    /**
     * Return the processors in RegisteredTemplate that are input processors ( processors without any incoming connections).
     * This will call out to NiFi to inspect and obtain the NiFi template if it doesn't exist on the registeredTemplate
     *
     * @param registeredTemplate the template to inspect
     * @return the processors in RegisteredTemplate that are input processors without any incoming connections
     */
    public List<RegisteredTemplate.Processor> getInputProcessorsInNifTemplate(RegisteredTemplate registeredTemplate) {
        TemplateDTO nifiTemplate = registeredTemplate.getNifiTemplate();
        if (nifiTemplate == null) {
            nifiTemplate = ensureNifiTemplate(registeredTemplate);
        }
        return getInputProcessorsInNifTemplate(nifiTemplate);
    }

    /**
     * Return the input processors (processors without any incoming connections) in a NiFi template object
     *
     * @param nifiTemplate the NiFi template
     * @return the input processors (processors without any incoming connections) in a NiFi template object
     */
    public List<RegisteredTemplate.Processor> getInputProcessorsInNifTemplate(TemplateDTO nifiTemplate) {
        List<RegisteredTemplate.Processor> processors = new ArrayList<>();
        if (nifiTemplate != null) {
            List<ProcessorDTO> inputProcessors = NifiTemplateUtil.getInputProcessorsForTemplate(nifiTemplate);
            if (inputProcessors != null) {
                inputProcessors.stream().forEach(processorDTO -> {
                    RegisteredTemplate.Processor p = registeredTemplateUtil.toRegisteredTemplateProcessor(processorDTO, false);
                    p.setInputProcessor(true);
                    processors.add(p);
                });
            }
        }
        return processors;
    }

    /**
     * pass in the Template Ids in Order
     */
    public void orderTemplates(List<String> orderedTemplateIds, Set<String> exclude) {
        metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedServicesAccessControl.EDIT_TEMPLATES);

            if (orderedTemplateIds != null && !orderedTemplateIds.isEmpty()) {
                IntStream.range(0, orderedTemplateIds.size()).forEach(i -> {
                    String id = orderedTemplateIds.get(i);
                    if (!"NEW".equals(id) && (exclude == null || (exclude != null && !exclude.contains(id)))) {
                        FeedManagerTemplate template = templateProvider.findById(templateProvider.resolveId(id));
                        if (template != null) {
                            if (template.getOrder() == null || !template.getOrder().equals(new Long(i))) {
                                //save the new order
                                template.setOrder(new Long(i));
                                templateProvider.update(template);
                            }
                        }
                    }
                });
            }
        }, MetadataAccess.ADMIN);


    }

    /**
     * Return the NiFi template id for the incoming template name
     *
     * @param templateName the name of the template
     * @return the NiFi template id for the incoming template name, null if not found
     */
    public String nifiTemplateIdForTemplateName(String templateName) {

        TemplateDTO templateDTO = null;
        templateDTO = nifiRestClient.getTemplateByName(templateName);

        if (templateDTO != null) {
            return templateDTO.getId();
        }
        return null;
    }


}
