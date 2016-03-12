package com.thinkbiganalytics.calendar;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.quartz.impl.calendar.HolidayCalendar;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SimpleHolidayCalendarServiceTest {

    @Test
    public void testLoad() {
        HolidayCalendarService defaultService = new SimpleHolidayCalendarService();


        SimpleHolidayCalendarService s = new SimpleHolidayCalendarService();
        s.setFilename("missing");
        s.load();

        SimpleHolidayCalendarService service = new SimpleHolidayCalendarService("src/test/resources/calendars.json");
        HolidayCalendar cal = service.getCalendar("USA-standard");

        assertNotNull(cal);
        // The time will not be included if it falls on a holiday
        assertFalse(cal.isTimeIncluded(new DateTime(2016, 02, 15, 0, 0).getMillis()));
        assertTrue(cal.isTimeIncluded(new DateTime(2016, 05, 18, 0, 0).getMillis()));
        assertTrue(cal.getExcludedDates().size() == 4);
        cal.addExcludedDate(new DateTime(2016, 6, 18, 0, 0).toDate());
        assertTrue(cal.getExcludedDates().size() == 5);
        cal.removeExcludedDate(new DateTime(2016, 6, 18, 0, 0).toDate());
        assertTrue(cal.getExcludedDates().size() == 4);
        assertTrue(cal.getNextIncludedTime(new DateTime(2016, 02, 15, 0, 0).getMillis()) == 1455609600000L);
        assertNotNull(cal.clone());

        Map calMap = new HashMap();
        CalendarDates calendarDates = new CalendarDates();
        calendarDates.getDates().add(new LocalDate());
        calMap.put("test", calendarDates);
        service.addCalendarDates(calMap);
        assertTrue(service.getCalendars().size() == 3);

        assertNotNull(service.getCalendar(null));
        assertNotNull(service.getCalendar("test"));
        assertTrue(service.getCalendarNames().contains("test"));


    }

}
