package com.thinkbiganalytics.metadata.api.category;

import com.thinkbiganalytics.metadata.api.BaseProvider;

/**
 * Manages the repository of category objects.
 *
 * @param <T> the type of provided category
 */
public interface CategoryProvider<T extends Category> extends BaseProvider<T, Category.ID> {

    T findBySystemName(String systemName);

    T ensureCategory(String systemName);


}
