package com.thinkbiganalytics.metadata.api.category;


import java.io.Serializable;
import java.util.List;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.feed.Feed;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface Category {

    interface ID extends Serializable { }


    ID getId();

    List<? extends Feed> getFeeds();

    String getDisplayName();

    String getName();


    Integer getVersion();

    String getDescription();

    void setDescription(String description);

    DateTime getCreatedTime();

    DateTime getModifiedTime();

    void setDisplayName(String displayName);

    void setName(String name);

    void setCreatedTime(DateTime createdTime);
    void setModifiedTime(DateTime modifiedTime);



}
