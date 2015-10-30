/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api.core;

import org.joda.time.Period;
import org.quartz.CronExpression;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
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
                                   String calendarName) {
        super();
        this.feedName = feedName;
        this.expectedExpression = expectedExpression;
        this.latePeriod = latePeriod;
        this.asOfPeriod = asOfPeriod;
        this.calendarName = calendarName;
    }


    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.api.Metric#getDescription()
     */
    @Override
    public String getDescription() {
        StringBuilder bldr = new StringBuilder("Data should arrive from feed ");
        bldr.append("\"").append(this.feedName).append("\" ")
            .append(generateCronDescription(this.expectedExpression.toString()))
            .append(" and no more than ").append(this.latePeriod).append(" hours later");
        return bldr.toString();
    }
    
    @Override
    public String toString() {
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
    
    private String generateCronDescription(String cronExp) {
        CronDefinition quartzDef = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser parser = new CronParser(quartzDef);
        Cron c = parser.parse(cronExp);
        return CronDescriptor.instance().describe(c);
    }

}
