package com.thinkbiganalytics.metadata.api.feedmgr.feed;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface FeedManagerFeed {

    void setTemplate(FeedManagerTemplate template);

    interface ID extends Serializable { }
    Feed getFeed();

    void setFeed(Feed feed);

    ID getId();

    FeedManagerCategory getCategory();

    void setCategory(FeedManagerCategory category);

    String getJson();

    void setJson(String json);

    String getState();

    void setState(String state);

    boolean isNew();

    FeedManagerTemplate getTemplate();

    DateTime getCreatedTime();

    DateTime getModifiedTime();
}
