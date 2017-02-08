/**
 *
 */
package com.thinkbiganalytics.metadata.api.sla;

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

import com.thinkbiganalytics.metadata.sla.api.Metric;

import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public abstract class DependentFeed implements Metric {

    private String feedName;
    private String categoryName;

    private String categoryAndFeed;

    public DependentFeed() {
    }

    public DependentFeed(String categoryAndFeed) {
        super();
        this.categoryName = StringUtils.substringBefore(categoryAndFeed, ".");
        this.feedName = StringUtils.substringAfter(categoryAndFeed, ".");
        this.categoryAndFeed = categoryAndFeed;
    }

    public DependentFeed(String categoryName, String feedName) {
        super();
        this.categoryName = categoryName;
        this.feedName = feedName;
        this.categoryAndFeed = categoryName + "." + feedName;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryAndFeed() {
        return categoryAndFeed;
    }
}
