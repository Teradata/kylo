/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

import java.beans.Transient;
import java.text.ParseException;

import org.joda.time.Period;
import org.quartz.CronExpression;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public class WithinSchedule implements Metric {

    private transient CronExpression cronExpression;
    private transient Period period;
    private String cronString;
    private String periodString;
    
    public WithinSchedule() {
    }

    public WithinSchedule(String cronExpression, String period) throws ParseException {
        this.cronExpression = new CronExpression(cronExpression);
        this.period = Period.parse(period);
        this.cronString = cronExpression;
        this.periodString = period;
    }
    
    public WithinSchedule(CronExpression cronExpression, Period period) throws ParseException {
        this.cronExpression = cronExpression;
        this.period = period;
        this.cronString = cronExpression.toString();
        this.periodString = period.toString();
    }
    
    public CronExpression getCronExpression() {
        return cronExpression;
    }
    
    public Period getPeriod() {
        return period;
    }
    
    @Override
    @Transient
    public String getDescription() {
        return "current time is within schedule " + getCronExpression();
    }

    protected String getCronString() {
        return cronString;
    }

    protected void setCronString(String cronString) {
        this.cronString = cronString;
    }

    protected String getPeriodString() {
        return periodString;
    }

    protected void setPeriodString(String periodString) {
        this.periodString = periodString;
    }

    
}
