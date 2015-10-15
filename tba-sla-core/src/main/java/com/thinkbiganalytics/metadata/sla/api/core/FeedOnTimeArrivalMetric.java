/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api.core;

import org.joda.time.Period;
import org.quartz.CronExpression;

import com.google.common.base.MoreObjects;
import com.thinkbiganalytics.metadata.sla.api.Metric;

/**
 *
 * @author Sean Felten
 */
public class FeedOnTimeArrivalMetric implements Metric {
    
    private String feedName;
    private CronExpression expectedExpression;
    private Period latePeriod;
    private Period asOfPeriod;
    private String calendarName;
    

    /**
     * 
     */
    public FeedOnTimeArrivalMetric() {
    }
    

    public FeedOnTimeArrivalMetric(String feedName, 
                                   CronExpression expectedExpression, 
                                   Period latePeriod,
                                   Period asOfPeriod, 
                                   String clalendarName) {
        super();
        this.feedName = feedName;
        this.expectedExpression = expectedExpression;
        this.latePeriod = latePeriod;
        this.asOfPeriod = asOfPeriod;
        this.calendarName = clalendarName;
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.Metric#getDescription()
     */
    @Override
    public String getDescription() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("feedName", this.feedName)
                .add("expectedExpression", this.expectedExpression)
                .add("latePeriod", this.latePeriod)
                .add("asOfPeriod", this.asOfPeriod)
                .add("calendarName", this.calendarName)
                .toString();
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public CronExpression getExpectedExpression() {
        return expectedExpression;
    }

    public void setExpectedExpression(CronExpression expectedExpression) {
        this.expectedExpression = expectedExpression;
    }

    public Period getLatePeriod() {
        return latePeriod;
    }

    public void setLatePeriod(Period latePeriod) {
        this.latePeriod = latePeriod;
    }

    public Period getAsOfPeriod() {
        return asOfPeriod;
    }

    public void setAsOfPeriod(Period asOfPeriod) {
        this.asOfPeriod = asOfPeriod;
    }

    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String claendarName) {
        this.calendarName = claendarName;
    }
    
    

}
