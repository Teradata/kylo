package com.thinkbiganalytics.metadata.modeshape.feed;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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
          category = createCategory(categorySystemName);
        }
        return category;
    }

    public Category createCategory(String categorySystemName){
        JcrCategory cat = (JcrCategory) categoryProvider.ensureCategory(categorySystemName);
        cat.setDescription(categorySystemName + " desc");
        cat.setTitle(categorySystemName);
        categoryProvider.update(cat);
        return cat;
    }

    public FeedManagerFeed findOrCreateFeed(String categorySystemName, String feedSystemName, String feedTemplate) {
        Category category = findOrCreateCategory(categorySystemName);
        FeedManagerFeed feed = feedManagerFeedProvider.ensureFeed(category.getId(), feedSystemName);
        feed.setDisplayName(feedSystemName);
        FeedManagerTemplate template = findOrCreateTemplate(feedTemplate);
        feed.setTemplate(template);
        return feedManagerFeedProvider.update(feed);
    }

    public FeedManagerFeed findOrCreateFeed(Category category, String feedSystemName,FeedManagerTemplate template) {
        FeedManagerFeed feed = feedManagerFeedProvider.ensureFeed(category.getId(), feedSystemName);
        feed.setDisplayName(feedSystemName);
        feed.setTemplate(template);
        feed.setJson(sampleFeedJson());
        return feedManagerFeedProvider.update(feed);
    }

    private String sampleFeedJson(){
        return "";
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
