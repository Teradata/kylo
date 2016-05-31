/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;

import java.beans.Transient;
import java.text.ParseException;

import org.quartz.CronExpression;

/**
 *
 * @author Sean Felten
 */
public class FeedExecutedSinceSchedule extends DependentFeed {

    private transient CronExpression cronExpression;
    private String cronString;
    
    public FeedExecutedSinceSchedule() {
    }

    public FeedExecutedSinceSchedule(String feedName, String cronStr) throws ParseException {
        super(feedName);
        this.cronExpression = new CronExpression(cronStr);
        this.cronString = cronStr;
    }
    
    public FeedExecutedSinceSchedule(String datasetName, CronExpression cronExpression) throws ParseException {
        super(datasetName);
        this.cronExpression = cronExpression;
        this.cronString = cronExpression.toString();
    }
    
    public CronExpression getCronExpression() {
        return cronExpression;
    }
    
    @Override
    @Transient
    public String getDescription() {
        return "feed " + getFeedName() + " has executed since " + getCronExpression();
    }
    
    protected String getCronString() {
        return cronString;
    }
    
    protected void setCronString(String cronString) {
        this.cronString = cronString;
        try {
            this.cronExpression = new CronExpression(cronString);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
