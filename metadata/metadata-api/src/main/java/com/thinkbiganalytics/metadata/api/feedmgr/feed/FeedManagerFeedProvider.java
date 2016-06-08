package com.thinkbiganalytics.metadata.api.feedmgr.feed;

import com.thinkbiganalytics.metadata.api.BaseProvider;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;

import java.util.List;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface FeedManagerFeedProvider extends BaseProvider<Feed, Feed.ID> {

    FeedManagerFeed findBySystemName(String categorySystemName, String systemName);

    List<? extends FeedManagerFeed> findByTemplateId(FeedManagerTemplate.ID templateId);

    List<? extends FeedManagerFeed> findByCategoryId(FeedManagerCategory.ID categoryId);
}
