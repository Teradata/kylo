package com.thinkbiganalytics.jobrepo.nifi.support;

import org.joda.time.DateTimeZone;

import java.util.Date;

/**
 * Created by sr186054 on 2/29/16.
 */
public class DateTimeUtil {

    public static Date convertToUTC(Date date) {
        DateTimeZone tz = DateTimeZone.getDefault();
        Date utc = new Date(tz.convertLocalToUTC(date.getTime(), false));
        return utc;
    }

    public static Date getUTCTime() {
        return convertToUTC(new Date());
    }
}
