/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed.precond;

import java.text.ParseException;

import org.quartz.CronExpression;

/**
 *
 * @author Sean Felten
 */
public class FeedExecutedSinceScheduleMetric extends DependentFeedMetric {

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
        return "feed " + getFeedName() + " has executed since " + getCronExpression();
    }
}
