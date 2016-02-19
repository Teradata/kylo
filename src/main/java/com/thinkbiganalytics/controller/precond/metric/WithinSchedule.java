/**
 * 
 */
package com.thinkbiganalytics.controller.precond.metric;

import java.text.ParseException;

import org.quartz.CronExpression;

import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public class WithinSchedule implements Metric {

    private CronExpression cronExpression;

    public WithinSchedule(String cronExpression) throws ParseException {
        this.cronExpression = new CronExpression(cronExpression);
    }
    
    public CronExpression getCronExpression() {
        return cronExpression;
    }
    
    @Override
    public String getDescription() {
        return "current time is within schedule " + getCronExpression();
    }

}
