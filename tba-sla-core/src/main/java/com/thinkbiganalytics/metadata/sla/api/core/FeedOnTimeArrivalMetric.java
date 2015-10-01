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
        this.clalendarName = clalendarName;
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.Metric#getDescription()
     */
    @Override
    public String getDescription() {
        Formatter f = new Formatter();
//        f.format("", getfe)
        // TODO: format default description
        return toString();
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
        return clalendarName;
    }

    public void setClalendarName(String claendarName) {
        this.clalendarName = claendarName;
    }
    
    

}
