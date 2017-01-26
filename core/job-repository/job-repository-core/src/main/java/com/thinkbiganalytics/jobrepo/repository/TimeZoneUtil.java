package com.thinkbiganalytics.jobrepo.repository;

/*-
 * #%L
 * thinkbig-job-repository-core
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

import org.joda.time.DateTimeZone;

import java.util.Date;
import java.util.TimeZone;

/**
 * Created by sr186054 on 4/20/16.
 */
public class TimeZoneUtil {

    public static final TimeZone utcTZ = TimeZone.getTimeZone("UTC");

    public static long toLocalTime(long time, TimeZone to) {
        return convertTime(time, utcTZ, to);
    }

    public static long toUTC(long time, TimeZone from) {
        return convertTime(time, from, utcTZ);
    }

    public static long convertTime(long time, TimeZone from, TimeZone to) {
        return time + getTimeZoneOffset(time, from, to);
    }

    private static long getTimeZoneOffset(long time, TimeZone from, TimeZone to) {
        int fromOffset = from.getOffset(time);
        int toOffset = to.getOffset(time);
        int diff = 0;

        if (fromOffset >= 0) {
            if (toOffset > 0) {
                toOffset = -1 * toOffset;
            } else {
                toOffset = Math.abs(toOffset);
            }
            diff = (fromOffset + toOffset) * -1;
        } else {
            if (toOffset <= 0) {
                toOffset = -1 * Math.abs(toOffset);
            }
            diff = (Math.abs(fromOffset) + toOffset);
        }
        return diff;
    }

    public static Date convertToUTC(Date date) {
        DateTimeZone tz = DateTimeZone.getDefault();
        Date utc = new Date(tz.convertLocalToUTC(date.getTime(), false));
        return utc;
    }

    public static Date getUTCTime() {
        return convertToUTC(new Date());
    }


}
