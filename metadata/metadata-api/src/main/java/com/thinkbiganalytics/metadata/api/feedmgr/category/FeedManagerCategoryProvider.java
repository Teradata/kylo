package com.thinkbiganalytics.metadata.api.feedmgr.category;

import com.thinkbiganalytics.metadata.api.BaseProvider;

import java.io.Serializable;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface FeedManagerCategoryProvider extends BaseProvider<FeedManagerCategory,FeedManagerCategory.ID>{
    FeedManagerCategory findBySystemName(String systemName);


}
