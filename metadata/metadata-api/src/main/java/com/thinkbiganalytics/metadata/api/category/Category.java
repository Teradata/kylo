package com.thinkbiganalytics.metadata.api.category;


import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface Category {

    interface ID extends Serializable { }


    ID getId();

    List<Feed> getFeeds();

    String getDisplayName();

    String getName();


    Integer getVersion();

    String getDescription();

    void setDescription(String description);

    DateTime getCreatedTime();

    DateTime getModifiedTime();

}
