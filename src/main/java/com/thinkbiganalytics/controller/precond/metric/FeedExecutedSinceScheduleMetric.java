/**
 * 
 */
package com.thinkbiganalytics.controller.precond.metric;

import java.text.ParseException;

import org.quartz.CronExpression;

/**
 *
 * @author Sean Felten
 */
public class FeedExecutedSinceScheduleMetric extends DependentDatasetMetric {

    private CronExpression cronExpression;

    public FeedExecutedSinceScheduleMetric(String feedName, String cronExpression) throws ParseException {
        super(feedName);
        this.cronExpression = new CronExpression(cronExpression);
    }
    
    public CronExpression getCronExpression() {
        return cronExpression;
    }
    
    @Override
    public String getDescription() {
        return "dataset " + getDatasetName() + " has been updated since " + getCronExpression();
    }
}
