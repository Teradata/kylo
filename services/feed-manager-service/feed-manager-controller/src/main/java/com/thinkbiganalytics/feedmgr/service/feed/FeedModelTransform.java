package com.thinkbiganalytics.feedmgr.service.feed;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.service.category.CategoryModelTransform;
import com.thinkbiganalytics.feedmgr.service.template.TemplateModelTransform;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by sr186054 on 5/4/16.
 */
public class FeedModelTransform {


    public static final Function<FeedManagerFeed, FeedMetadata>
        DOMAIN_TO_FEED =
        new Function<FeedManagerFeed, FeedMetadata>() {
            @Override
            public FeedMetadata apply(FeedManagerFeed domain) {
                String json = domain.getJson();
                FeedMetadata feed = ObjectMapperSerializer.deserialize(json, FeedMetadata.class);
                feed.setId(domain.getId().toString());
                feed.setFeedId(domain.getId().toString());
                feed.setTemplateId(domain.getTemplate().getId().toString());
                if (domain.getCreatedTime() != null) {
                    feed.setCreateDate(domain.getCreatedTime().toDate());
                }
                if (domain.getModifiedTime() != null) {
                    feed.setUpdateDate(domain.getModifiedTime().toDate());
                }

                FeedManagerTemplate template = domain.getTemplate();
                if (template != null) {
                    RegisteredTemplate registeredTemplate = TemplateModelTransform.DOMAIN_TO_REGISTERED_TEMPLATE.apply(template);
                    feed.setRegisteredTemplate(registeredTemplate);
                    feed.setTemplateId(registeredTemplate.getId());
                }
                FeedManagerCategory category = domain.getCategory();
                if (category != null) {
                    FeedCategory feedCategory = CategoryModelTransform.DOMAIN_TO_FEED_CATEGORY_SIMPLE.apply(category);
                    feed.setCategory(feedCategory);
                }
                feed.setState(domain.getState() != null ? domain.getState().name() : null);
                feed.setVersion(domain.getVersion().longValue());
                return feed;
            }
        };

    public static final Function<Feed, FeedSummary> DOMAIN_TO_FEED_SUMMARY = new Function<Feed, FeedSummary>() {
        @Nullable
        @Override
        public FeedSummary apply(@Nullable Feed feedManagerFeed) {
            FeedSummary feedSummary = new FeedSummary();
            feedSummary.setId(feedManagerFeed.getId().toString());
            feedSummary.setFeedId(feedManagerFeed.getId().toString());
            feedSummary.setCategoryId(feedManagerFeed.getCategory().getId().toString());
            feedSummary.setCategoryIcon(((FeedManagerCategory) feedManagerFeed.getCategory()).getIcon());
            feedSummary.setCategoryIconColor(((FeedManagerCategory) feedManagerFeed.getCategory()).getIconColor());
            feedSummary.setCategoryName(feedManagerFeed.getCategory().getDisplayName());
            feedSummary.setSystemCategoryName(feedManagerFeed.getCategory().getName());
            feedSummary.setUpdateDate(feedManagerFeed.getModifiedTime().toDate());
            feedSummary.setFeedName(feedManagerFeed.getDisplayName());
            feedSummary.setSystemFeedName(feedManagerFeed.getName());
            feedSummary.setActive(feedManagerFeed.getState() != null ? feedManagerFeed.getState().equals(Feed.State.ENABLED) : false);
            return feedSummary;
        }
    };


    public static List<FeedMetadata> domainToFeedMetadata(Collection<FeedManagerFeed> domain) {
        return new ArrayList<>(Collections2.transform(domain, DOMAIN_TO_FEED));
    }


    public static List<FeedSummary> domainToFeedSummary(Collection<Feed> domain) {
        return new ArrayList<>(Collections2.transform(domain, DOMAIN_TO_FEED_SUMMARY));
    }


}
