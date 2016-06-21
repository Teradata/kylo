package com.thinkbiganalytics.metadata.api.feedmgr.category;


import com.thinkbiganalytics.metadata.api.category.Category;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface FeedManagerCategory extends Category{

    String getIcon();

    String getIconColor();

    void setIcon(String icon);

    void setIconColor(String iconColor);
}
