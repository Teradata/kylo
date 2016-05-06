package com.thinkbiganalytics.metadata.api.feedmgr.category;


import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface FeedManagerCategory {
    void setJson(String json);

    interface ID extends Serializable { }

    void setFeeds(List<FeedManagerFeed> feeds);

    ID getId();

    List<FeedManagerFeed> getFeeds();

    String getDisplayName();

    void setDisplayName(String displayName);

    String getSystemName();

    void setSystemName(String systemName);

    String getJson();

    String getDescription();

    void setDescription(String description);

    DateTime getCreatedTime();

    DateTime getModifiedTime();

    String getIcon();

    String getIconColor();
}
