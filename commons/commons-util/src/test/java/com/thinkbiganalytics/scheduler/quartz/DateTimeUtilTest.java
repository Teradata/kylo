package com.thinkbiganalytics.scheduler.quartz;

import com.thinkbiganalytics.DateTimeUtil;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * Created by Jeremy Merrifield on 9/30/16.
 */
public class DateTimeUtilTest {

    @Test
    public void testGetNowUTCTime() {
        long startTime = 1475259939569L;
        DateTimeUtils.setCurrentMillisFixed(startTime);
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/Los_Angeles")));

        DateTime jerDate = new DateTime(startTime);
        DateTimeZone dtZone = DateTimeZone.forID("UTC");
        DateTime dtus = jerDate.withZone(dtZone);
        Date jerDateResult = dtus.toLocalDate().toDate();

        DateTime localDateTime = new DateTime(startTime);

        DateTimeZone tz = DateTimeZone.getDefault();
        DateTime utc = new DateTime(tz.convertLocalToUTC(startTime, false));

        DateTime timeToTest = DateTimeUtil.getNowUTCTime();
        System.out.println(timeToTest.toString());
        assertTrue(utc.equals(timeToTest));
    }

}
