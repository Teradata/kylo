package com.thinkbiganalytics.metadata.api.feedmgr.category;

import com.thinkbiganalytics.metadata.api.BaseProvider;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;

import java.io.Serializable;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface FeedManagerCategoryProvider extends CategoryProvider {
    FeedManagerCategory findBySystemName(String systemName);


}
