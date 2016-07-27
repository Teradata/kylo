/**
 *
 */
package com.thinkbiganalytics.metadata.sla.api.core;

import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreementMetric;
import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;
import com.thinkbiganalytics.policy.PropertyLabelValue;
import com.thinkbiganalytics.policy.validation.PolicyPropertyTypes;
import com.thinkbiganalytics.scheduler.util.TimerToCronExpression;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Locale;

/**
 * @author Sean Felten
 *
 *         TODO Wire CalendarName into the @PolicyProperty so its available on the UI
 */
@ServiceLevelAgreementMetric(name = "Feed Processing deadline",
                             description = "Ensure a Feed processes data by a specified time")
public class FeedOnTimeArrivalMetric implements Metric {

    @PolicyProperty(name = "FeedName",
                    type = PolicyPropertyTypes.PROPERTY_TYPE.currentFeed,
                    hidden = true)
    private String feedName;

    @PolicyProperty(name = "ExpectedDeliveryTime",
                    displayName = "Expected Delivery Time",
                    type = PolicyPropertyTypes.PROPERTY_TYPE.cron,
                    hint = "Cron Expression for when you expect to receive this data",
                    value = "0 0 12 1/1 * ? *",
                    required = true)
    private String cronString;

    @PolicyProperty(name = "NoLaterThanTime",
                    displayName = "No later than time",
                    type = PolicyPropertyTypes.PROPERTY_TYPE.number,
                    hint = "Number specifying the amount of time allowed after the Expected Delivery Time",
                    group = "lateTime",
                    required = true)
    private Integer lateTime;

    @PolicyProperty(name = "NoLaterThanUnits",
                    displayName = "Units", type = PolicyPropertyTypes.PROPERTY_TYPE.select,
                    group = "lateTime",
                    labelValues = {@PropertyLabelValue(label = "Days", value = "days"),
                                   @PropertyLabelValue(label = "Hours", value = "hrs"),
                                   @PropertyLabelValue(label = "Minutes", value = "min"),
                                   @PropertyLabelValue(label = "Seconds", value = "sec")},
                    required = true)
    private String lateUnits;

    /*  @PolicyProperty(name = "AsOfTime",
                        displayName = "As of time",
                        type = PolicyPropertyTypes.PROPERTY_TYPE.number,
                        hint = "as of time",
                        group = "asOfTime",
                        required = true) */
    private Integer asOfTime;

    /* @PolicyProperty(name = "AsOfUnits", displayName = "Units",
                     type = PolicyPropertyTypes.PROPERTY_TYPE.select,
                     group = "asOfTime",
                     labelValues = {@PropertyLabelValue(label = "Days", value = "days"),
                                    @PropertyLabelValue(label = "Hours", value = "hrs"),
                                    @PropertyLabelValue(label = "Minutes", value = "min"),
                                    @PropertyLabelValue(label = "Seconds", value = "sec")},
                     required = true)*/
    private String asOfUnits;


    @JsonIgnore
    private CronExpression expectedExpression;
    private Period latePeriod;

    private Period asOfPeriod;

    private String calendarName;


    /**
     *
     */
    public FeedOnTimeArrivalMetric() {
    }

    public FeedOnTimeArrivalMetric(@PolicyPropertyRef(name = "FeedName") String feedName,
                                   @PolicyPropertyRef(name = "ExpectedDeliveryTime") String cronString,
                                   @PolicyPropertyRef(name = "NoLaterThanTime") Integer lateTime,
                                   @PolicyPropertyRef(name = "NoLaterThanUnits") String lateUnits,
                                   Integer asOfTime,
                                   String asOfUnits) throws ParseException {
        this.feedName = feedName;
        this.cronString = cronString;
        this.lateTime = lateTime;
        this.lateUnits = lateUnits;
        this.asOfTime = asOfTime;
        this.asOfUnits = asOfUnits;

        this.expectedExpression = new CronExpression(this.cronString);
        this.latePeriod = TimerToCronExpression.timerStringToPeriod(this.lateTime + " " + this.lateUnits);

        if (asOfTime != null && StringUtils.isNotBlank(asOfUnits)) {
            this.asOfPeriod = TimerToCronExpression.timerStringToPeriod(this.asOfTime + " " + this.asOfUnits);
        }




    }


    public FeedOnTimeArrivalMetric(@PolicyPropertyRef(name = "FeedName") String feedName,
                                   @PolicyPropertyRef(name = "ExpectedDeliveryTime") String cronString,
                                   @PolicyPropertyRef(name = "NoLaterThanTime") Integer lateTime,
                                   @PolicyPropertyRef(name = "NoLaterThanUnits") String lateUnits) throws ParseException {
        this.feedName = feedName;
        this.cronString = cronString;
        this.lateTime = lateTime;
        this.lateUnits = lateUnits;

        this.expectedExpression = new CronExpression(this.cronString);
        this.latePeriod = TimerToCronExpression.timerStringToPeriod(this.lateTime + " " + this.lateUnits);
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
        StringBuilder bldr = new StringBuilder("Data expected from feed ");

        bldr.append("\"").append(this.feedName).append("\" ")
            .append(generateCronDescription(this.getExpectedExpression().toString()))
            .append(", and no more than ").append(this.latePeriod.getHours()).append(" hours late");
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
        if (this.expectedExpression == null && this.cronString != null) {
            try {
                this.expectedExpression = new CronExpression(this.cronString);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
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
        return CronDescriptor.instance(Locale.US).describe(c);
    }

}
