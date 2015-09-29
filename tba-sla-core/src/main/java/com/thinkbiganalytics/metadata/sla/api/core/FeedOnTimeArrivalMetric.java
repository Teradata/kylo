/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api.core;

import java.util.Formatter;

import org.joda.time.Chronology;
import org.joda.time.Period;
import org.quartz.Calendar;
import org.quartz.CronExpression;

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
    private String clalendarName;
//    private Chronology chronology;
//    private Calendar calendar;
    

    /**
     * 
     */
    public FeedOnTimeArrivalMetric() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.Metric#getDescription()
     */
    @Override
    public String getDescription() {
        Formatter f = new Formatter();
//        f.format("", getfe)
        return null;
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

    public String getClaendarName() {
        return clalendarName;
    }

    public void setClalendarName(String claendarName) {
        this.clalendarName = claendarName;
    }
    
    

}
