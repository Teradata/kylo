package com.thinkbiganalytics.feedmgr.service.template;

import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by sr186054 on 5/4/16.
 */
public class JpaFeedManagerTemplateService extends AbstractFeedManagerTemplateService implements FeedManagerTemplateService {

    private static final Logger log = LoggerFactory.getLogger(JpaFeedManagerTemplateService.class);

    @Inject
    FeedManagerTemplateProvider templateProvider;


    @Override
    @Transactional(transactionManager = "metadataTransactionManager")
    protected RegisteredTemplate saveRegisteredTemplate(RegisteredTemplate registeredTemplate) {
        //ensure that the incoming template name doesnt already exist.
        //if so remove and replace with this one
        RegisteredTemplate template = getRegisteredTemplateByName(registeredTemplate.getTemplateName());
        if(template != null && !template.getId().equalsIgnoreCase(registeredTemplate.getId())){
            //Warning cant save.. duplicate Name
            log.error("Unable to save template {}.  There is already a template with this name registered in the system",registeredTemplate.getTemplateName());
            return null;
        }
        else {
            log.info("About to save Registered Template {} ({}), nifi template Id of {} ",registeredTemplate.getTemplateName(),registeredTemplate.getId(),registeredTemplate.getNifiTemplateId());
            FeedManagerTemplate domain = TemplateModelTransform.REGISTERED_TEMPLATE_TO_DOMAIN.apply(registeredTemplate);
            log.info("Domain Object is {} ({}), nifi template Id of {}",domain.getName(),domain.getId(),domain.getNifiTemplateId());
            domain = templateProvider.update(domain);
            //query it back to display to the ui
            domain = templateProvider.findById(domain.getId());
            return TemplateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(domain);
        }
    }

    @Override
    @Transactional(transactionManager = "metadataTransactionManager")
    public void registerTemplate(RegisteredTemplate registeredTemplate) {
        saveRegisteredTemplate(registeredTemplate);
    }



    @Override
    public RegisteredTemplate getRegisteredTemplate(String templateId) {
        RegisteredTemplate registeredTemplate = null;
        FeedManagerTemplate.ID domainId = templateProvider.resolveId(templateId);
        FeedManagerTemplate domainTemplate = templateProvider.findById(domainId);
        if(domainTemplate != null) {
            //transform it
            registeredTemplate = TemplateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(domainTemplate);
        }
        return registeredTemplate;
    }

    public void deleteRegisteredTemplate(String templateId)
    {
        FeedManagerTemplate.ID domainId = templateProvider.resolveId(templateId);
        FeedManagerTemplate domainTemplate = templateProvider.findById(domainId);
        //only allow deletion if there are no feeds
        if(domainTemplate != null && (domainTemplate.getFeeds() == null || domainTemplate.getFeeds().size() ==0)) {
            templateProvider.deleteById(domainId);
        }
    }
    @Override
    public RegisteredTemplate getRegisteredTemplateByName(String templateName) {
        RegisteredTemplate registeredTemplate = null;
        FeedManagerTemplate template = templateProvider.findByName(templateName);
        if(template != null) {
            //transform it
             registeredTemplate = TemplateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(template);
        }
        return registeredTemplate;
    }

    @Override
    public RegisteredTemplate getRegisteredTemplateForNifiProperties(String nifiTemplateId, String nifiTemplateName) {
        RegisteredTemplate registeredTemplate =null;
        FeedManagerTemplate template = templateProvider.findByNifiTemplateId(nifiTemplateId);
        if(template == null){
            template = templateProvider.findByName(nifiTemplateName);
        }
        if(template != null){
            registeredTemplate = TemplateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(template);
        }
        return registeredTemplate;

    }

    @Override
    public List<RegisteredTemplate> getRegisteredTemplates() {
        List<RegisteredTemplate> registeredTemplates = null;
        List<FeedManagerTemplate> templates = templateProvider.findAll();
        if(templates != null) {
            registeredTemplates = TemplateModelTransform.domainToRegisteredTemplate(templates);
        }
        return registeredTemplates;
    }




}
