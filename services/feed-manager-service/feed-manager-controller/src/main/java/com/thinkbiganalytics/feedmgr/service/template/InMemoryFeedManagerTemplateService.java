package com.thinkbiganalytics.feedmgr.service.template;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.service.FileObjectPersistence;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by sr186054 on 5/1/16.
 */
public class InMemoryFeedManagerTemplateService extends AbstractFeedManagerTemplateService implements FeedManagerTemplateService {


  @Inject
  private FeedManagerFeedService feedProvider;

  private Map<String, RegisteredTemplate> registeredTemplates = new HashMap<>();


  @PostConstruct
  private void postConstruct(){
    Collection<RegisteredTemplate> templates = FileObjectPersistence.getInstance().getTemplatesFromFile();
    if(templates != null){
      for(RegisteredTemplate t : templates){
        String id = t.getId() == null ? t.getNifiTemplateId() : t.getId();
        registeredTemplates.put(id, t);
      }
    }
  }





  @Override
  public void registerTemplate(RegisteredTemplate registeredTemplate) {
    Date updateDate = new Date();

    if (registeredTemplate.getId() == null || !registeredTemplates.containsKey(registeredTemplate.getId())) {
      registeredTemplate.setCreateDate(updateDate);
    }
    registeredTemplate.setUpdateDate(updateDate);
    if(registeredTemplate.getId() == null){
      registeredTemplate.setId(UUID.randomUUID().toString());

    }
    saveRegisteredTemplate(registeredTemplate);

  }

  protected RegisteredTemplate saveRegisteredTemplate(RegisteredTemplate registeredTemplate){
    //ensure that the incoming template name doesnt already exist.
    //if so remove and replace with this one
    RegisteredTemplate template = getRegisteredTemplateByName(registeredTemplate.getTemplateName());
    if(template != null && !template.getId().equalsIgnoreCase(registeredTemplate.getId())){
      //remove the old one with the same name
      registeredTemplates.remove(template.getId());
      //update those feeds that were pointing to this old one, to this new one



      feedProvider.updateFeedsWithTemplate(template.getId(), registeredTemplate.getId());
      }

    registeredTemplates.put(registeredTemplate.getId(), registeredTemplate);
    if(registeredTemplates.containsKey(registeredTemplate.getNifiTemplateId())){
      registeredTemplates.remove(registeredTemplate.getNifiTemplateId());
    }

    FileObjectPersistence.getInstance().writeTemplatesToFile(registeredTemplates.values());

    return registeredTemplate;
  }



  @Override
  public RegisteredTemplate getRegisteredTemplate(String templateId) {
    RegisteredTemplate savedTemplate = registeredTemplates.get(templateId);
    if(savedTemplate != null) {
      return new RegisteredTemplate(savedTemplate);
    }
    return null;
  }

  @Override
  public RegisteredTemplate getRegisteredTemplateByName(final String templateName) {

    return Iterables.tryFind(registeredTemplates.values(), new Predicate<RegisteredTemplate>() {
      @Override
      public boolean apply(RegisteredTemplate registeredTemplate) {
        return registeredTemplate.getTemplateName().equalsIgnoreCase(templateName);
      }
    }).orNull();
  }



  @Override
  public RegisteredTemplate getRegisteredTemplateForNifiProperties(final String nifiTemplateId, final String nifiTemplateName) {
    RegisteredTemplate match = Iterables.tryFind(registeredTemplates.values(), new Predicate<RegisteredTemplate>() {
      @Override
      public boolean apply(RegisteredTemplate registeredTemplate) {
        boolean match = nifiTemplateId.equalsIgnoreCase(registeredTemplate.getNifiTemplateId());
        if (!match && nifiTemplateName != null) {
          match = nifiTemplateName.equalsIgnoreCase(registeredTemplate.getTemplateName());
        }
        return match;
      }
    }).orNull();
    return match;
  }




  public List<String> getRegisteredTemplateIds() {
    return new ArrayList<>(registeredTemplates.keySet());
  }

  @Override
  public List<RegisteredTemplate> getRegisteredTemplates() {
    return new ArrayList<>(registeredTemplates.values());
  }

}
