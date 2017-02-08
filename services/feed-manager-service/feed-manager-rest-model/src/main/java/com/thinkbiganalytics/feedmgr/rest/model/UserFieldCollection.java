package com.thinkbiganalytics.feedmgr.rest.model;

/*-
 * #%L
 * thinkbig-feed-manager-rest-model
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Set;

/**
 * A collection of user-defined fields for all categories and feeds.
 *
 * @see UserField
 * @since 0.4.0
 */
public class UserFieldCollection {

    /**
     * User-defined fields for categories
     */
    private Set<UserField> categoryFields;

    /**
     * User-defined fields for feeds
     */
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
