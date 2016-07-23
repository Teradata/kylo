package com.thinkbiganalytics.calendar;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.spring.FileResourceService;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.quartz.impl.calendar.HolidayCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
public class SimpleHolidayCalendarService implements HolidayCalendarService{

    private static final Logger LOG = LoggerFactory.getLogger(SimpleHolidayCalendarService.class);

    private static final HolidayCalendar DEFAULT_CALENDAR = new HolidayCalendar();

    private Map<String, HolidayCalendar> calendars;

    @Inject
   private FileResourceService fileResourceService;

    @Value("${holiday.calendars:#{null}}")
    private String filename;

    public SimpleHolidayCalendarService() {
        super();
        this.calendars = Collections.synchronizedMap(new HashMap<String, HolidayCalendar>());
    }

    public SimpleHolidayCalendarService(String filename) {
        this();
        this.filename = filename;
        load();
    }
    
    public SimpleHolidayCalendarService(Map<String, CalendarDates> dates) {
        addCalendarDates(dates);
    }
    
    protected void addCalendarDates(Map<String, CalendarDates> dates) {
        synchronized (this.calendars) {
            for (Entry<String, CalendarDates> calEntry : dates.entrySet()) {
                HolidayCalendar cal = new HolidayCalendar();

                for (LocalDate date : calEntry.getValue().getDates()) {
                    cal.addExcludedDate(date.toDate());
                }

                this.calendars.put(calEntry.getKey(), cal);
            }
        }
    }

    @PostConstruct
    protected void load() {
        File file = null;
        try {
        if(StringUtils.isNotBlank(filename)) {
            file = new File(filename);
            ObjectMapper mapper = new ObjectMapper();

            Map<String, CalendarDates> map = mapper.readValue(file, new TypeReference<Map<String, CalendarDates>>() {
            });
            addCalendarDates(map);
        }
        } catch (IOException e) {
            LOG.error("Could not load calendars", e);
        }
    }
    
    @Override
    public Map<String, HolidayCalendar> getCalendars() {
        synchronized (this.calendars) {
            return new HashMap<>(this.calendars);
        }
    }

    @Override
    public HolidayCalendar getCalendar(String name) {
        HolidayCalendar cal = calendars.get(name);
        
        if (cal != null) {
            return cal;
        } else {
            return DEFAULT_CALENDAR;
        }
    }

    @Override
    public Set<String> getCalendarNames() {
        synchronized (this.calendars) {
            return new HashSet<>(this.calendars.keySet());
        }
    }

    /**
     * Set the path to the calendar
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public FileResourceService getFileResourceService() {
        return fileResourceService;
    }

    public void setFileResourceService(FileResourceService fileResourceService) {
        this.fileResourceService = fileResourceService;
    }
}
