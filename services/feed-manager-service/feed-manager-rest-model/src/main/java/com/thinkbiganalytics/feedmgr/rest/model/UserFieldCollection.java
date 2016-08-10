package com.thinkbiganalytics.feedmgr.rest.model;

import java.util.Set;

/**
 * A collection of user-defined fields for all categories and feeds.
 *
 * @see UserField
 * @since 0.4.0
 */
public class UserFieldCollection {

    /** User-defined fields for categories */
    private Set<UserField> categoryFields;

    /** User-defined fields for feeds */
    private Set<UserField> feedFields;

    /**
     * Gets the user-defined fields for all categories.
     *
     * @return the user-defined fields
     */
    public Set<UserField> getCategoryFields() {
        return categoryFields;
    }

    /**
     * Sets the user-defined fields for all categories.
     *
     * @param categoryFields the user-defined fields
     */
    public void setCategoryFields(Set<UserField> categoryFields) {
        this.categoryFields = categoryFields;
    }

    /**
     * Gets the user-defined fields for all feeds.
     *
     * @return the user-defined fields
     */
    public Set<UserField> getFeedFields() {
        return feedFields;
    }

    /**
     * Sets the user-defined fields for all feeds.
     *
     * @param feedFields the user-defined fields
     */
    public void setFeedFields(Set<UserField> feedFields) {
        this.feedFields = feedFields;
    }
}
