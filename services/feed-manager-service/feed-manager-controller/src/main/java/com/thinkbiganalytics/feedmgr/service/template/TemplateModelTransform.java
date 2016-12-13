package com.thinkbiganalytics.feedmgr.service.template;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by sr186054 on 5/4/16.
 */
public class TemplateModelTransform {

    @Inject
    FeedManagerTemplateProvider templateProvider;


    public final Function<FeedManagerTemplate, RegisteredTemplate>
            DOMAIN_TO_REGISTERED_TEMPLATE =
            new Function<FeedManagerTemplate, RegisteredTemplate>() {
                @Override
                public RegisteredTemplate apply(FeedManagerTemplate domain) {
                    String json = domain.getJson();
                    RegisteredTemplate template = ObjectMapperSerializer.deserialize(json, RegisteredTemplate.class);
                    template.setId(domain.getId().toString());
                    template.setState(domain.getState().name());
                    template.setNifiTemplateId(domain.getNifiTemplateId());
                    List<FeedManagerFeed> feeds = domain.getFeeds();
                    template.setFeedsCount(feeds == null ? 0 : feeds.size());
                    if(domain.getCreatedTime() != null) {
                         template.setCreateDate(domain.getCreatedTime().toDate());
                    }
                    if(domain.getModifiedTime() != null) {
                         template.setUpdateDate(domain.getModifiedTime().toDate());
                    }
                    template.setOrder(domain.getOrder());
                    return template;
                }
            };

    public final Function<RegisteredTemplate, FeedManagerTemplate>
            REGISTERED_TEMPLATE_TO_DOMAIN =
            new Function<RegisteredTemplate,FeedManagerTemplate>() {
                @Override
                public FeedManagerTemplate apply(RegisteredTemplate registeredTemplate) {
                    //resolve the id
                    FeedManagerTemplate.ID domainId = registeredTemplate.getId() != null ? templateProvider.resolveId(registeredTemplate.getId()) : null;
                    FeedManagerTemplate domain = null;
                    if (domainId != null) {
                        domain = templateProvider.findById(domainId);
                    }
                    if (domain == null) {
                        domain = templateProvider.ensureTemplate(registeredTemplate.getTemplateName());
                    }
                    domainId = domain.getId();
                    //clean the order from the template
                    registeredTemplate.setTemplateOrder(null);
                    String json = ObjectMapperSerializer.serialize(registeredTemplate);
                    domain.setNifiTemplateId(registeredTemplate.getNifiTemplateId());
                    domain.setAllowPreconditions(registeredTemplate.isAllowPreconditions());
                    domain.setName(registeredTemplate.getTemplateName());
                    domain.setDataTransformation(registeredTemplate.isDataTransformation());
                    domain.setDefineTable(registeredTemplate.isDefineTable());
                    domain.setIcon(registeredTemplate.getIcon());
                    domain.setIconColor(registeredTemplate.getIconColor());
                    domain.setDescription(registeredTemplate.getDescription());
                    domain.setOrder(registeredTemplate.getOrder());
                    domain.setJson(json);
                    FeedManagerTemplate.State state = FeedManagerTemplate.State.ENABLED;
                    try {
                        if (registeredTemplate.getState() != null) {
                            state = FeedManagerTemplate.State.valueOf(registeredTemplate.getState());
                        }
                    } catch (IllegalArgumentException e) {
                        // make enabled by default
                    }
                    domain.setState(state);

                    //assign the id back to the ui model
                    registeredTemplate.setId(domainId.toString());
                    return domain;
                }
            };

    public List<RegisteredTemplate> domainToRegisteredTemplate(Collection<FeedManagerTemplate> domain) {
        return new ArrayList<>(Collections2.transform(domain, DOMAIN_TO_REGISTERED_TEMPLATE));
    }

    public RegisteredTemplate domainToRegisteredTemplate(FeedManagerTemplate domain) {
        return DOMAIN_TO_REGISTERED_TEMPLATE.apply(domain);
    }

    public List<FeedManagerTemplate> registeredTemplateToDomain(Collection<RegisteredTemplate> registeredTemplates) {
        return new ArrayList<>(Collections2.transform(registeredTemplates, REGISTERED_TEMPLATE_TO_DOMAIN));
    }
}
