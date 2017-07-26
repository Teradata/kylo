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

import java.util.List;

/**
 * Stores the search results coming from a feed metadata
 */
public class FeedMetadataSearchResultData extends AbstractSearchResultData {

    private String feedSystemName;
    private String feedTitle;
    private String feedDescription;
    private String feedCategoryId;
    private List<String> feedTags;

    public FeedMetadataSearchResultData() {
        final String ICON = "linear_scale";
        final String COLOR = "Maroon";
        super.setIcon(ICON);
        super.setColor(COLOR);
        super.setType(SearchResultType.KYLO_FEEDS);
    }

    public String getFeedSystemName() {
        return feedSystemName;
    }

    public void setFeedSystemName(String feedSystemName) {
        this.feedSystemName = feedSystemName;
    }

    public String getFeedTitle() {
        return feedTitle;
    }

    public void setFeedTitle(String feedTitle) {
        this.feedTitle = feedTitle;
    }

    public String getFeedDescription() {
        return feedDescription;
    }

    public void setFeedDescription(String feedDescription) {
        this.feedDescription = feedDescription;
    }

    public String getFeedCategoryId() {
        return feedCategoryId;
    }

    public void setFeedCategoryId(String feedCategoryId) {
        this.feedCategoryId = feedCategoryId;
    }

    public List<String> getFeedTags() {
        return feedTags;
    }

    public void setFeedTags(List<String> feedTags) {
        this.feedTags = feedTags;
    }
}
