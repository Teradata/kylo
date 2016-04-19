package com.thinkbiganalytics.feedmgr.rest.model;

import java.util.Date;

/**
 * Created by sr186054 on 4/1/16.
 */
public interface UIFeed {
    String getCategoryName();

    Long getCategoryId();

    Long getId();

    String getFeedName();

    boolean isActive();

    Date getUpdateDate();

    String getCategoryAndFeedDisplayName();

    String getSystemCategoryName();

    String getSystemFeedName();

    String getFeedId();

    String getCategoryIcon();

    String getCategoryIconColor();
}
