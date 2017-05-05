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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.ReusableTemplateConnectionInfo;
import com.thinkbiganalytics.feedmgr.service.FileObjectPersistence;
import com.thinkbiganalytics.feedmgr.service.feed.FeedManagerFeedService;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;

import org.apache.nifi.web.api.dto.PortDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * in memory implementation of the FeedTemplateService for use in testing
 */
public class InMemoryFeedManagerTemplateService implements FeedManagerTemplateService {


    @Inject
    private FeedManagerFeedService feedProvider;

    private Map<String, RegisteredTemplate> registeredTemplates = new HashMap<>();


    @PostConstruct
    private void postConstruct() {
        Collection<RegisteredTemplate> templates = FileObjectPersistence.getInstance().getTemplatesFromFile();
        if (templates != null) {
            for (RegisteredTemplate t : templates) {
                String id = t.getId() == null ? t.getNifiTemplateId() : t.getId();
                registeredTemplates.put(id, t);
            }
        }
    }


    @Override
    public RegisteredTemplate registerTemplate(RegisteredTemplate registeredTemplate) {
        Date updateDate = new Date();

        if (registeredTemplate.getId() == null || !registeredTemplates.containsKey(registeredTemplate.getId())) {
            registeredTemplate.setCreateDate(updateDate);
        }
        registeredTemplate.setUpdateDate(updateDate);
        if (registeredTemplate.getId() == null) {
            registeredTemplate.setId(UUID.randomUUID().toString());

        }
        return saveRegisteredTemplate(registeredTemplate);
    }

    @Override
    public void ensureRegisteredTemplateInputProcessors(RegisteredTemplate registeredTemplate) {

    }

    protected RegisteredTemplate saveRegisteredTemplate(RegisteredTemplate registeredTemplate) {
        //ensure that the incoming template name doesnt already exist.
        //if so remove and replace with this one
        RegisteredTemplate template = getRegisteredTemplateByName(registeredTemplate.getTemplateName());
        if (template != null && !template.getId().equalsIgnoreCase(registeredTemplate.getId())) {
            //remove the old one with the same name
            registeredTemplates.remove(template.getId());
            //update those feeds that were pointing to this old one, to this new one

            List<FeedMetadata> feedsToUpdate = feedProvider.getFeedsWithTemplate(registeredTemplate.getId());
            if (feedsToUpdate != null && !feedsToUpdate.isEmpty()) {
                for (FeedMetadata feedMetadata : feedsToUpdate) {
                    feedMetadata.setTemplateId(template.getId());
                }
                //save the feeds
                FileObjectPersistence.getInstance().writeFeedsToFile(feedProvider.getFeeds());
            }
        }

        registeredTemplates.put(registeredTemplate.getId(), registeredTemplate);
        if (registeredTemplates.containsKey(registeredTemplate.getNifiTemplateId())) {
            registeredTemplates.remove(registeredTemplate.getNifiTemplateId());
        }

        FileObjectPersistence.getInstance().writeTemplatesToFile(registeredTemplates.values());

        return registeredTemplate;
    }

    public boolean deleteRegisteredTemplate(String templateId) {
        throw new UnsupportedOperationException("unable to delete the template");
    }


    @Override
    public RegisteredTemplate getRegisteredTemplate(String templateId) {
        RegisteredTemplate savedTemplate = registeredTemplates.get(templateId);
        if (savedTemplate != null) {
            return new RegisteredTemplate(savedTemplate);
        }
        return null;
    }

    public RegisteredTemplate getRegisteredTemplateByName(final String templateName) {

        return Iterables.tryFind(registeredTemplates.values(), new Predicate<RegisteredTemplate>() {
            @Override
            public boolean apply(RegisteredTemplate registeredTemplate) {
                return registeredTemplate.getTemplateName().equalsIgnoreCase(templateName);
            }
        }).orNull();
    }




    public List<String> getRegisteredTemplateIds() {
        return new ArrayList<>(registeredTemplates.keySet());
    }

    @Override
    public List<RegisteredTemplate> getRegisteredTemplates() {
        return new ArrayList<>(registeredTemplates.values());
    }


    @Override
    public RegisteredTemplate enableTemplate(String templateId) {
        return null;
    }

    @Override
    public RegisteredTemplate disableTemplate(String templateId) {
        return null;
    }


    @Override
    public List<RegisteredTemplate.Processor> getRegisteredTemplateProcessors(String templateId, boolean includeReusableProcessors) {
        return null;
    }


    @Override
    public void orderTemplates(List<String> orderedTemplateIds, Set<String> exclude) {

    }

    @Override
    public List<NifiProperty> getTemplateProperties(String templateId) {
        return null;
    }

    @Override
    public Set<PortDTO> getReusableFeedInputPorts() {
        return null;
    }

    @Override
    public List<RegisteredTemplate.Processor> getReusableTemplateProcessorsForInputPorts(List<String> inputPortIds) {
        return null;
    }

    @Override
    public List<RegisteredTemplate.FlowProcessor> getNiFiTemplateFlowProcessors(String templateId, List<ReusableTemplateConnectionInfo> connectionInfo) {
        return null;
    }

    @Override
    public List<RegisteredTemplate.Processor> getNiFiTemplateProcessorsWithProperties(String templateId) {
        return null;
    }

    @Override
    public RegisteredTemplate findRegisteredTemplateByName(String templateName) {
        return null;
    }
}
