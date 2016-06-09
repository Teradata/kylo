package com.thinkbiganalytics.metadata.api.category;

import com.thinkbiganalytics.metadata.api.BaseProvider;

/**
 * Created by sr186054 on 5/4/16.
 */
public interface CategoryProvider<T extends Category> extends BaseProvider<T,Category.ID>{
    T findBySystemName(String systemName);


    T ensureCategory(String systemName);


}
