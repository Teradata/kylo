package com.thinkbiganalytics.calendar;

import org.joda.time.LocalDate;
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
        assertTrue(calDates1.getDates().size() == 1);

        Vector<String> datesList = new Vector<>();
        datesList.add("2010-12-01");
        CalendarDates calDates2 = new CalendarDates(datesList);
        assertTrue(calDates2.getDates().size()  == 1);

        CalendarDates calDates = new CalendarDates();
        // should be immutable
        calDates.getDates().add(new LocalDate());
        assertTrue(calDates.getDates().size() == 0);

    }
}