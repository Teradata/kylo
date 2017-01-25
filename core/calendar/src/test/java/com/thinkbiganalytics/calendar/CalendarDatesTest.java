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

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Vector;

import static org.junit.Assert.assertTrue;

/**
 * Created by matthutton on 3/11/16.
 */
public class CalendarDatesTest {


    @Test
    public void testGetDates() throws Exception {

        HashSet<LocalDate> calDatesSet = new HashSet<>();
        calDatesSet.add(new LocalDate());
        CalendarDates calDates1 = new CalendarDates(calDatesSet);
        Assert.assertTrue(calDates1.getDates().size() == 1);

        Vector<String> datesList = new Vector<>();
        datesList.add("2010-12-01");
        CalendarDates calDates2 = new CalendarDates(datesList);
        Assert.assertTrue(calDates2.getDates().size() == 1);

        CalendarDates calDates = new CalendarDates();
        // should be immutable
        calDates.getDates().add(new LocalDate());
        Assert.assertTrue(calDates.getDates().size() == 0);

    }
}
