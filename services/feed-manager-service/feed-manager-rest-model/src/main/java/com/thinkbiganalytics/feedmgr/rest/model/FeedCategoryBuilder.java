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

import com.thinkbiganalytics.feedmgr.rest.support.SystemNamingService;

/**
 */
public class FeedCategoryBuilder {

    private String name;
    private String description;
    private String icon;
    private String iconColor;
    private int relatedFeeds;


    public FeedCategoryBuilder(String name) {
        this.name = name;
        this.iconColor = "black";
    }

    public FeedCategoryBuilder description(String description) {
        this.description = description;
        return this;
    }

    public FeedCategoryBuilder icon(String icon) {
        this.icon = icon;
        return this;
    }

    public FeedCategoryBuilder iconColor(String iconColor) {
        this.iconColor = iconColor;
        return this;
    }

    public FeedCategoryBuilder relatedFeeds(int relatedFeeds) {
        this.relatedFeeds = relatedFeeds;
        return this;
    }

    public FeedCategory build() {
        FeedCategory category = new FeedCategory();
        category.setName(this.name);
        category.setDescription(this.description);
        category.setIconColor(this.iconColor);
        category.setIcon(this.icon);

        category.setSystemName(SystemNamingService.generateSystemName(category.getName()));
        return category;
    }


}

