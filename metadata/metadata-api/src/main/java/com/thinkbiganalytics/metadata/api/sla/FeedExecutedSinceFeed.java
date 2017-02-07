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

import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang3.StringUtils;

import java.beans.Transient;

/**
 */
public class FeedExecutedSinceFeed extends DependentFeed {


    private String sinceCategoryAndFeedName;

    private String sinceFeedName;
    private String sinceCategoryName;

    public FeedExecutedSinceFeed() {

    }

    public FeedExecutedSinceFeed(@JsonProperty("sinceCategoryAndFeedName") String sinceCategoryAndFeed, @JsonProperty("categoryAndFeed") String categoryAndFeed) {
        super(categoryAndFeed);
        this.sinceCategoryAndFeedName = sinceCategoryAndFeed;
        this.sinceCategoryName = StringUtils.substringBefore(sinceCategoryAndFeedName, ".");
        this.sinceFeedName = StringUtils.substringAfter(sinceCategoryAndFeedName, ".");
    }

    public FeedExecutedSinceFeed(String hasRunCategory, String hasRunFeed, String sinceCategory, String sinceFeed) {
        super(hasRunCategory, hasRunFeed);
        this.sinceFeedName = sinceFeed;
        this.sinceCategoryName = sinceCategory;
        this.sinceCategoryAndFeedName = sinceCategory + "." + this.sinceFeedName;
    }

    public String getSinceFeedName() {
        return sinceFeedName;
    }

    public void setSinceFeedName(String sinceFeedName) {
        this.sinceFeedName = sinceFeedName;
    }

    public String getSinceCategoryName() {
        return sinceCategoryName;
    }

    public void setSinceCategoryName(String sinceCategoryName) {
        this.sinceCategoryName = sinceCategoryName;
    }

    public String getSinceCategoryAndFeedName() {
        return sinceCategoryAndFeedName;
    }

    public void setSinceCategoryAndFeedName(String sinceCategoryAndFeedName) {
        this.sinceCategoryAndFeedName = sinceCategoryAndFeedName;
    }

    @Override
    @Transient
    public String getDescription() {
        return "Check if feed " + getSinceCategoryAndFeedName() + " has executed successfully since feed " + getSinceCategoryAndFeedName();
    }
}
