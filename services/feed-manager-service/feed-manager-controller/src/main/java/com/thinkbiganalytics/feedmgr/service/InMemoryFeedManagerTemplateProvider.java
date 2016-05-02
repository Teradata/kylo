package com.thinkbiganalytics.feedmgr.service;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.nifi.rest.client.NifiRestClient;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.nifi.rest.support.NifiPropertyUtil;
import com.thinkbiganalytics.rest.JerseyClientException;

import org.apache.nifi.web.api.dto.TemplateDTO;
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
public class InMemoryFeedManagerTemplateProvider implements FeedManagerTemplateProvider {

  @Autowired
  private NifiRestClient nifiRestClient;

  @Inject
  private  FeedManagerFeedProvider feedProvider;

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



  public RegisteredTemplate checkAndSyncTemplateId(String templateIdOrName) throws JerseyClientException {
    TemplateDTO dto = null;
    RegisteredTemplate registeredTemplate = registeredTemplates.get(templateIdOrName);
    String id = "";
    if(registeredTemplate != null){
      String templateId = registeredTemplate.getNifiTemplateId();
      //verify it exists in nifi
      try {
        dto = nifiRestClient.getTemplateById(templateId);
      }catch(JerseyClientException e){

      }
      if(dto == null){
        try {
          dto = nifiRestClient.getTemplateByName(registeredTemplate.getTemplateName());
          if(dto != null){
            //template got changed ... resync
            registeredTemplate.setNifiTemplateId(dto.getId());
            saveRegisteredTemplate(registeredTemplate);
          }
        }
        catch (JerseyClientException e){

        }
      }
      if(dto != null){
        return registeredTemplate;

      }
    }
    if(dto == null){
      throw new JerseyClientException("Unable to find a matching template for Nifi Template Id: "+registeredTemplate.getNifiTemplateId()+" or Name: "+registeredTemplate.getTemplateName());
    }
    return null;
  }



  @Override
  public String templateIdForTemplateName(String templateName) {

    TemplateDTO templateDTO = null;
    try {
      templateDTO = nifiRestClient.getTemplateByName(templateName);
    } catch (JerseyClientException e) {
      e.printStackTrace();
    }
    if(templateDTO != null){
      return templateDTO.getId();
    }
    return null;
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

  private void saveRegisteredTemplate(RegisteredTemplate registeredTemplate){
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


  }

  @Override
  public List<NifiProperty> getTemplateProperties(String templateId) {
    List<NifiProperty> list = new ArrayList<>();
    RegisteredTemplate template = getRegisteredTemplate(templateId);
    if (template != null) {
      list = template.getProperties();
    }
    return list;
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


  private void syncTemplateId(RegisteredTemplate template){
    String nifiTemplateId = templateIdForTemplateName(template.getTemplateName());
    template.setNifiTemplateId(nifiTemplateId);

    RegisteredTemplate t = getRegisteredTemplate(template.getId());
    template.setProperties(t.getProperties());
    saveRegisteredTemplate(template);
  }


  /**
   * Get Registered TEmplate for incoming RegisteredTemplate.id or Nifi Template Id
   * if there is no RegisteredTEmplate matching the incoming id it is assumed to be a new Tempate and it tries to fetch it from Nifi
   */
  @Override
  public RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId) throws JerseyClientException {
    RegisteredTemplate registeredTemplate = getRegisteredTemplate(templateId);
    //if it is null check to see if the template exists in nifi and is already registered
    if(registeredTemplate == null){
      registeredTemplate = getRegisteredTemplateForNifiProperties(templateId,null);
    }
    if (registeredTemplate == null) {
      List<NifiProperty> properties = nifiRestClient.getPropertiesForTemplate(templateId);
      registeredTemplate = new RegisteredTemplate();
      registeredTemplate.setProperties(properties);
      registeredTemplate.setNifiTemplateId(templateId);
      //get the template Name
      TemplateDTO templateDTO = nifiRestClient.getTemplateById(templateId);
      if (templateDTO != null) {
        registeredTemplate.setTemplateName(templateDTO.getName());
      }
    } else {
      List<NifiProperty> properties = null;
      int matchCount = 0;
      try {
        properties = nifiRestClient.getPropertiesForTemplate(registeredTemplate.getNifiTemplateId());
        List<NifiProperty> matchedProperties = NifiPropertyUtil
            .matchAndSetPropertyByIdKey(properties, registeredTemplate.getProperties());
        matchCount = matchedProperties.size();
      }catch(JerseyClientException e) {

      }
      if(properties == null || matchCount == 0) {
        //sync templateId for name


        properties = nifiRestClient.getPropertiesForTemplateByName(registeredTemplate.getTemplateName());
        if(properties != null) {
          //   property = NifiPropertyUtil.findPropertyByProcessorType(properties, "com.thinkbiganalytics.nifi.GetTableData", "Archive Unit");
          NifiPropertyUtil.matchAndSetPropertyByProcessorName(properties, registeredTemplate.getProperties());
          //    registeredTemplate.setProperties(properties);
        }
        syncTemplateId(registeredTemplate);

      }
      if(properties == null){
        //ERROR
        properties = new ArrayList<>();
      }
      //merge with the registered properties

      RegisteredTemplate copy = new RegisteredTemplate(registeredTemplate);
     /*        copy.setId(registeredTemplate.getId());
            copy.setNifiTemplateId(registeredTemplate.getNifiTemplateId());
            copy.setTemplateName(registeredTemplate.getTemplateName());
            copy.setDefineTable(registeredTemplate.isDefineTable());
            copy.setAllowPreconditions(registeredTemplate.isAllowPreconditions());
            copy.setDataTransformation(registeredTemplate.isDataTransformation());
            copy.setIcon(registeredTemplate.getIcon());
            copy.setIconColor(registeredTemplate.getIconColor());
             copy.setDescription(registeredTemplate.getDescription());
               */
      copy.setProperties(properties);

      registeredTemplate = copy;

    }
    return registeredTemplate;
  }

  public List<String> getRegisteredTemplateIds() {
    return new ArrayList<>(registeredTemplates.keySet());
  }

  @Override
  public List<RegisteredTemplate> getRegisteredTemplates() {
    return new ArrayList<>(registeredTemplates.values());
  }

}
