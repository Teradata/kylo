/**
 *
 */
package com.thinkbiganalytics.metadata.api.sla;

/*-
 * #%L
 * thinkbig-metadata-api
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

import com.thinkbiganalytics.metadata.sla.api.Metric;

import org.joda.time.Period;
import org.quartz.CronExpression;

import java.beans.Transient;
import java.text.ParseException;

/**
 *
 */
public class WithinSchedule implements Metric {

    private transient CronExpression cronExpression;
    private transient Period period;
    private String cronString;
    private String periodString;

    public WithinSchedule() {
    }

    public WithinSchedule(String cronExpression, String period) throws ParseException {
        this.cronExpression = new CronExpression(cronExpression);
        this.period = Period.parse(period);
        this.cronString = cronExpression;
        this.periodString = period;
    }

    public WithinSchedule(CronExpression cronExpression, Period period) throws ParseException {
        this.cronExpression = cronExpression;
        this.period = period;
        this.cronString = cronExpression.toString();
        this.periodString = period.toString();
    }

    public CronExpression getCronExpression() {
        return cronExpression;
    }

    public Period getPeriod() {
        return period;
    }

    @Override
    @Transient
    public String getDescription() {
        return "current time is within schedule " + getCronExpression();
    }

    protected String getCronString() {
        return cronString;
    }

    protected void setCronString(String cronString) {
        this.cronString = cronString;
    }

    protected String getPeriodString() {
        return periodString;
    }

    protected void setPeriodString(String periodString) {
        this.periodString = periodString;
    }


}
