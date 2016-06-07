package com.thinkbiganalytics.metadata.api.category;

import com.thinkbiganalytics.metadata.api.BaseProvider;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface CategoryProvider extends BaseProvider<Category,Category.ID>{
    Category findBySystemName(String systemName);

    Category ensureCategory(String systemName);


}
