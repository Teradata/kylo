/*-
 * #%L
 * thinkbig-ui-feed-manager
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
package com.thinkbiganalytics.feedmgr.service.feed.reindexing;

import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiFeedStats;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.NifiFeedStatisticsRepository;
import com.thinkbiganalytics.metadata.rest.model.feed.reindex.HistoryReindexingState;

import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Service to support data history reindexing
 */
@Component
public class FeedHistoryDataReindexingService {

    @org.springframework.beans.factory.annotation.Value("${search.history.data.reindexing.enabled:false}")
    private boolean historyDataReindexingEnabled;

    @Inject
    NifiFeedStatisticsRepository nifiFeedStatisticsRepository;

    public FeedHistoryDataReindexingService() {

    }

    public boolean isHistoryDataReindexingEnabled() {
        return historyDataReindexingEnabled;
    }

    public void setHistoryDataReindexingEnabled(boolean historyDataReindexingEnabled) {
        this.historyDataReindexingEnabled = historyDataReindexingEnabled;
    }

    public boolean isFeedRunning(FeedMetadata feedMetadata) {
        JpaNifiFeedStats latestFeedStatus = nifiFeedStatisticsRepository.findLatestForFeedWithoutAcl(feedMetadata.getCategoryAndFeedName());
        if (latestFeedStatus != null) {
            return latestFeedStatus.isFeedRunning();
        }
        return false;
    }

    //Accept feed data history reindexing request only if:
    // (1) enabled via Kylo services
    // (2) feed is not currently running
    public void checkAndEnsureFeedHistoryDataReindexingRequestIsAcceptable(FeedMetadata feedMetadata) {
        if (!feedMetadata.getHistoryReindexingStatus().equals(HistoryReindexingState.DIRTY.toString())) {
            return;
        }

        if (!isHistoryDataReindexingEnabled()) {
            throw new FeedHistoryDataReindexingNotEnabledException();
        }

        if (isFeedRunning(feedMetadata)) {
            throw new FeedCurrentlyRunningException(feedMetadata.getCategoryName(), feedMetadata.getFeedName());
        }
    }
}
