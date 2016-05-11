package com.thinkbiganalytics.feedmgr.service.category;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.service.feed.FeedModelTransform;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.jpa.feedmgr.category.JpaFeedManagerCategory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by sr186054 on 5/4/16.
 */
public class CategoryModelTransform {

    public static Function<FeedManagerCategory, FeedCategory>
            DOMAIN_TO_FEED_CATEGORY =
            new Function<FeedManagerCategory, FeedCategory>() {
                @Override
                public FeedCategory apply(FeedManagerCategory domainCategory) {
                    String json = domainCategory.getJson();
                    FeedCategory category = ObjectMapperSerializer.deserialize(json, FeedCategory.class);
                    category.setId(domainCategory.getId().toString());
                    if(domainCategory.getFeeds() != null){
                       List<FeedSummary> summaries = FeedModelTransform.domainToFeedSummary(domainCategory.getFeeds());
                        category.setFeeds(summaries);
                    }
                    category.setIconColor(domainCategory.getIconColor());
                    category.setIcon(domainCategory.getIcon());
                    category.setName(domainCategory.getDisplayName());
                    category.setDescription(domainCategory.getDescription());
                  //  category.setCreateDate(domainCategory.getCreatedTime().toDate());
                  //  category.setUpdateDate(domainCategory.getModifiedTime().toDate());
                    return category;
                }
            };

    public static Function<FeedManagerCategory, FeedCategory>
            DOMAIN_TO_FEED_CATEGORY_SIMPLE =
            new Function<FeedManagerCategory, FeedCategory>() {
                @Override
                public FeedCategory apply(FeedManagerCategory domainCategory) {
                    String json = domainCategory.getJson();
                    //FeedCategory category = ObjectMapperSerializer.deserialize(json, FeedCategory.class);
                    FeedCategory category = new FeedCategory();
                    category.setId(domainCategory.getId().toString());
                    category.setIconColor(domainCategory.getIconColor());
                    category.setIcon(domainCategory.getIcon());
                    category.setName(domainCategory.getDisplayName());
                    category.setDescription(domainCategory.getDescription());
                    //  category.setCreateDate(domainCategory.getCreatedTime().toDate());
                    //  category.setUpdateDate(domainCategory.getModifiedTime().toDate());
                    return category;
                }
            };

    public static Function<FeedCategory, FeedManagerCategory>
            FEED_CATEGORY_TO_DOMAIN =
            new Function<FeedCategory,FeedManagerCategory>() {
                @Override
                public FeedManagerCategory apply(FeedCategory feedCategory) {
                    JpaFeedManagerCategory.FeedManagerCategoryId domainId = feedCategory.getId() != null ? new JpaFeedManagerCategory.FeedManagerCategoryId(feedCategory.getId()): JpaFeedManagerCategory.FeedManagerCategoryId.create();
                   if(feedCategory.getId() == null){
                       feedCategory.setId(domainId.toString());
                   }
                    JpaFeedManagerCategory
                            category = new JpaFeedManagerCategory(domainId);
                    category.setDisplayName(feedCategory.getName());
                    category.setSystemName(feedCategory.getSystemName());
                    category.setDescription(feedCategory.getDescription());
                    category.setIcon(feedCategory.getIcon());
                    category.setIconColor(feedCategory.getIconColor());
                    category.setJson(ObjectMapperSerializer.serialize(feedCategory));
                    return category;
                }
            };

    public static List<FeedCategory> domainToFeedCategory(Collection<FeedManagerCategory> domain) {
        return new ArrayList<>(Collections2.transform(domain, DOMAIN_TO_FEED_CATEGORY));
    }

    public static List<FeedCategory> domainToFeedCategorySimple(Collection<FeedManagerCategory> domain) {
        return new ArrayList<>(Collections2.transform(domain, DOMAIN_TO_FEED_CATEGORY_SIMPLE));
    }

    public static List<FeedManagerCategory> feedCategoryToDomain(Collection<FeedCategory> feedCategories) {
        return new ArrayList<>(Collections2.transform(feedCategories, FEED_CATEGORY_TO_DOMAIN));
    }
}
