package com.thinkbiganalytics.calendar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Test;
import org.quartz.impl.calendar.HolidayCalendar;

import com.thinkbiganalytics.calendar.HolidayCalendarService;
import com.thinkbiganalytics.calendar.SimpleHolidayCalendarService;

public class SimpleHolidayCalendarServiceTest {
	
	@Test
	public void testLoad() {
		HolidayCalendarService service = new SimpleHolidayCalendarService("src/test/resources/calendars.json");
		HolidayCalendar cal = service.getCalendar("USA-standard");
		
		assertNotNull(cal);
		// The time will not be included if it falls on a holiday
		assertFalse(cal.isTimeIncluded(new DateTime(2016,02,15,0,0).getMillis()));
		assertTrue(cal.isTimeIncluded(new DateTime(2016,05,18,0,0).getMillis()));
	}

}
