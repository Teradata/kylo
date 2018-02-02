package com.thinkbiganalytics.metadata.rest.model.feed.reindex;

/*-
 * #%L
 * kylo-metadata-api
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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.Serializable;

/**
 * Hold status of data history reindexing for a feed (rest model)
 */
@SuppressWarnings("serial")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryReindexingStatus implements Serializable {

    private HistoryReindexingState historyReindexingState = HistoryReindexingState.NEVER_RUN;
    private DateTime lastModifiedTimestamp = DateTime.now(DateTimeZone.UTC);

    public HistoryReindexingStatus() {
        super();
    }

    public HistoryReindexingStatus(HistoryReindexingState historyReindexingState) {
        this(historyReindexingState, DateTime.now(DateTimeZone.UTC));
    }

    public HistoryReindexingStatus(HistoryReindexingState historyReindexingState, DateTime lastModifiedTimestamp) {
        super();
        this.historyReindexingState = historyReindexingState;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    public HistoryReindexingState getHistoryReindexingState() {
        return historyReindexingState;
    }

    public DateTime getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setHistoryReindexingState(HistoryReindexingState historyReindexingState) {
        this.historyReindexingState = historyReindexingState;
    }

    public void setLastModifiedTimestamp(DateTime lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }
}
