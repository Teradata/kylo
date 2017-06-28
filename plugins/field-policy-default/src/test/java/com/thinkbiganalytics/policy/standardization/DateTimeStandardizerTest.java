package com.thinkbiganalytics.policy.standardization;

/*-
 * #%L
 * thinkbig-field-policy-default
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

import org.junit.Before;
import org.junit.Test;

import static com.thinkbiganalytics.policy.standardization.DateTimeStandardizer.OutputFormats;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the {@link DateTimeStandardizer}
 */
public class DateTimeStandardizerTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testUnixTimestamp() {
        String unixTime = "1466627584"; // time in seconds (no millis)
        DateTimeStandardizer unixTimestampStandardizer = new DateTimeStandardizer(null, OutputFormats.DATETIME, null, "PST");
        String convertedValue = unixTimestampStandardizer.convertValue(unixTime);
        assertEquals("2016-06-22 13:33:04.000", convertedValue);
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

    @Test
    public void testAcceptValidType() {
        DateTimeStandardizer standardizer = new DateTimeStandardizer("YYYY-MM-dd", OutputFormats.DATE_ONLY);
        assertTrue(standardizer.accepts("2017-03-28"));
    }

    @Test
    public void testAcceptInvalidType() {
        DateTimeStandardizer standardizer = new DateTimeStandardizer("YYYY-MM-dd", OutputFormats.DATE_ONLY);
        Double doubleValue = 1000.05d;
        assertFalse(standardizer.accepts(doubleValue));
    }

    @Test
    public void testConvertRawValueValidType() {
        Object expectedValue = "2016-05-06 15:32:54";
        Object rawValue = "05/06/2016 22:32:54";
        DateTimeStandardizer
            standardizer =
            new DateTimeStandardizer("MM/dd/YYYY HH:mm:ss", OutputFormats.DATETIME_NOMILLIS, "UTC", "PST");
        assertEquals(expectedValue, standardizer.convertRawValue(rawValue));
    }

    @Test
    public void testConvertRawValueInvalidType() {
        Object expectedValue = Double.valueOf("1000.05");
        Object rawValue = Double.valueOf("1000.05");
        DateTimeStandardizer
            standardizer =
            new DateTimeStandardizer("MM/dd/YYYY HH:mm:ss", OutputFormats.DATETIME_NOMILLIS, "UTC", "PST");
        assertEquals(expectedValue, standardizer.convertRawValue(rawValue));
    }

    @Test
    public void testIdenticalResults() {
        DateTimeStandardizer
            standardizer =
            new DateTimeStandardizer("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'", OutputFormats.DATETIME_NOMILLIS);
        Object rawValueObj = "2004-10-19T07:00:00.000Z";
        Object expectedValueObj = "2004-10-19 07:00:00";
        String rawValueStr = "2004-10-19T07:00:00.000Z";
        String expectedValueStr = "2004-10-19 07:00:00";
        assertEquals(standardizer.convertValue(rawValueStr), standardizer.convertRawValue(rawValueObj).toString());
        assertEquals(standardizer.convertValue(rawValueStr), expectedValueStr);
        assertEquals(standardizer.convertRawValue(rawValueObj), expectedValueObj);
    }

}
