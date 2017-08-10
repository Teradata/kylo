package com.thinkbiganalytics.metadata.jpa.alerts;

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
import com.thinkbiganalytics.alerts.api.AlertSummary;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;

import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/3/17.
 */
public class DefaultAlertSummary implements AlertSummary{

    private String type;

    private String subtype;

    private Alert.Level level;
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

    public String getGroupByKey(){
        return new ImmutableList.Builder<String>()
            .add(getType())
            .add(getSubtype()).build().stream().collect(Collectors.joining(":"));
    }
}
