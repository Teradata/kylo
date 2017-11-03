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

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.AtomicLongMap;
import com.thinkbiganalytics.DateTimeUtil;
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
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.rest.model.search.SearchResultImpl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.comparator.NullSafeComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
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

    private Comparator<FeedSummary> byRunningStatus = Comparator.comparing(FeedSummary::getRunStatus, Comparator.nullsLast(Comparator.naturalOrder()));

    private Comparator<FeedSummary> byStartTime = Comparator.comparing(FeedSummary::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()));

    private Comparator<FeedSummary> byStream = Comparator.comparing(FeedSummary::isStream, Comparator.nullsLast(Comparator.naturalOrder()));

    private Comparator<FeedSummary> byName = Comparator.comparing(FeedSummary::getFeedName, Comparator.nullsLast(Comparator.naturalOrder()));

    private Comparator<FeedSummary> byHealth = new NullSafeComparator<FeedSummary>(new Comparator<FeedSummary>() {

        private Integer getHealthy(FeedSummary feedSummary) {
            return feedSummary.getFailedCount() == null || feedSummary.getFailedCount() == 0 ? 0 : 1;
        }

        @Override
        public int compare(FeedSummary o1, FeedSummary o2) {
            Integer healthy = getHealthy(o1);
            Integer healthy2 = getHealthy(o2);
            return healthy.compareTo(healthy2);
        }
    }, true);

    private Comparator<FeedSummary> bySinceTime = new NullSafeComparator<FeedSummary>(new Comparator<FeedSummary>() {

        private Long getTime(FeedSummary feedSummary) {
            Long time1 = -1L;
            if (feedSummary.getRunStatus() == FeedSummary.RunStatus.RUNNING && feedSummary.getStartTime() != null) {
                time1 = DateTimeUtil.getNowUTCTime().getMillis() - feedSummary.getStartTime().getMillis();
            } else if (feedSummary.getEndTime() != null) {
                time1 = DateTimeUtil.getNowUTCTime().getMillis() - feedSummary.getEndTime().getMillis();
            }
            return time1;
        }

        @Override
        public int compare(FeedSummary o1, FeedSummary o2) {
            Long time1 = getTime(o1);
            Long time2 = getTime(o2);
            return time1.compareTo(time2);
        }
    }, true);

    private Comparator<FeedSummary> byLastRunTime = new NullSafeComparator<FeedSummary>(new Comparator<FeedSummary>() {

        private Long getTime(FeedSummary feedSummary) {
            Long time1 = -1L;
            if (feedSummary.getRunStatus() == FeedSummary.RunStatus.RUNNING && feedSummary.getStartTime() != null) {
                time1 = -1L;
            } else if (feedSummary.getEndTime() != null && feedSummary.getStartTime() != null) {
                time1 = feedSummary.getEndTime().getMillis() - feedSummary.getStartTime().getMillis();
            }
            return time1;
        }

        @Override
        public int compare(FeedSummary o1, FeedSummary o2) {
            Long time1 = getTime(o1);
            Long time2 = getTime(o2);
            return time1.compareTo(time2);
        }
    }, true);

    private Comparator<FeedSummary> getComparator(String sort) {
        Comparator c = byName;
        if (sort.toLowerCase().contains("feed")) {
            c = byName;
        } else if (sort.toLowerCase().contains("health")) {
            c = byHealth;
        } else if (sort.toLowerCase().contains("status")) {
            c = byRunningStatus;
        } else if (sort.toLowerCase().contains("since")) {
            c = bySinceTime;
        } else if (sort.toLowerCase().contains("runtime")) {
            c = byLastRunTime;
        } else if (sort.toLowerCase().contains("stream")) {
            c = byStream;
        }

        return sort.startsWith("-") ? c.reversed() : c;
    }


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
        return getUserFeeds(time, new FeedSummaryFilter(feedName));
    }

    public FeedStatus getUserFeeds(FeedSummaryFilter feedSummaryFilter) {
        Long time = TimeUtil.getTimeNearestFiveSeconds();
        return getUserFeeds(time, feedSummaryFilter);
    }

    public Map<String, Long> getUserFeedHealthCounts(Long time) {
        AtomicLongMap<String> healthCounts = AtomicLongMap.create();
        RoleSetExposingSecurityExpressionRoot userContext = feedAclCache.userContext();
        List<? extends FeedSummary> list = getFeedSummaryList(time);
        list.stream()
            .filter(filter(new FeedSummaryFilter(), userContext))
            .forEach(f -> {
                String key = f.getFailedCount() == null || f.getFailedCount() == 0 ? "HEALTHY" : "UNHEALTHY";
                healthCounts.incrementAndGet(key);
            });
        return healthCounts.asMap();
    }

    /**
     * Used for Feed Health KPI and Feed card
     */
    public FeedStatus getUserFeeds(Long time) {
        return getUserFeeds(time, new FeedSummaryFilter());
    }

    public SearchResult getUserFeedHealth(FeedSummaryFilter feedSummaryFilter) {
        Long time = TimeUtil.getTimeNearestFiveSeconds();
        return getUserFeedHealth(time, feedSummaryFilter);
    }

    /**
     * @return SearchResult filled with FeedSummary objects
     */
    public SearchResult getUserFeedHealth(Long time, FeedSummaryFilter feedSummaryFilter) {
        SearchResult<com.thinkbiganalytics.jobrepo.query.model.FeedSummary> searchResult = new SearchResultImpl();
        RoleSetExposingSecurityExpressionRoot userContext = feedAclCache.userContext();
        List<FeedHealth> feedSummaryHealth = null;
        //get the entire list back and filter it for user access
        List<? extends FeedSummary> list = getFeedSummaryList(time).stream().filter(filter(feedSummaryFilter, userContext)).collect(Collectors.toList());
        feedSummaryHealth = list.stream()
            .sorted(feedSummaryFilter.getSort() != null ? getComparator(feedSummaryFilter.getSort()) : byName)
            .skip(feedSummaryFilter.getStart())
            .limit(feedSummaryFilter.getLimit() > 0 ? feedSummaryFilter.getLimit() : Integer.MAX_VALUE).map(f -> FeedModelTransform.feedHealth(f)).
                collect(Collectors.toList());

        //Transform it to FeedSummary objects
        FeedStatus feedStatus = FeedModelTransform.feedStatus(feedSummaryHealth);
        Long total = new Long(list.size());
        searchResult.setData(feedStatus.getFeedSummary());
        searchResult.setRecordsTotal(total);
        searchResult.setRecordsFiltered(total);

        return searchResult;
    }

    public FeedStatus getUserFeeds(Long time, FeedSummaryFilter feedSummaryFilter) {
        SearchResult<com.thinkbiganalytics.jobrepo.query.model.FeedSummary> searchResult = getUserFeedHealth(time, feedSummaryFilter);
        List<com.thinkbiganalytics.jobrepo.query.model.FeedSummary> feedSummaryHealth = searchResult.getData();
        return FeedModelTransform.feedStatusFromFeedSummary(feedSummaryHealth);
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
            Stopwatch stopwatch = Stopwatch.createStarted();
            List<? extends FeedSummary> list = opsManagerFeedProvider.findFeedSummary();
            log.debug("******FETCHING FEEDS!!!!!!");
            Map<String, FeedSummary> latestFeeds = new HashMap<>();
            //NOTE it could also populate the last job execution time since the above query gets a union of the running jobs along with the latest finished jobs by feed
            list.stream()
                .sorted(byRunningStatus.thenComparing(byStartTime)).forEach(f -> {
                String feedId = f.getFeedId().toString();
                if (!latestFeeds.containsKey(feedId)) {
                    latestFeeds.put(feedId, f);
                }
            });
            stopwatch.stop();
            log.debug("Time to fetchAndDedupe FeedSummary: {} ", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new ArrayList<>(latestFeeds.values());
        }, MetadataAccess.SERVICE);
    }

    @Override
    public boolean isAvailable() {
        return feedAclCache.isUserCacheAvailable();
    }

    private Predicate<FeedSummary> filter(FeedSummaryFilter filter, RoleSetExposingSecurityExpressionRoot userContext) {
        return s -> {
            try {
                return feedAclCache.hasAccess(userContext, s.getFeedId().toString()) && fixedFilter(s, filter) && (filter.containsFeed(s.getFeedName()) || filter
                    .containsState(s.getRunStatus().name().toLowerCase()));
            } catch (Exception e) {
                return false;
            }
        };

    }

    private boolean fixedFilter(FeedSummary feedSummary, FeedSummaryFilter feedSummaryFilter) {
        switch (feedSummaryFilter.getFilter()) {
            case ALL:
                return true;
            case HEALTHY:
                return feedSummary.getFailedCount() == null || feedSummary.getFailedCount() == 0L;
            case UNHEALTHY:
                return feedSummary.getFailedCount() != null && feedSummary.getFailedCount() > 0L;
            case RUNNING:
                return feedSummary.getRunStatus() == FeedSummary.RunStatus.RUNNING;
            case STREAMING:
                return feedSummary.isStream();
            default:
                return true;
        }
    }


    public static class FeedSummaryFilter {

        public enum FIXED_FILTER {
            ALL, HEALTHY, UNHEALTHY, RUNNING, STREAMING
        }

        String feedName;
        String state;

        Integer limit = 0;
        Integer start = 0;
        String sort;
        boolean applyPaging = false;

        FIXED_FILTER filter = FIXED_FILTER.ALL;

        public FeedSummaryFilter() {
        }

        public FeedSummaryFilter(String fixedFilter, String feedName, String state) {
            this.feedName = feedName;
            this.state = state;
            if (StringUtils.isNotBlank(fixedFilter)) {
                try {
                    this.filter = FIXED_FILTER.valueOf(fixedFilter.toUpperCase());
                } catch (Exception e) {
                    this.filter = FIXED_FILTER.ALL;
                }
            } else {
                this.filter = FIXED_FILTER.ALL;
            }
        }

        public FeedSummaryFilter(String feedName, String state) {
            this.feedName = feedName;
            this.state = state;
            this.filter = FIXED_FILTER.ALL;
        }

        public FeedSummaryFilter(String feedName) {
            this.feedName = feedName;
            this.filter = FIXED_FILTER.ALL;
        }

        public FeedSummaryFilter(String fixedFilter, String feedName, String state, Integer limit, Integer start, String sort) {
            this(fixedFilter, feedName, state);
            this.limit = limit;
            this.start = start;
            this.sort = sort;
        }

        public String getFeedName() {
            return feedName;
        }

        public void setFeedName(String feedName) {
            this.feedName = feedName;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public Integer getStart() {
            return start;
        }

        public void setStart(Integer start) {
            this.start = start;
        }

        public String getSort() {
            return sort;
        }

        public void setSort(String sort) {
            this.sort = sort;
        }

        public FIXED_FILTER getFilter() {
            return filter;
        }

        public void setFilter(FIXED_FILTER filter) {
            this.filter = filter;
        }

        public boolean containsFeed(String feed) {
            return StringUtils.isBlank(this.feedName) || (StringUtils.isNotBlank(this.feedName) && feed.contains(this.feedName));
        }

        public boolean containsState(String state) {
            return StringUtils.isBlank(this.state) || (StringUtils.isNotBlank(this.state) && state.contains(this.state));
        }

        public boolean isApplyPaging() {
            return applyPaging;
        }

        public void setApplyPaging(boolean applyPaging) {
            this.applyPaging = applyPaging;
        }
    }

}
