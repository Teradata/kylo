/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

import java.text.ParseException;

/**
 *
 * @author Sean Felten
 */
public class FeedExecutedSinceScheduleMetric extends DependentFeedMetric {

    private String cronExpression;

    public FeedExecutedSinceScheduleMetric(String feedName, String cronExpression) throws ParseException {
        super(feedName);
        this.cronExpression = cronExpression;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
}
