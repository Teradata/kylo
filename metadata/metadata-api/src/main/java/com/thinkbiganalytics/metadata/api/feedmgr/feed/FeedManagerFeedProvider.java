package com.thinkbiganalytics.metadata.api.feedmgr.feed;

import java.util.List;

import com.thinkbiganalytics.metadata.api.BaseProvider;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface FeedManagerFeedProvider extends BaseProvider<FeedManagerFeed, Feed.ID> {

    FeedManagerFeed findBySystemName(String categorySystemName, String systemName);

    List<? extends FeedManagerFeed> findByTemplateId(FeedManagerTemplate.ID templateId);

    List<? extends FeedManagerFeed> findByCategoryId(FeedManagerCategory.ID categoryId);

    FeedManagerFeed ensureFeed(Feed feed);

    FeedManagerFeed ensureFeed(Category.ID categoryId, String feedSystemName);




}
