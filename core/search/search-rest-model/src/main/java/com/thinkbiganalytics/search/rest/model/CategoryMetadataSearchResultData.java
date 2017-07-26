package com.thinkbiganalytics.search.rest.model;

/*-
 * #%L
 * kylo-search-rest-model
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

/**
 * Stores the search results coming from a category metadata
 */
public class CategoryMetadataSearchResultData extends AbstractSearchResultData {

    private String categorySystemName;
    private String categoryTitle;
    private String categoryDescription;

    public CategoryMetadataSearchResultData() {
        final String ICON = "folder_special";
        final String COLOR = "Indigo";
        super.setIcon(ICON);
        super.setColor(COLOR);
        super.setType(SearchResultType.KYLO_CATEGORIES);
    }

    public String getCategorySystemName() {
        return categorySystemName;
    }

    public void setCategorySystemName(String categorySystemName) {
        this.categorySystemName = categorySystemName;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public String getCategoryDescription() {
        return categoryDescription;
    }

    public void setCategoryDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }
}
