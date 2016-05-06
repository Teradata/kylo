package com.thinkbiganalytics.feedmgr.service.template;

import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.rest.JerseyClientException;

import java.util.List;

/**
 * Created by sr186054 on 5/1/16.
 */
public interface FeedManagerTemplateService {

  String templateIdForTemplateName(String templateName);

  void registerTemplate(RegisteredTemplate registeredTemplate);

  List<NifiProperty> getTemplateProperties(String templateId);

  RegisteredTemplate getRegisteredTemplate(String templateId);

  RegisteredTemplate getRegisteredTemplateByName(String templateName);

  RegisteredTemplate getRegisteredTemplateForNifiProperties(String nifiTemplateId, String nifiTemplateName);

  RegisteredTemplate getRegisteredTemplateWithAllProperties(String templateId) throws JerseyClientException;

  List<RegisteredTemplate> getRegisteredTemplates();


}
