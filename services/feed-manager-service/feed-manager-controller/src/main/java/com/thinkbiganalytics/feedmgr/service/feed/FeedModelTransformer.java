package com.thinkbiganalytics.feedmgr.service.feed;

import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.service.category.CategoryModelTransform;
import com.thinkbiganalytics.feedmgr.service.feed.datasource.NifiFeedDatasourceFactory;
import com.thinkbiganalytics.feedmgr.service.template.TemplateModelTransform;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.jpa.feed.JpaFeed;
import com.thinkbiganalytics.metadata.jpa.feedmgr.feed.JpaFeedManagerFeed;
import com.thinkbiganalytics.metadata.jpa.feedmgr.template.JpaFeedManagerTemplate;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;

/**
 * Created by sr186054 on 5/11/16.
 */
public class FeedModelTransformer  {


    @Inject
    private FeedProvider feedProvider;

    @Inject
    FeedManagerTemplateProvider templateProvider;

    @Inject
    private FeedManagerFeedProvider feedManagerFeedProvider;

    public FeedManagerFeed feedToDomain(FeedMetadata feedMetadata){
        //resolve the id
        boolean isNew = feedMetadata.getId() == null;
        JpaFeed.FeedId domainId = feedMetadata.getId() != null ? new JpaFeed.FeedId(feedMetadata.getId()): JpaFeed.FeedId.create();
        JpaFeedManagerFeed domain = null;
        domain = (JpaFeedManagerFeed) feedManagerFeedProvider.findById(domainId);
        if(domain == null){
            isNew = true;
            domain = new JpaFeedManagerFeed(domainId,feedMetadata.getSystemFeedName(),feedMetadata.getDescription());
            domain.setState(Feed.State.ENABLED);
            feedMetadata.setId(domainId.toString());
            feedMetadata.setFeedId(domainId.toString());
            feedMetadata.setState(Feed.State.ENABLED.name());
        }
        domain.setDisplayName(feedMetadata.getFeedName());
        domain.setDescription(feedMetadata.getDescription());

        feedMetadata.setId(domain.getId().toString());

        FeedCategory category = feedMetadata.getCategory();
        if(category != null){
            FeedManagerCategory domainCategory = CategoryModelTransform.FEED_CATEGORY_TO_DOMAIN.apply(category);
            domain.setCategory(domainCategory);
        }
        RegisteredTemplate template = feedMetadata.getRegisteredTemplate();
        if(template != null){
            //TODO is this needed, or should it just be looked up and assigned
            FeedManagerTemplate domainTemplate = TemplateModelTransform.REGISTERED_TEMPLATE_TO_DOMAIN.apply(template);
            domain.setTemplate(domainTemplate);
        }
        if(StringUtils.isNotBlank(feedMetadata.getState())) {
            Feed.State state = Feed.State.valueOf(feedMetadata.getState().toUpperCase());
            domain.setState(state);
        }

        domain.setJson(ObjectMapperSerializer.serialize(feedMetadata));
        if(feedMetadata.getVersion() == null){
            feedMetadata.setVersion(1L);
        }

        //Datasource datasource = NifiFeedDatasourceFactory.transformSources(feedMetadata);


        if(domain.getTemplate() == null){
            FeedManagerTemplate.ID templateId = new JpaFeedManagerTemplate.FeedManagerTemplateId(feedMetadata.getTemplateId());
            FeedManagerTemplate domainTemplate = templateProvider.findById(templateId);
            domain.setTemplate(domainTemplate);
        }

        domain.setVersion(feedMetadata.getVersion().intValue());
        return domain;


    }


}
