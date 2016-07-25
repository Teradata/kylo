package com.thinkbiganalytics.metadata.sla.config;

import com.thinkbiganalytics.calendar.HolidayCalendarService;
import com.thinkbiganalytics.calendar.SimpleHolidayCalendarService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceLevelAgreementConfiguration {


    @Bean
    public HolidayCalendarService calendarService(@Value("${holiday.calendars:#{null}}") String filename) {
        return new SimpleHolidayCalendarService(filename);
    }

}
