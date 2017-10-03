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
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.AlertCriteria;
import com.thinkbiganalytics.alerts.api.AlertProvider;
import com.thinkbiganalytics.alerts.api.AlertSummary;
import com.thinkbiganalytics.alerts.api.core.AlertCriteriaInput;
import com.thinkbiganalytics.alerts.rest.AlertsModel;
import com.thinkbiganalytics.alerts.rest.model.AlertSummaryGrouped;
import com.thinkbiganalytics.metadata.cache.util.TimeUtil;
import com.thinkbiganalytics.metadata.config.RoleSetExposingSecurityExpressionRoot;
import com.thinkbiganalytics.metadata.jpa.feed.security.FeedAclCache;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Created by sr186054 on 9/27/17.
 */
public class AlertsCache implements TimeBasedCache<AlertSummaryGrouped> {


    private static final Logger log = LoggerFactory.getLogger(AlertsCache.class);


    @Inject
    private FeedAclCache feedAclCache;

    @Inject
    private AlertProvider alertProvider;

    @Inject
    private AlertsModel alertsModel;


    LoadingCache<Long, List<AlertSummaryGrouped>> alertSummaryCache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).build(new CacheLoader<Long, List<AlertSummaryGrouped>>() {
        @Override
        public List<AlertSummaryGrouped> load(Long millis) throws Exception {
            return fetchUnhandledAlerts();
        }
    });

    public List<AlertSummaryGrouped> getAlertSummary(Long time) {
        return alertSummaryCache.getUnchecked(time);
    }

    public List<AlertSummaryGrouped> getUserAlertSummary() {
        Long time = TimeUtil.getTimeNearestFiveSeconds();
        return getUserAlertSummary(time);
    }

    public List<AlertSummaryGrouped> getUserAlertSummary(Long time) {
        RoleSetExposingSecurityExpressionRoot userContext = feedAclCache.userContext();
        return getAlertSummary(time).stream().filter(alertSummaryGrouped -> feedAclCache.hasAccess(userContext, alertSummaryGrouped.getFeedId())).collect(Collectors.toList());
    }

    public List<AlertSummaryGrouped> getUserAlertSummaryForFeedId(String feedId) {
        Long time = TimeUtil.getTimeNearestFiveSeconds();
        return getUserAlertSummaryForFeedId(time, feedId);
    }


    public List<AlertSummaryGrouped> getUserAlertSummaryForFeedId(Long time, String feedId) {
        if (StringUtils.isBlank(feedId)) {
            return new ArrayList<>();
        } else {
            RoleSetExposingSecurityExpressionRoot userContext = feedAclCache.userContext();
            return getAlertSummary(time).stream()
                .filter(alertSummaryGrouped -> feedId.equalsIgnoreCase(alertSummaryGrouped.getFeedId()) && feedAclCache.hasAccess(userContext, alertSummaryGrouped.getFeedId()))
                .collect(Collectors.toList());
        }
    }

    public List<AlertSummaryGrouped> getUserAlertSummaryForFeedName(String feedName) {
        Long time = TimeUtil.getTimeNearestFiveSeconds();
        return getUserAlertSummaryForFeedName(time, feedName);
    }

    public List<AlertSummaryGrouped> getUserAlertSummaryForFeedName(Long time, String feedName) {
        if (StringUtils.isBlank(feedName)) {
            return new ArrayList<>();
        } else {
            RoleSetExposingSecurityExpressionRoot userContext = feedAclCache.userContext();
            return getAlertSummary(time).stream()
                .filter(alertSummaryGrouped -> feedName.equalsIgnoreCase(alertSummaryGrouped.getFeedName()) && feedAclCache.hasAccess(userContext, alertSummaryGrouped.getFeedId()))
                .collect(Collectors.toList());
        }
    }

    @Override
    public List<AlertSummaryGrouped> getCache(Long time) {
        return getAlertSummary(time);
    }

    @Override
    public List<AlertSummaryGrouped> getUserCache(Long time) {
        return getUserAlertSummary(time);
    }

    protected List<AlertSummaryGrouped> fetchUnhandledAlerts() {
        List<AlertSummary> alerts = new ArrayList<>();
        AlertCriteria criteria = alertProvider.criteria();
        new AlertCriteriaInput.Builder()
            .state(Alert.State.UNHANDLED)
            .asServiceAccount(true)
            .onlyIfChangesDetected(true)
            .applyToCriteria(criteria);
        Iterator<? extends AlertSummary> itr = alertProvider.getAlertsSummary(criteria);
        if (itr.hasNext()) {
            itr.forEachRemaining(alerts::add);
            List<AlertSummaryGrouped> latestAlerts = new ArrayList<>(alertsModel.groupAlertSummaries(alerts));
            return latestAlerts;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isAvailable() {
        return feedAclCache.isUserCacheAvailable();
    }
}
