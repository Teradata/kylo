package com.thinkbiganalytics.feedmgr.config;

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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Cache to hold information on whether feeds required for history data reindexing are available in Kylo
 */
@Component
public class HistoryDataReindexingFeedsAvailableCache implements PostMetadataConfigAction {

    private static final Logger log = LoggerFactory.getLogger(HistoryDataReindexingFeedsAvailableCache.class);

    @org.springframework.beans.factory.annotation.Value("${search.history.data.reindexing.regular.feed.category:system}")
    private String historyDataReindexingRegularFeedCategory;

    @org.springframework.beans.factory.annotation.Value("${search.history.data.reindexing.regular.feed.systemName:index_text_service}")
    private String historyDataReindexingRegularFeedSystemName;

    @org.springframework.beans.factory.annotation.Value("${search.history.data.reindexing.reindex.feed.category:system}")
    private String historyDataReindexingReindexFeedCategory;

    @org.springframework.beans.factory.annotation.Value("${search.history.data.reindexing.reindex.feed.systemName:history_reindex_text_service}")
    private String historyDataReindexingReindexFeedSystemName;

    @Inject
    private FeedProvider feedProvider;

    @Inject
    protected MetadataAccess metadataAccess;

    private boolean regularFeedAvailableWithMatchingName = false;
    private boolean regularFeedAvailableWithMatchingProperty = false;
    private boolean reindexFeedAvailableWithMatchingName = false;
    private boolean reindexFeedAvailableWithMatchingProperty = false;

    //cache
    private boolean kyloHistoryDataReindexingFeedsAvailable = false;
    private boolean initialized = false;

    public static final String REINDEX_SUPPORT_PROPERTY_NAME_FOR_CHECK = "kylo-history-reindex-support";
    public static final String REINDEX_SUPPORT_PROPERTY_VALUE_FOR_CHECK = "true";

    public void setKyloHistoryDataReindexingFeedsAvailable(boolean kyloHistoryDataReindexingFeedsAvailable) {
        this.kyloHistoryDataReindexingFeedsAvailable = kyloHistoryDataReindexingFeedsAvailable;
    }

    public String getHistoryDataReindexingRegularFeedCategory() {
        return historyDataReindexingRegularFeedCategory;
    }

    public void setHistoryDataReindexingRegularFeedCategory(String historyDataReindexingRegularFeedCategory) {
        this.historyDataReindexingRegularFeedCategory = historyDataReindexingRegularFeedCategory;
    }

    public String getHistoryDataReindexingRegularFeedSystemName() {
        return historyDataReindexingRegularFeedSystemName;
    }

    public void setHistoryDataReindexingRegularFeedSystemName(String historyDataReindexingRegularFeedSystemName) {
        this.historyDataReindexingRegularFeedSystemName = historyDataReindexingRegularFeedSystemName;
    }

    public String getHistoryDataReindexingReindexFeedCategory() {
        return historyDataReindexingReindexFeedCategory;
    }

    public void setHistoryDataReindexingReindexFeedCategory(String historyDataReindexingReindexFeedCategory) {
        this.historyDataReindexingReindexFeedCategory = historyDataReindexingReindexFeedCategory;
    }

    public String getHistoryDataReindexingReindexFeedSystemName() {
        return historyDataReindexingReindexFeedSystemName;
    }

    public void setHistoryDataReindexingReindexFeedSystemName(String historyDataReindexingReindexFeedSystemName) {
        this.historyDataReindexingReindexFeedSystemName = historyDataReindexingReindexFeedSystemName;
    }

    public boolean isRegularFeedAvailableWithMatchingName() {
        return regularFeedAvailableWithMatchingName;
    }

    public void setRegularFeedAvailableWithMatchingName(boolean regularFeedAvailableWithMatchingName) {
        this.regularFeedAvailableWithMatchingName = regularFeedAvailableWithMatchingName;
    }

    public boolean isRegularFeedAvailableWithMatchingProperty() {
        return regularFeedAvailableWithMatchingProperty;
    }

    public void setRegularFeedAvailableWithMatchingProperty(boolean regularFeedAvailableWithMatchingProperty) {
        this.regularFeedAvailableWithMatchingProperty = regularFeedAvailableWithMatchingProperty;
    }

    public boolean isReindexFeedAvailableWithMatchingName() {
        return reindexFeedAvailableWithMatchingName;
    }

    public void setReindexFeedAvailableWithMatchingName(boolean reindexFeedAvailableWithMatchingName) {
        this.reindexFeedAvailableWithMatchingName = reindexFeedAvailableWithMatchingName;
    }

    public boolean isReindexFeedAvailableWithMatchingProperty() {
        return reindexFeedAvailableWithMatchingProperty;
    }

    public void setReindexFeedAvailableWithMatchingProperty(boolean reindexFeedAvailableWithMatchingProperty) {
        this.reindexFeedAvailableWithMatchingProperty = reindexFeedAvailableWithMatchingProperty;
    }

    private void init() {
        updateInfoFlagsForRegularFeed();
        updateInfoFlagsForReindexFeed();
        checkAndSetKyloHistoryDataReindexingFeedsAvailableFlag();
        initialized = true;
        log.info("Initialized cache to look up availability of history data reindexing feeds");
    }

    public boolean areKyloHistoryDataReindexingFeedsAvailable() {
        if (!initialized) {
            init();
        }
        return kyloHistoryDataReindexingFeedsAvailable;
    }

    private void updateInfoFlagsForRegularFeed() {
        metadataAccess.read(() -> {
            Feed regularIndexFeed = feedProvider.findBySystemName(historyDataReindexingRegularFeedCategory, historyDataReindexingRegularFeedSystemName);
            if (regularIndexFeed == null) {
                regularFeedAvailableWithMatchingName = false;
                regularFeedAvailableWithMatchingProperty = false;
            } else {
                regularFeedAvailableWithMatchingName = true;
                if (!regularIndexFeed.getUserProperties().isEmpty()
                    && regularIndexFeed.getUserProperties().containsKey(REINDEX_SUPPORT_PROPERTY_NAME_FOR_CHECK)
                    && regularIndexFeed.getUserProperties().get(REINDEX_SUPPORT_PROPERTY_NAME_FOR_CHECK).equals(REINDEX_SUPPORT_PROPERTY_VALUE_FOR_CHECK)) {
                    regularFeedAvailableWithMatchingProperty = true;
                    log.info("Feed supporting history data reindexing (regular feed) found in Kylo");
                } else {
                    regularFeedAvailableWithMatchingProperty = false;
                }
            }
        }, MetadataAccess.SERVICE);
    }

    private void updateInfoFlagsForReindexFeed() {
        metadataAccess.read(() -> {
            Feed historyReindexFeed = feedProvider.findBySystemName(historyDataReindexingReindexFeedCategory, historyDataReindexingReindexFeedSystemName);
            if (historyReindexFeed == null) {
                reindexFeedAvailableWithMatchingName = false;
                reindexFeedAvailableWithMatchingProperty = false;
            } else {
                reindexFeedAvailableWithMatchingName = true;
                if (!historyReindexFeed.getUserProperties().isEmpty()
                    && historyReindexFeed.getUserProperties().containsKey(REINDEX_SUPPORT_PROPERTY_NAME_FOR_CHECK)
                    && historyReindexFeed.getUserProperties().get(REINDEX_SUPPORT_PROPERTY_NAME_FOR_CHECK).equals(REINDEX_SUPPORT_PROPERTY_VALUE_FOR_CHECK)) {
                    reindexFeedAvailableWithMatchingProperty = true;
                    log.info("Feed supporting history data reindexing (reindex feed) found in Kylo");
                } else {
                    reindexFeedAvailableWithMatchingProperty = false;
                }
            }
        }, MetadataAccess.SERVICE);
    }

    private boolean checkAndSetKyloHistoryDataReindexingFeedsAvailableFlag() {
        if (regularFeedAvailableWithMatchingName
            && regularFeedAvailableWithMatchingProperty
            && reindexFeedAvailableWithMatchingName
            && reindexFeedAvailableWithMatchingProperty) {

            if (!kyloHistoryDataReindexingFeedsAvailable) {
                log.info("Feeds supporting history data reindexing now available");
            }
            kyloHistoryDataReindexingFeedsAvailable = true;
        } else {
            if (kyloHistoryDataReindexingFeedsAvailable) {
                log.info("Feeds supporting history data reindexing now missing");
            }

            kyloHistoryDataReindexingFeedsAvailable = false;
        }
        return kyloHistoryDataReindexingFeedsAvailable;
    }

    //returns updated indicator flag
    public boolean updateCache(String categorySystemName, String feedSystemName) {

        if (!initialized) {
            init();
        }
        //Check if category and feed name match with the ones configured in this class. If yes, then update the info flags. Then update the cache flag that uses the info flags.
        if ((categorySystemName.equals(historyDataReindexingRegularFeedCategory)) && (feedSystemName.equals(historyDataReindexingRegularFeedSystemName))) {
            updateInfoFlagsForRegularFeed();
        } else if ((categorySystemName.equals(historyDataReindexingReindexFeedCategory)) && (feedSystemName.equals(historyDataReindexingReindexFeedSystemName))) {
            updateInfoFlagsForReindexFeed();
        }
        return checkAndSetKyloHistoryDataReindexingFeedsAvailableFlag();
    }

    @Override
    public void run() {

    }
}
