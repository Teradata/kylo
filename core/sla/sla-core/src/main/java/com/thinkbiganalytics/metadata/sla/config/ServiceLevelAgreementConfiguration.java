package com.thinkbiganalytics.metadata.sla.config;

import com.thinkbiganalytics.calendar.HolidayCalendarService;
import com.thinkbiganalytics.calendar.SimpleHolidayCalendarService;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;
import com.thinkbiganalytics.metadata.sla.spi.core.SimpleServiceLevelAssessor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceLevelAgreementConfiguration {


    @Bean
    public HolidayCalendarService calendarService(@Value("${holiday.calendars:#{null}}") String filename) {
        return new SimpleHolidayCalendarService(filename);
    }


    @Bean(name = "slaAssessor")
    public ServiceLevelAssessor slaAssessor() {
        return new SimpleServiceLevelAssessor();
    }


}
