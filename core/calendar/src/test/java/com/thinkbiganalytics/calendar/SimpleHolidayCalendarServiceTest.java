package com.thinkbiganalytics.calendar;

/*-
 * #%L
 * thinkbig-calendar
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
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.quartz.impl.calendar.HolidayCalendar;

import java.util.*;

import static org.junit.Assert.*;

public class SimpleHolidayCalendarServiceTest {

    private Map<String, CalendarDates> calendarDatesMap;

    @Before
    public void setUpSimpleHolidayCalendarService() throws Exception {

        List<String> datesList = Arrays.asList("2016-01-01", "2016-01-18", "2016-02-15", "2016-05-30");

        calendarDatesMap = new HashMap<>();
        calendarDatesMap.put("USA-standard", new CalendarDates(datesList));
    }

    @Test
    public void testAll() {

        SimpleHolidayCalendarService service = new SimpleHolidayCalendarService();
        service.setFilename("missing");
        service.load();

        // read from file
        service = new SimpleHolidayCalendarService("src/test/resources/calendars.json");
        Assert.assertNotNull(service);

        // read from hash map
        service = new SimpleHolidayCalendarService();
        service.addCalendarDates(calendarDatesMap);

        Set<String> calNames = service.getCalendarNames();
        Assert.assertTrue(calNames.contains("USA-standard"));

        // get calendar
        HolidayCalendar cal = service.getCalendar("USA-standard");
        Assert.assertNotNull(cal);

        System.out.println(cal);

        // The time will not be included if it falls on a holiday
        Assert.assertFalse(cal.isTimeIncluded(new DateTime(2016, 2, 15, 0, 0).getMillis()));
        Assert.assertTrue(cal.isTimeIncluded(new DateTime(2016, 5, 18, 0, 0).getMillis()));
        Assert.assertEquals(4, cal.getExcludedDates().size());
        cal.addExcludedDate(new DateTime(2016, 6, 18, 0, 0).toDate());
        Assert.assertEquals(5, cal.getExcludedDates().size());
        cal.removeExcludedDate(new DateTime(2016, 6, 18, 0, 0).toDate());
        Assert.assertTrue(cal.getExcludedDates().size() == 4);
        Assert.assertNotNull(cal.getNextIncludedTime(new DateTime(2016, 2, 15, 0, 0).getMillis()));
        Assert.assertNotNull(cal.clone());

        Map<String, CalendarDates> calMap = new HashMap<>();
        CalendarDates calendarDates = new CalendarDates();
        calendarDates.getDates().add(new LocalDate());
        calMap.put("test", calendarDates);
        service.addCalendarDates(calMap);
        assertEquals(2, service.getCalendars().size());

        Assert.assertNotNull(service.getCalendar(null));
        Assert.assertNotNull(service.getCalendar("test"));
        Assert.assertTrue(service.getCalendarNames().contains("test"));

    }

}
