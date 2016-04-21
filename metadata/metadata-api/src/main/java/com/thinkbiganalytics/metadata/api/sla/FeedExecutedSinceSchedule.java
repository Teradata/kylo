/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

import java.text.ParseException;

import org.quartz.CronExpression;

/**
 *
 * @author Sean Felten
 */
public class FeedExecutedSinceSchedule extends DependentFeed {

    private final CronExpression cronExpression;

    public FeedExecutedSinceSchedule(String feedName, String cronExpression) throws ParseException {
        super(feedName);
        this.cronExpression = new CronExpression(cronExpression);
    }
    
    public CronExpression getCronExpression() {
        return cronExpression;
    }
    
    @Override
    public String getDescription() {
        return "feed " + getFeedName() + " has executed since " + getCronExpression();
    }
}
