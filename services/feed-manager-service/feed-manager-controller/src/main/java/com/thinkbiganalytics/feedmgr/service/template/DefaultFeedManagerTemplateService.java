package com.thinkbiganalytics.feedmgr.service.template;

import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.security.FeedsAccessControl;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.security.AccessController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Created by sr186054 on 5/4/16.
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
    private AccessController accessController;

    @Override
    //@Transactional(transactionManager = "metadataTransactionManager")
    protected RegisteredTemplate saveRegisteredTemplate(final RegisteredTemplate registeredTemplate) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.EDIT_TEMPLATES);

            //ensure that the incoming template name doesnt already exist.
            //if so remove and replace with this one
            RegisteredTemplate template = getRegisteredTemplateByName(registeredTemplate.getTemplateName());
            if(registeredTemplate.getId() == null && template != null){
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
                log.info("Domain Object is {} ({}), nifi template Id of {}", domain.getName(), domain.getId(), domain.getNifiTemplateId());
                domain = templateProvider.update(domain);
                //query it back to display to the ui
                domain = templateProvider.findById(domain.getId());
                return templateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(domain);
            }
        });

    }

    /**
     * Ensures that the {@code RegisteredTemplate#inputProcessors} list is populated not only with the processors which were defined as having user inputs, but also those that done require any input
     */
    public void ensureRegisteredTemplateInputProcessors(RegisteredTemplate registeredTemplate) {
        registeredTemplate.initializeInputProcessors();
        List<RegisteredTemplate.Processor> nifiProcessors = getInputProcessorsInNifTemplate(registeredTemplate.getNifiTemplateId());

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
        return saveRegisteredTemplate(registeredTemplate);
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

    @Override
    public List<RegisteredTemplate> getRegisteredTemplates() {
        return metadataAccess.read(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_TEMPLATES);

            List<RegisteredTemplate> registeredTemplates = null;
            List<FeedManagerTemplate> templates = templateProvider.findAll();
            if (templates != null) {
                registeredTemplates = templateModelTransform.domainToRegisteredTemplate(templates);
            }
            return registeredTemplates;
        });

    }

    @Override
    public RegisteredTemplate enableTemplate(String templateId) {
        return metadataAccess.commit(() -> {
            this.accessController.checkPermission(AccessController.SERVICES, FeedsAccessControl.ACCESS_TEMPLATES);
            FeedManagerTemplate.ID domainId = templateProvider.resolveId(templateId);
            if(domainId != null){
                FeedManagerTemplate template =   templateProvider.enable(domainId);
                if(template != null){
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
            if(domainId != null){
                FeedManagerTemplate template =   templateProvider.disable(domainId);
                if(template != null){
                    return templateModelTransform.domainToRegisteredTemplate(template);
                }
            }
            return null;
        });
    }
}
