package com.thinkbiganalytics.metadata.cache;
/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.jobrepo.query.model.FeedHealth;
import com.thinkbiganalytics.jobrepo.query.model.FeedStatus;
import com.thinkbiganalytics.jobrepo.query.model.transform.FeedModelTransform;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.feed.FeedSummary;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.cache.util.TimeUtil;
import com.thinkbiganalytics.metadata.config.RoleSetExposingSecurityExpressionRoot;
import com.thinkbiganalytics.metadata.jpa.feed.security.FeedAclCache;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Created by sr186054 on 9/27/17.
 */
public class FeedHealthSummaryCache implements TimeBasedCache<FeedSummary> {


    private static final Logger log = LoggerFactory.getLogger(FeedHealthSummaryCache.class);

    @Inject
    private MetadataEventService metadataEventService;

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    OpsManagerFeedProvider opsManagerFeedProvider;

    @Inject
    private FeedAclCache feedAclCache;


    LoadingCache<Long, List<? extends FeedSummary>> feedSummaryCache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).build(new CacheLoader<Long, List<? extends FeedSummary>>() {
        @Override
        public List<? extends FeedSummary> load(Long millis) throws Exception {
            return fetchFeedSummary();
        }
    });


    public List<? extends FeedSummary> getFeedSummaryList(Long time) {
        return feedSummaryCache.getUnchecked(time);
    }


    public FeedStatus getUserFeeds() {
        Long time = TimeUtil.getTimeNearestFiveSeconds();
        return getUserFeeds(time);
    }

    public FeedStatus getUserFeed(String feedName) {
        Long time = TimeUtil.getTimeNearestFiveSeconds();
        return getUserFeeds(time, feedName);
    }

    /**
     * Used for Feed Health KPI and Feed card
     */
    public FeedStatus getUserFeeds(Long time) {
        return getUserFeeds(time, null);
    }

    public FeedStatus getUserFeeds(Long time, String feedName) {
        RoleSetExposingSecurityExpressionRoot userContext = feedAclCache.userContext();
        //streamline the summaries as they could have multiple
        Map<String, FeedSummary> latestFeeds = new HashMap<>();

        Comparator<FeedSummary> byRunningStatus = Comparator.comparing(FeedSummary::getRunStatus, Comparator.nullsLast(Comparator.naturalOrder()));

        Comparator<FeedSummary> byStartTime = Comparator.comparing(FeedSummary::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()));

        getFeedSummaryList(time).stream()
            .filter(summary -> (StringUtils.isBlank(feedName) || feedName.equalsIgnoreCase(summary.getFeedName())) && feedAclCache.hasAccess(userContext, summary.getFeedId().toString()))
            .sorted(byRunningStatus.thenComparing(byStartTime)).forEach(f -> {
            String feedId = f.getFeedId().toString();
            if (!latestFeeds.containsKey(feedId)) {
                latestFeeds.put(feedId, f);
            }
        });

        //NOTE it could also populate the last job execution time since the above query gets a union of the running jobs along with the latest finished jobs by feed
        List<FeedHealth> feedSummaryHealth = latestFeeds.values().stream().map(f -> FeedModelTransform.feedHealth(f)).collect(Collectors.toList());
        return FeedModelTransform.feedStatus(feedSummaryHealth);
    }


    @Override
    public List<FeedSummary> getCache(Long time) {
        return (List<FeedSummary>) getFeedSummaryList(time);
    }

    @Override
    public List<FeedSummary> getUserCache(Long time) {
        return (List<FeedSummary>) getUserFeeds(time);
    }

    private List<? extends FeedSummary> fetchFeedSummary() {
        return metadataAccess.read(() -> {
            List<? extends FeedSummary> list = opsManagerFeedProvider.findFeedSummary();
            return list;
        }, MetadataAccess.SERVICE);
    }

    @Override
    public boolean isAvailable() {
        return feedAclCache.isUserCacheAvailable();
    }

}
