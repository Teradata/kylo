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

import com.google.common.collect.Sets;
import com.thinkbiganalytics.feedmgr.nifi.NifiFlowCache;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.PortDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

/**
 */
public class DefaultFeedManagerTemplateService extends AbstractFeedManagerTemplateService implements FeedManagerTemplateService {

    private static final Logger log = LoggerFactory.getLogger(DefaultFeedManagerTemplateService.class);

    @Inject
    FeedManagerTemplateProvider templateProvider;

    @Inject
    TemplateModelTransform templateModelTransform;

    @Inject
    MetadataAccess metadataAccess;

    @Inject
    NifiFlowCache nifiFlowCache;

    @Inject
    private AccessController accessController;

    @Override
    //@Transactional(transactionManager = "metadataTransactionManager")
    protected RegisteredTemplate saveRegisteredTemplate(final RegisteredTemplate registeredTemplate) {
        List<String> templateOrder = registeredTemplate.getTemplateOrder();
        RegisteredTemplate savedTemplate = metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_TEMPLATES);

            //ensure that the incoming template name doesnt already exist.
            //if so remove and replace with this one
            RegisteredTemplate template = getRegisteredTemplateByName(registeredTemplate.getTemplateName());
            if (registeredTemplate.getId() == null && template != null) {
                registeredTemplate.setId(template.getId());
            }
            if (template != null && !template.getId().equalsIgnoreCase(registeredTemplate.getId())) {
                //Warning cant save.. duplicate Name
                log.error("Unable to save template {}.  There is already a template with this name registered in the system", registeredTemplate.getTemplateName());
                return null;
            } else {
                log.info("About to save Registered Template {} ({}), nifi template Id of {} ", registeredTemplate.getTemplateName(), registeredTemplate.getId(),
                         registeredTemplate.getNifiTemplateId());
                ensureRegisteredTemplateInputProcessors(registeredTemplate);

                FeedManagerTemplate domain = templateModelTransform.REGISTERED_TEMPLATE_TO_DOMAIN.apply(registeredTemplate);
                ensureNifiTemplateId(domain);
                log.info("Domain Object is {} ({}), nifi template Id of {}", domain.getName(), domain.getId(), domain.getNifiTemplateId());
                domain = templateProvider.update(domain);
                //query it back to display to the ui
                domain = templateProvider.findById(domain.getId());
                return templateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(domain);
            }
        });

        if (StringUtils.isBlank(registeredTemplate.getId())) {
            templateOrder = templateOrder.stream().map(template -> {
                if ("NEW".equals(template)) {
                    return savedTemplate.getId();
                } else {
                    return template;
                }
            }).collect(Collectors.toList());
        }

        //order it
        orderTemplates(templateOrder, Sets.newHashSet(savedTemplate.getId()));

        return savedTemplate;

    }

    /**
     * pass in the Template Ids in Order
     */
    public void orderTemplates(List<String> orderedTemplateIds, Set<String> exclude) {
        metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_TEMPLATES);

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
        });


    }


    @Override
    public List<RegisteredTemplate.Processor> getRegisteredTemplateProcessors(String templateId, boolean includeReusableProcessors) {
        List<RegisteredTemplate.Processor> processorProperties = new ArrayList<>();

        RegisteredTemplate template = getRegisteredTemplateWithAllProperties(templateId, null);
        if (template != null) {
            template.initializeProcessors();
            processorProperties.addAll(template.getInputProcessors());
            processorProperties.addAll(template.getNonInputProcessors());
        }
        if (includeReusableProcessors && template.getReusableTemplateConnections() != null && !template.getReusableTemplateConnections().isEmpty()) {

            //1 fetch ports in reusable templates
            Map<String, PortDTO> reusableTemplateInputPorts = new HashMap<>();
            Set<PortDTO> ports = getReusableFeedInputPorts();
            if (ports != null) {
                ports.stream().forEach(portDTO -> reusableTemplateInputPorts.put(portDTO.getName(), portDTO));
            }

            //match to the name
            List<String>
                matchingPortIds =
                template.getReusableTemplateConnections().stream().filter(conn -> reusableTemplateInputPorts.containsKey(conn.getReusableTemplateInputPortName()))
                    .map(reusableTemplateConnectionInfo -> reusableTemplateInputPorts.get(reusableTemplateConnectionInfo.getReusableTemplateInputPortName()).getId()).collect(Collectors.toList());

            List<RegisteredTemplate.Processor> reusableProcessors = getReusableTemplateProcessorsForInputPorts(matchingPortIds);
            processorProperties.addAll(reusableProcessors);
        }
        return processorProperties;
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

    @Override
    //@Transactional(transactionManager = "metadataTransactionManager")
    public RegisteredTemplate registerTemplate(RegisteredTemplate registeredTemplate) {
        RegisteredTemplate template = saveRegisteredTemplate(registeredTemplate);
        nifiFlowCache.updateRegisteredTemplate(template);
        return template;
    }


    @Override
    public RegisteredTemplate getRegisteredTemplate(final String templateId) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_TEMPLATES);

            RegisteredTemplate registeredTemplate = null;
            FeedManagerTemplate.ID domainId = templateProvider.resolveId(templateId);
            FeedManagerTemplate domainTemplate = templateProvider.findById(domainId);
            if (domainTemplate != null) {
                //transform it
                registeredTemplate = templateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(domainTemplate);
            }
            if (registeredTemplate != null) {
                registeredTemplate.initializeProcessors();
                ensureNifiTemplate(registeredTemplate);
            }

            return registeredTemplate;
        });

    }

    public boolean deleteRegisteredTemplate(final String templateId) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_TEMPLATES);

            FeedManagerTemplate.ID domainId = templateProvider.resolveId(templateId);
            return templateProvider.deleteTemplate(domainId);
        });

    }

    @Override
    public RegisteredTemplate getRegisteredTemplateByName(final String templateName) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_TEMPLATES);

            RegisteredTemplate registeredTemplate = null;
            FeedManagerTemplate template = templateProvider.findByName(templateName);
            if (template != null) {
                //transform it
                registeredTemplate = templateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(template);
            }
            return registeredTemplate;
        });

    }

    @Override
    public RegisteredTemplate getRegisteredTemplateForNifiProperties(final String nifiTemplateId, final String nifiTemplateName) {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_TEMPLATES);

            RegisteredTemplate registeredTemplate = null;
            FeedManagerTemplate template = templateProvider.findByNifiTemplateId(nifiTemplateId);
            if (template == null) {
                template = templateProvider.findByName(nifiTemplateName);
            }
            if (template != null) {
                registeredTemplate = templateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(template);
            }
            return registeredTemplate;
        });


    }

    public FeedManagerTemplate ensureNifiTemplateId(FeedManagerTemplate feedManagerTemplate) {
        if (feedManagerTemplate.getNifiTemplateId() == null) {
            String nifiTemplateId = nifiTemplateIdForTemplateName(feedManagerTemplate.getName());
            feedManagerTemplate.setNifiTemplateId(nifiTemplateId);
        }
        return feedManagerTemplate;
    }

    @Override
    public List<RegisteredTemplate> getRegisteredTemplates() {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_TEMPLATES);

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

    @Override
    public RegisteredTemplate enableTemplate(String templateId) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_TEMPLATES);
            FeedManagerTemplate.ID domainId = templateProvider.resolveId(templateId);
            if (domainId != null) {
                FeedManagerTemplate template = templateProvider.enable(domainId);
                if (template != null) {
                    return templateModelTransform.domainToRegisteredTemplate(template);
                }
            }
            return null;
        });
    }

    @Override
    public RegisteredTemplate disableTemplate(String templateId) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_TEMPLATES);
            FeedManagerTemplate.ID domainId = templateProvider.resolveId(templateId);
            if (domainId != null) {
                FeedManagerTemplate template = templateProvider.disable(domainId);
                if (template != null) {
                    return templateModelTransform.domainToRegisteredTemplate(template);
                }
            }
            return null;
        });
    }


}
