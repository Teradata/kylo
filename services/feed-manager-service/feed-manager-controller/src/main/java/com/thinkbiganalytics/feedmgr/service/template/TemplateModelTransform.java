package com.thinkbiganalytics.feedmgr.service.template;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.jpa.feedmgr.template.JpaFeedManagerTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 5/4/16.
 */
public class TemplateModelTransform {



    public static final Function<FeedManagerTemplate, RegisteredTemplate>
            DOMAIN_TO_REGISTERED_TEMPLATE =
            new Function<FeedManagerTemplate, RegisteredTemplate>() {
                @Override
                public RegisteredTemplate apply(FeedManagerTemplate domain) {
                    String json = domain.getJson();
                    RegisteredTemplate template = ObjectMapperSerializer.deserialize(json, RegisteredTemplate.class);
                    template.setId(domain.getId().toString());
                    if(domain.getCreatedTime() != null) {
                         template.setCreateDate(domain.getCreatedTime().toDate());
                    }
                    if(domain.getModifiedTime() != null) {
                         template.setUpdateDate(domain.getModifiedTime().toDate());
                    }
                    return template;
                }
            };

    public static final Function<RegisteredTemplate, FeedManagerTemplate>
            REGISTERED_TEMPLATE_TO_DOMAIN =
            new Function<RegisteredTemplate,FeedManagerTemplate>() {
                @Override
                public FeedManagerTemplate apply(RegisteredTemplate registeredTemplate) {
                    //resolve the id
                    JpaFeedManagerTemplate.FeedManagerTemplateId domainId = registeredTemplate.getId() != null ? new JpaFeedManagerTemplate.FeedManagerTemplateId(registeredTemplate.getId()): JpaFeedManagerTemplate.FeedManagerTemplateId.create();
                    JpaFeedManagerTemplate
                            domain = new JpaFeedManagerTemplate(domainId);
                    registeredTemplate.setId(domainId.toString());
                    String json = ObjectMapperSerializer.serialize(registeredTemplate);
                    domain.setNifiTemplateId(registeredTemplate.getNifiTemplateId());
                    domain.setAllowPreconditions(registeredTemplate.isAllowPreconditions());
                    domain.setName(registeredTemplate.getTemplateName());
                    domain.setDataTransformation(registeredTemplate.isDataTransformation());
                    domain.setDefineTable(registeredTemplate.isDefineTable());
                    domain.setIcon(registeredTemplate.getIcon());
                    domain.setIconColor(registeredTemplate.getIconColor());
                    domain.setDescription(registeredTemplate.getDescription());
                    domain.setJson(json);
                    return domain;
                }
            };

    public static List<RegisteredTemplate> domainToRegisteredTemplate(Collection<FeedManagerTemplate> domain) {
        return new ArrayList<>(Collections2.transform(domain, DOMAIN_TO_REGISTERED_TEMPLATE));
    }

    public static List<FeedManagerTemplate> registeredTemplateToDomain(Collection<RegisteredTemplate> registeredTemplates) {
        return new ArrayList<>(Collections2.transform(registeredTemplates, REGISTERED_TEMPLATE_TO_DOMAIN));
    }
}
