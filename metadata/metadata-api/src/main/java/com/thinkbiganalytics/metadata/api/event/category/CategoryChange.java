/**
 *
 */
package com.thinkbiganalytics.metadata.api.event.category;

/*-
 * #%L
 * thinkbig-metadata-api
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

import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.event.MetadataChange;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

/**
 *
 */
public class CategoryChange extends MetadataChange {

    public static final String CLUSTER_EVENT_TYPE = "CategoryChange";

    private static final long serialVersionUID = 1L;

    private final Category.ID categoryId;
    private final String categoryName;

    public CategoryChange(ChangeType change, Category.ID categoryId) {
        this(change, "", categoryId);
    }

    public CategoryChange(ChangeType change, String descr, Category.ID categoryId) {
        this(change,descr,null,categoryId);
    }

    public CategoryChange(ChangeType change, String descr, String categoryName, Category.ID categoryId) {
        super(change, descr);
        this.categoryId = categoryId;
        this.categoryName = StringUtils.isBlank(categoryName)? null : categoryName;
    }

    public Category.ID getCategoryId() {
        return categoryId;
    }

    public Optional<String> getCategoryName() {
        return Optional.ofNullable(categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.categoryId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CategoryChange) {
            CategoryChange that = (CategoryChange) obj;
            return super.equals(that) &&
                   Objects.equals(this.categoryId, that.categoryId);
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Category change ");
        return sb
            .append("(").append(getChange()).append(") - ")
            .append("ID: ").append(this.categoryId)
            .toString();

    }
}
