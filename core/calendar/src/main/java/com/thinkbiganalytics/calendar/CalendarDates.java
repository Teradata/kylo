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
