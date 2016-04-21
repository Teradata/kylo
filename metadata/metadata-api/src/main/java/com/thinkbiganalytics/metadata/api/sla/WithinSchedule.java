/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

import java.text.ParseException;

import org.joda.time.Period;
import org.quartz.CronExpression;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public class WithinSchedule implements Metric {

    private final CronExpression cronExpression;
    private final Period period;

    public WithinSchedule(String cronExpression, String period) throws ParseException {
        this.cronExpression = new CronExpression(cronExpression);
        this.period = Period.parse(period);
    }
    
    public WithinSchedule(CronExpression cronExpression, Period period) throws ParseException {
        this.cronExpression = cronExpression;
        this.period = period;
    }
    
    public CronExpression getCronExpression() {
        return cronExpression;
    }
    
    public Period getPeriod() {
        return period;
    }
    
    @Override
    public String getDescription() {
        return "current time is within schedule " + getCronExpression();
    }

}
