package com.thinkbiganalytics.metadata.api.feedmgr.category;


import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.feedmgr.feed.FeedManagerFeed;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface FeedManagerCategory extends Category{

    String getIcon();

    String getIconColor();
}
