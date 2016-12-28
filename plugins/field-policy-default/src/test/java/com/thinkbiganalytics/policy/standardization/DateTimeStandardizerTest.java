/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.standardization;

import org.junit.Before;
import org.junit.Test;

import static com.thinkbiganalytics.policy.standardization.DateTimeStandardizer.OutputFormats;
import static org.junit.Assert.assertEquals;

/**
 * Created by matthutton on 5/6/16.
 */
public class DateTimeStandardizerTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testUnixTimestamp(){
        String unixTime = "1466627584"; // time in seconds (no millis)
        DateTimeStandardizer unixTimestampStandardizer = new DateTimeStandardizer(null, OutputFormats.DATETIME, null, "PST");
        String convertedValue = unixTimestampStandardizer.convertValue(unixTime);
        assertEquals("2016-06-22 13:33:04.000",convertedValue);
        int i = 0;
    }

    @Test
    public void testEpochConversion() throws Exception {
        String testTime = "1462573974495";
        DateTimeStandardizer epochStandardizer = new DateTimeStandardizer(null, OutputFormats.DATETIME, null, "PST");
        String convertedTime = epochStandardizer.convertValue(testTime);
        assertEquals("2016-05-06 15:32:54.495", convertedTime);

        epochStandardizer = new DateTimeStandardizer(null, OutputFormats.DATETIME, null, "UTC");
        convertedTime = epochStandardizer.convertValue(testTime);
        assertEquals("2016-05-06 22:32:54.495", convertedTime);

        epochStandardizer = new DateTimeStandardizer(null, OutputFormats.DATE_ONLY, null, "UTC");
        convertedTime = epochStandardizer.convertValue(testTime);
        assertEquals("2016-05-06", convertedTime);

        epochStandardizer = new DateTimeStandardizer(null, OutputFormats.DATETIME_NOMILLIS, null, "UTC");
        convertedTime = epochStandardizer.convertValue(testTime);
        assertEquals("2016-05-06 22:32:54", convertedTime);
    }

    @Test
    public void testSimpleDateConversion() throws Exception {
        DateTimeStandardizer standardizer = new DateTimeStandardizer("MM/dd/YYYY", OutputFormats.DATE_ONLY);
        assertEquals("1974-01-14", standardizer.convertValue("1/14/1974"));
        assertEquals("2014-12-01", standardizer.convertValue("12/01/2014"));

        standardizer = new DateTimeStandardizer("MM-dd-YYYY", OutputFormats.DATE_ONLY);
        assertEquals("1974-01-14", standardizer.convertValue("1-14-1974"));
        assertEquals("2014-12-01", standardizer.convertValue("12-01-2014"));
    }

    @Test
    public void testDateTimeConversion() throws Exception {
        DateTimeStandardizer
            standardizer =
            new DateTimeStandardizer("MM/dd/YYYY HH:mm:ss", OutputFormats.DATETIME_NOMILLIS, "UTC", "PST");
        assertEquals("2016-05-06 15:32:54", standardizer.convertValue("05/06/2016 22:32:54"));
    }

    @Test
    public void testIsoConversion() throws Exception {
        DateTimeStandardizer
            standardizer =
            new DateTimeStandardizer("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'", OutputFormats.DATETIME_NOMILLIS);
        assertEquals("2004-10-19 07:00:00", standardizer.convertValue("2004-10-19T07:00:00.000Z"));


    }

    @Test
    public void testDateTimeConversion1() throws Exception {
        DateTimeStandardizer
            standardizer =
            new DateTimeStandardizer("MM-dd-YYYYHH:mm", OutputFormats.DATE_ONLY);
        assertEquals("2016-07-25", standardizer.convertValue("07-25-201617:18"));
    }


}