package com.thinkbiganalytics.calendar;

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
        assertNotNull(cal);

        System.out.println(cal);

        // The time will not be included if it falls on a holiday
        assertFalse(cal.isTimeIncluded(new DateTime(2016, 2, 15, 0, 0).getMillis()));
        assertTrue(cal.isTimeIncluded(new DateTime(2016, 5, 18, 0, 0).getMillis()));
        assertEquals(4, cal.getExcludedDates().size());
        cal.addExcludedDate(new DateTime(2016, 6, 18, 0, 0).toDate());
        assertEquals(5, cal.getExcludedDates().size());
        cal.removeExcludedDate(new DateTime(2016, 6, 18, 0, 0).toDate());
        assertTrue(cal.getExcludedDates().size() == 4);
        assertNotNull(cal.getNextIncludedTime(new DateTime(2016, 2, 15, 0, 0).getMillis()));
        assertNotNull(cal.clone());

        Map<String, CalendarDates> calMap = new HashMap<>();
        CalendarDates calendarDates = new CalendarDates();
        calendarDates.getDates().add(new LocalDate());
        calMap.put("test", calendarDates);
        service.addCalendarDates(calMap);
        assertEquals(2, service.getCalendars().size());

        assertNotNull(service.getCalendar(null));
        assertNotNull(service.getCalendar("test"));
        assertTrue(service.getCalendarNames().contains("test"));

    }

}
