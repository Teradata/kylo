package com.thinkbiganalytics.metadata.api.alerts;

/*-
 * #%L
 * thinkbig-alerts-default
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

import com.google.common.collect.ImmutableList;
import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.util.stream.Collectors;

/**
 * Created by sr186054 on 7/21/17.
 */
public class KyloEntityAwareAlertSummary implements EntityAwareAlertSummary {

    private String type;

    private String subtype;

    private Alert.Level level;

    private OpsManagerFeed.ID feedId;
    private String feedName;
    private ServiceLevelAgreement.ID slaId;
    private String slaName;

    private Long count;

    private Long lastAlertTimestamp;

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    @Override
    public Alert.Level getLevel() {
        return level;
    }

    public void setLevel(Alert.Level level) {
        this.level = level;
    }

    @Override
    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public Long getLastAlertTimestamp() {
        return lastAlertTimestamp;
    }

    public void setLastAlertTimestamp(Long lastAlertTimestamp) {
        this.lastAlertTimestamp = lastAlertTimestamp;
    }

    @Override
    public OpsManagerFeed.ID getFeedId() {
        return feedId;
    }

    public void setFeedId(OpsManagerFeed.ID feedId) {
        this.feedId = feedId;
    }

    @Override
    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    @Override
    public ServiceLevelAgreement.ID getSlaId() {
        return slaId;
    }

    public void setSlaId(ServiceLevelAgreement.ID slaId) {
        this.slaId = slaId;
    }

    @Override
    public String getSlaName() {
        return slaName;
    }

    public void setSlaName(String slaName) {
        this.slaName = slaName;
    }

    @Override
    public String getGroupByKey(){
       return new ImmutableList.Builder<String>()
           .add(getFeedId() != null ? getFeedId().toString() : "")
           .add(getSlaId() != null ? getSlaId().toString() : "")
           .add(getType())
           .add(getSubtype() != null ? getSubtype() : "").build().stream().collect(Collectors.joining(":"));
    }
}
