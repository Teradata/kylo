package com.thinkbiganalytics.metadata.modeshape.feed;

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;

import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Created by sr186054 on 11/10/16.
 */
@Component
public class FeedTestUtil {


    @Inject
    CategoryProvider categoryProvider;

    @Inject
    FeedProvider feedProvider;

    @Inject
    FeedManagerFeedProvider feedManagerFeedProvider;

    @Inject
    FeedManagerTemplateProvider feedManagerTemplateProvider;

    @Inject
    FeedManagerCategoryProvider feedManagerCategoryProvider;

    @Inject
    private JcrMetadataAccess metadata;


    /**
     * must be called within metdata.commit()
     */
    public Category findOrCreateCategory(String categorySystemName) {
        Category category = categoryProvider.findBySystemName(categorySystemName);
        if (category == null) {
            JcrCategory cat = (JcrCategory) categoryProvider.ensureCategory(categorySystemName);
            cat.setDescription(categorySystemName + " desc");
            cat.setTitle(categorySystemName);
            categoryProvider.update(cat);
            category = cat;
        }
        return category;
    }

    public FeedManagerFeed findOrCreateFeed(String categorySystemName, String feedSystemName, String feedTemplate) {
        Category category = findOrCreateCategory(categorySystemName);
        FeedManagerFeed feed = feedManagerFeedProvider.ensureFeed(category.getId(), feedSystemName);
        feed.setDisplayName(feedSystemName);
        FeedManagerTemplate template = findOrCreateTemplate(feedTemplate);
        feed.setTemplate(template);
        return feedManagerFeedProvider.update(feed);
    }

    public FeedManagerFeed findFeed(String categorySystemName, String feedSystemName) {
        FeedManagerFeed feed = feedManagerFeedProvider.findBySystemName(categorySystemName, feedSystemName);
        return feed;
    }

    /**
     * returns a FeedManagerTemplate. Must be called within a metadata.commit() call
     */
    public FeedManagerTemplate findOrCreateTemplate(String templateName) {
        FeedManagerTemplate template = feedManagerTemplateProvider.findByName(templateName);
        if (template == null) {
            template = feedManagerTemplateProvider.ensureTemplate(templateName);
            return template;
        } else {
            return template;
        }
    }

}
