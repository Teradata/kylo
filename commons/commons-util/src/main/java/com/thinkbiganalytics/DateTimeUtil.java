package com.thinkbiganalytics;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;

/**
 * Created by sr186054 on 2/29/16.
 */
public class DateTimeUtil {

    public static Date convertToUTC(Date date) {
        DateTime time = new DateTime(date.getTime());
        DateTimeZone dtZone = DateTimeZone.forID("UTC");
        DateTime utc = time.withZone(dtZone);
        return utc.toDate();
    }

    public static DateTime convertToUTC(DateTime date) {
        DateTimeZone dtZone = DateTimeZone.forID("UTC");
        DateTime utc = date.withZone(dtZone);
        return new DateTime(utc);
    }

    public static Date getUTCTime() {
        return convertToUTC(new Date());
    }

    public static DateTime getNowUTCTime() {
        return convertToUTC(DateTime.now());
    }
}
