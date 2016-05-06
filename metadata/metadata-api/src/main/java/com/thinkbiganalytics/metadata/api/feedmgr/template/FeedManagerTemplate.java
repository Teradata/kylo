package com.thinkbiganalytics.metadata.api.feedmgr.template;

import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface FeedManagerTemplate {

    interface ID extends Serializable { }
    List<FeedManagerFeed> getFeeds();

    ID getId();

    String getName();

    String getNifiTemplateId();

    String getDescription();

    boolean isDefineTable();

    boolean isDataTransformation();

    boolean isAllowPreconditions();

    String getIcon();

    String getIconColor();

    String getJson();

    DateTime getCreatedTime();

    DateTime getModifiedTime();
}
