package com.thinkbiganalytics.feedmgr.service.category;

import com.thinkbiganalytics.feedmgr.InvalidOperationException;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.UserField;
import com.thinkbiganalytics.feedmgr.rest.model.UserProperty;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Manages Feed Manager categories.
 */
public interface FeedManagerCategoryService {

    Collection<FeedCategory> getCategories();

    FeedCategory getCategoryById(String id);

    FeedCategory getCategoryBySystemName(String name);

    void saveCategory(FeedCategory category);

    boolean deleteCategory(String categoryId) throws InvalidOperationException;

    /**
     * Gets the user-defined fields for all categories.
     *
     * @return the user-defined fields
     */
    @Nonnull
    Set<UserField> getUserFields();

    /**
     * Gets the user-defined fields for all categories.
     *
     * @return the user-defined fields
     */
    @Nonnull
    Set<UserProperty> getUserProperties();

    /**
     * Sets the user-defined fields for all categories.
     *
     * @param userFields the new set of user-defined fields
     */
    void setUserFields(@Nonnull Set<UserField> userFields);
}
