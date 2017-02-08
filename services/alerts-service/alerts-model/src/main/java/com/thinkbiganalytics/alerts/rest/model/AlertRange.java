package com.thinkbiganalytics.alerts.rest.model;

/*-
 * #%L
 * thinkbig-alerts-model
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
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the range of alerts as a result of a query.  There are also attributes for
 * the result size and the alert IDs that bound the result.
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertRange {

    /**
     * Time of the newest alert in the results
     */
    private DateTime newestTime;

    /**
     * Time of the oldest alert in the results
     */
    private DateTime oldestTime;

    /**
     * Number of alerts in the results
     */
    private int size;

    /**
     * The list of alerts
     */
    private List<Alert> alerts;

    public AlertRange() {
    }

    public AlertRange(List<Alert> alerts) {
        assert alerts != null;

        if (alerts.size() > 0) {
            this.newestTime = alerts.get(0).getCreatedTime();
            this.oldestTime = alerts.get(alerts.size() - 1).getCreatedTime();
        } else {
            this.newestTime = null;
            this.newestTime = null;
        }

        this.alerts = new ArrayList<>(alerts);
        this.size = alerts.size();
    }

    public DateTime getNewestTime() {
        return newestTime;
    }

    public void setNewestTime(DateTime firstTime) {
        this.newestTime = firstTime;
    }

    public DateTime getOldestTime() {
        return oldestTime;
    }

    public void setOldestTime(DateTime lastTime) {
        this.oldestTime = lastTime;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts;
    }

}
