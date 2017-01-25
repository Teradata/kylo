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

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by matthutton on 7/26/15.
 */
public class RepositoryUtils {

    /**
     * Corrects for timezone. The date-time is stored in MYSQL as UTC (without encoding timezone) and interpreted by JOOQ as local time
     */
    public static DateTime utcDateTimeCorrection(DateTime utcDate) {
        DateTime newUTC = null;
        if (utcDate == null) {
            newUTC = new DateTime();
        } else {
            newUTC = utcDate;
/*
            String isoDate = utcDate.toDateTimeISO().toString();
            if (!isoDate.endsWith("Z")) {
            	// Strip the timezone information and replace it
                int tzIndex = isoDate.lastIndexOf("-");
                isoDate = isoDate.substring(0, tzIndex-1)+"-00:00";
            }
            org.joda.time.format.DateTimeFormatter parser    = ISODateTimeFormat.dateTimeParser();
            newUTC = parser.parseDateTime(isoDate);
*/
        }
        return newUTC;
    }

    public static DateTime utcDateTimeCorrection(Date utcDate) {
        if (utcDate != null) {
            return utcDateTimeCorrection(new DateTime(utcDate));
        } else {
            return utcDateTimeCorrection((DateTime) null);
        }
    }


}
