package com.thinkbiganalytics.metadata.rest.model.feed.reindex;

/*-
 * #%L
 * kylo-metadata-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * Information about updated history reindexing status of a feed, and columns to index
 */
@SuppressWarnings("serial")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedDataHistoryReindexParams implements Serializable {

    private String feedId;
    private String feedSystemName;
    private String categorySystemName;
    private String commaSeparatedColumnsForIndexing;
    private HistoryReindexingStatus historyReindexingStatus;

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getFeedSystemName() {
        return feedSystemName;
    }

    public void setFeedSystemName(String feedSystemName) {
        this.feedSystemName = feedSystemName;
    }

    public String getCategorySystemName() {
        return categorySystemName;
    }

    public void setCategorySystemName(String categorySystemName) {
        this.categorySystemName = categorySystemName;
    }

    public String getCommaSeparatedColumnsForIndexing() {
        return commaSeparatedColumnsForIndexing;
    }

    public void setCommaSeparatedColumnsForIndexing(String commaSeparatedColumnsForIndexing) {
        this.commaSeparatedColumnsForIndexing = commaSeparatedColumnsForIndexing;
    }

    public HistoryReindexingStatus getHistoryReindexingStatus() {
        return historyReindexingStatus;
    }

    public void setHistoryReindexingStatus(HistoryReindexingStatus historyReindexingStatus) {
        this.historyReindexingStatus = historyReindexingStatus;
    }
}
