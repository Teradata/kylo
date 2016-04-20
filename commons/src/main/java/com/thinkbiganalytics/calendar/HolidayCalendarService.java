package com.thinkbiganalytics.calendar;

import java.util.Map;
import java.util.Set;

import org.quartz.impl.calendar.HolidayCalendar;

public interface HolidayCalendarService {
	
	Map<String, HolidayCalendar> getCalendars();
	
	HolidayCalendar getCalendar(String name);
	
	Set<String> getCalendarNames();
}
