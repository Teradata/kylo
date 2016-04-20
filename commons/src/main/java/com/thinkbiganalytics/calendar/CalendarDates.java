package com.thinkbiganalytics.calendar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CalendarDates {
	
	private Set<LocalDate> dates;
	
	public CalendarDates() {
		this.dates = new HashSet<LocalDate>();
	}
	
	public CalendarDates(Set<LocalDate> dates){
		this.dates = dates;
	}
	
	@JsonCreator
	public CalendarDates(List<String> datelist) {
		this.dates = new HashSet<LocalDate>();
		if (dates != null) {
			for (String date: datelist) {
				dates.add(LocalDate.parse(date,ISODateTimeFormat.date()));
			}
		} 
	}

	public Set<LocalDate> getDates() {
		return new HashSet<LocalDate>(dates);
	}
}
