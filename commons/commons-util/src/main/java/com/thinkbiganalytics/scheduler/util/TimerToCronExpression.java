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

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.Seconds;
import org.quartz.CronExpression;

import java.text.ParseException;

/**
 * Converts a simple timer string such as  5 sec, 200 min, 50 days and converts it to a valid cron Expression
 */
public class TimerToCronExpression {

    /**
     * Parse a timer string to a Joda time period
     *
     * @param timer      a string indicating a time unit (i.e. 5 sec)
     * @param periodType the Period unit to use.
     */
    public static Period timerStringToPeriod(String timer, PeriodType periodType) {
        String cronString = null;
        Integer time = Integer.parseInt(StringUtils.substringBefore(timer, " "));
        String units = StringUtils.substringAfter(timer, " ").toLowerCase();
        //time to years,days,months,hours,min, sec
        Integer days = 0;
        Integer hours = 0;
        Integer min = 0;
        Integer sec = 0;
        Period p = null;
        if (units.startsWith("sec")) {
            p = Period.seconds(time);
        } else if (units.startsWith("min")) {
            p = Period.minutes(time);
        } else if (units.startsWith("hr") || units.startsWith("hour")) {
            p = Period.hours(time);
        } else if (units.startsWith("day")) {
            p = Period.days(time);
        }
        if (periodType != null) {
            p = p.normalizedStandard(periodType);
        } else {
        }
        return p;
    }

    /**
     * Parse a timer string to a Joda time period
     */
    public static Period timerStringToPeriod(String timer) {
        return timerStringToPeriod(timer, PeriodType.dayTime());
    }

    /**
     * Pass in a time String with a single unit the Max Unit is days (sec or secs, min or mins, hrs or hours, day or days) Examples:  5 sec, 100 min, 30 days,  20 hrs
     */
    public static CronExpression timerToCronExpression(String timer) throws ParseException {

        Period p = timerStringToPeriod(timer);
        if (p != null) {
            String cron = getSecondsCron(p) + " " + getMinutesCron(p) + " " + getHoursCron(p) + " " + getDaysCron(p) + " * ? *";
            return new CronExpression(cron);
        }
        return null;

    }

    private static String getSecondsCron(Period p) {
        Integer sec = p.getSeconds();
        Seconds s = p.toStandardSeconds();
        Integer seconds = s.getSeconds();
        String str = "0" + (sec > 0 ? "/" + sec : "");
        if (seconds > 60) {
            str = sec + "";
        }
        return str;
    }

    private static String getMinutesCron(Period p) {
        Integer min = p.getMinutes();
        Minutes m = p.toStandardMinutes();
        Integer minutes = m.getMinutes();
        String str = "0" + (min > 0 ? "/" + min : "");
        if (minutes > 60) {
            str = min + "";
        }
        return str;
    }

    private static String getHoursCron(Period p) {
        Integer hrs = p.getHours();
        Hours h = p.toStandardHours();
        Integer hours = h.getHours();
        String str = "0" + (hrs > 0 ? "/" + hrs : "");
        if (hours > 24) {
            str = hrs + "";
        }
        return str;
    }

    private static String getDaysCron(Period p) {
        Integer days = p.getDays();
        String str = "1" + (days > 0 ? "/" + days : "/1");
        return str;
    }


}
