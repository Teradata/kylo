package com.thinkbiganalytics.scheduler.util;

/*-
 * #%L
 * thinkbig-commons-util
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

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Generic Cron Utility for calculating Cron Dates both Previous and Next Occurrences
 */
public class CronExpressionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CronExpressionUtil.class);

    public static Long getCronInterval(String cronExpression) throws ParseException {
        return getCronInterval(new CronExpression(cronExpression));
    }

    public static Long getCronInterval(CronExpression cron) {
        Date nextValidTime = getNextFireTime(cron);
        Date subsequentNextValidTime = getNextFireTime(nextValidTime, cron);
        long interval = subsequentNextValidTime.getTime() - nextValidTime.getTime();
        return interval;
    }

    public static Date getPreviousFireTime(String cronExpression) throws ParseException {
        return getPreviousFireTime(new CronExpression(cronExpression));
    }

    public static Date getPreviousFireTime(CronExpression cron) {
        Long interval = getCronInterval(cron);
        Date nextValidTime = getNextFireTime(cron);
        return new Date(nextValidTime.getTime() - interval);
    }

    public static Date getPreviousFireTime(CronExpression cron, int previousNumber) {
        if (previousNumber <= 0) {
            previousNumber = 1;
        }
        List<Date> dates = getPreviousFireTimes(cron, previousNumber);
        return dates.get(previousNumber - 1);

    }

    public static Date getPreviousFireTime(Date lastFireTime, String cronExpression) throws ParseException {
        return getPreviousFireTime(lastFireTime, new CronExpression(cronExpression));
    }

    public static Date getPreviousFireTime(Date lastFireTime, CronExpression cron) {
        Long interval = getCronInterval(cron);
        Date nextValidTime = getNextFireTime(lastFireTime, cron);
        return new Date(nextValidTime.getTime() - interval);
    }


    public static Date getNextFireTime(String cronExpression) throws ParseException {
        return getNextFireTime(new CronExpression(cronExpression));
    }

    public static Date getNextFireTime(CronExpression cron) {
        return cron.getNextValidTimeAfter(new Date());
    }

    public static Date getNextFireTime(Date lastFireTime, String cronExpression) throws ParseException {
        return getNextFireTime(lastFireTime, new CronExpression(cronExpression));
    }

    public static Date getNextFireTime(Date lastFireTime, CronExpression cron) {
        return cron.getNextValidTimeAfter(lastFireTime);
    }

    public static List<Date> getNextFireTimes(String cronExpression, Integer count) throws ParseException {
        return getNextFireTimes(new CronExpression(cronExpression), count);
    }

    public static List<Date> getNextFireTimes(CronExpression cron, Integer count) throws ParseException {
        List<Date> dates = new ArrayList<Date>();
        Date lastDate = new Date();
        for (int i = 0; i < count; i++) {
            Date nextDate = cron.getNextValidTimeAfter(lastDate);
            dates.add(nextDate);
            lastDate = nextDate;
        }
        return dates;
    }

    public static List<Date> getPreviousFireTimes(String cronExpression, Integer count) throws ParseException {
        return getPreviousFireTimes(new CronExpression(cronExpression), count);
    }

    public static List<Date> getPreviousFireTimes(CronExpression cron, Integer count) {
        List<Date> dates = new ArrayList<Date>();
        Long interval = getCronInterval(cron);
        Date nextFireTime = getNextFireTime(cron);
        for (int i = 0; i < count; i++) {
            Date previous = new Date(nextFireTime.getTime() - interval);
            dates.add(previous);
            nextFireTime = previous;
        }
        return dates;
    }

    public static CronExpression timerToCronExpression(String timer) throws ParseException {
        return TimerToCronExpression.timerToCronExpression(timer);
    }

    public static String timerToCronString(String timer) {
        try {
            CronExpression cronExpression = timerToCronExpression(timer);
            return cronExpression.getCronExpression();
        } catch (ParseException e) {
            LOG.error("Unable to create CronExpression from timer {}.  Error: {}", timer, e.getMessage(), e);
        }
        return null;
    }

}
