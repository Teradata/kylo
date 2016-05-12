package com.thinkbiganalytics.metadata.api.feedmgr.feed;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface FeedManagerFeed extends Feed{

    void setTemplate(FeedManagerTemplate template);

    FeedManagerCategory getCategory();

    void setCategory(FeedManagerCategory category);

    String getJson();

    void setJson(String json);

    boolean isNew();

    FeedManagerTemplate getTemplate();

    DateTime getCreatedTime();

    DateTime getModifiedTime();

}
