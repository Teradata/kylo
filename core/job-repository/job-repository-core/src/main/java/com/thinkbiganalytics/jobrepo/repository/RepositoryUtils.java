package com.thinkbiganalytics.jobrepo.repository;

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
