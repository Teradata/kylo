package com.thinkbiganalytics.metadata.cache.util;

/*-
 * #%L
 * thinkbig-job-repository-controller
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

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 9/27/17.
 */
public class TimeUtil {

    public static Long getTimeNearestSecond() {
        //get the DateTime trimmed to the nearest second
        return new DateTime().withMillisOfSecond(0).getMillis();
    }

    public static Long getTimeNearestFiveSeconds() {
        DateTime dt = new DateTime().withMillisOfSecond(0);
        int seconds = dt.getSecondOfMinute();
        if (seconds % 5 > 0) {
            dt = dt.withSecondOfMinute(seconds - (seconds % 5));
        }
        return dt.getMillis();

    }
}
