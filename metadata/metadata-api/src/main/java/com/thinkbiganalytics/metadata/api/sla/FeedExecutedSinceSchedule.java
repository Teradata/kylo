/**
 * 
 */
package com.thinkbiganalytics.metadata.api.sla;


import org.quartz.CronExpression;

import java.beans.Transient;
import java.text.ParseException;

/**
 *
 * @author Sean Felten
 */
public class FeedExecutedSinceSchedule extends DependentFeed {

    private transient CronExpression cronExpression;
    private String cronString;
    
    public FeedExecutedSinceSchedule() {
    }

    public FeedExecutedSinceSchedule(String categoryAndFeed, String cronStr) throws ParseException {
        super(categoryAndFeed);
        this.cronExpression = new CronExpression(cronStr);
        this.cronString = cronStr;
    }

    public FeedExecutedSinceSchedule(String categoryName, String feedName, String cronStr) throws ParseException {
        super(categoryName, feedName);
        this.cronExpression = new CronExpression(cronStr);
        this.cronString = cronStr;
    }

    public FeedExecutedSinceSchedule(String categoryName, String datasetName, CronExpression cronExpression) throws ParseException {
        super(categoryName, datasetName);
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
