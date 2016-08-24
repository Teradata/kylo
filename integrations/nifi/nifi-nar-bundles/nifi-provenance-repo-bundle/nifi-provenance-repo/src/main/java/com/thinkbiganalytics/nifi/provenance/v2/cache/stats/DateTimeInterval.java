package com.thinkbiganalytics.nifi.provenance.v2.cache.stats;

import org.joda.time.DateTime;


/**
 * Ensure the start and endtime fall within the same Minute
 *
 * Created by sr186054 on 8/23/16.
 */
public class DateTimeInterval {
    private DateTime incomingStartTime;
    private DateTime incomingEndTime;

    private DateTime adjustedEndTime;

    private DateTime nextStartTime;


    public DateTimeInterval(DateTime incomingStartTime, DateTime incomingEndTime){
        this.incomingStartTime = incomingStartTime;
        this.incomingEndTime = incomingEndTime;
        adjustTimes();
    }


    public DateTime getNextStartTime() {
        return nextStartTime;
    }

    public DateTime getAdjustedEndTime() {
        return adjustedEndTime;
    }

    private void adjustTimes(){
        adjustedEndTime = incomingEndTime;
        nextStartTime =incomingEndTime;
        //if the day of the month is different adjust the time to be the last
        boolean dayMatch = false;
        boolean hourMatch = false;
        boolean minuteMatch = false;
        if(adjustedEndTime.getDayOfYear() == incomingStartTime.getDayOfYear()){
            dayMatch = true;
        }
        if(adjustedEndTime.getHourOfDay() == incomingStartTime.getHourOfDay()){
            hourMatch = true;
        }
        if(adjustedEndTime.getMinuteOfHour() == incomingStartTime.getMinuteOfHour()){
            minuteMatch = true;
        }

        if(!dayMatch){
            //if the Days dont match then set the time to be the last possible datetime matching the dayOfYear
            adjustedEndTime = incomingStartTime.millisOfDay().withMaximumValue();
            nextStartTime = adjustedEndTime.plusMillis(1);
        }
        else if(!hourMatch) {
            //set the hour to be the max possible time
            adjustedEndTime = incomingStartTime.hourOfDay().withMaximumValue().minuteOfHour().withMaximumValue().secondOfMinute().withMaximumValue().millisOfSecond().withMaximumValue();
            nextStartTime = adjustedEndTime.plusMillis(1);
        }
        else if(!minuteMatch){
            adjustedEndTime = incomingStartTime.secondOfMinute().withMaximumValue().millisOfSecond().withMaximumValue();
            nextStartTime = adjustedEndTime.plusMillis(1);
        }



    }
}
