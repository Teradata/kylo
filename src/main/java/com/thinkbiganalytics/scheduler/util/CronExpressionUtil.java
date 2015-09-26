package com.thinkbiganalytics.scheduler.util;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *Generic Cron Utility for calculating Cron Dates both Previous and Next Occurrences
 */
public class CronExpressionUtil {
    private static Logger LOG = LoggerFactory.getLogger(CronExpressionUtil.class);

    public static Long getCronInterval(String cronExpression) throws ParseException{
        Date nextValidTime = getNextFireTime(cronExpression);
        Date subsequentNextValidTime = getNextFireTime(nextValidTime,cronExpression);
        long interval = subsequentNextValidTime.getTime() - nextValidTime.getTime();
        return interval;
    }

    public static Date getPreviousFireTime(String cronExpression) throws ParseException{
        Long interval = getCronInterval(cronExpression);
        Date nextValidTime = getNextFireTime(cronExpression);
        return new Date(nextValidTime.getTime() - interval);
    }

    public static Date getPreviousFireTime(Date lastFireTime, String cronExpression) throws ParseException{
        Long interval = getCronInterval(cronExpression);
        Date nextValidTime = getNextFireTime(lastFireTime,cronExpression);
        return new Date(nextValidTime.getTime() - interval);
    }


    public static Date getNextFireTime(String cronExpression) throws ParseException{
        CronExpression c = new CronExpression(cronExpression);
       return  c.getNextValidTimeAfter(new Date());
    }

    public static Date getNextFireTime(Date lastFireTime, String cronExpression) throws ParseException{
        CronExpression c = new CronExpression(cronExpression);
        return  c.getNextValidTimeAfter(lastFireTime);
    }

    public static List<Date> getNextFireTimes(String cronExpression, Integer count) throws ParseException{
        List<Date> dates = new ArrayList<Date>();
        Date lastDate = new Date();
        CronExpression cron = new CronExpression(cronExpression);
        for(int i=0; i<count; i++) {
            Date nextDate = cron.getNextValidTimeAfter(lastDate);
            dates.add(nextDate);
            lastDate = nextDate;
        }
        return dates;
    }

    public static List<Date> getPreviousFireTimes(String cronExpression, Integer count) throws ParseException{
        List<Date> dates = new ArrayList<Date>();
        Long interval = getCronInterval(cronExpression);
        Date nextFireTime = getNextFireTime(cronExpression);
        for(int i=0; i<count; i++) {
            Date previous = new Date(nextFireTime.getTime() - interval);
            dates.add(previous);
            nextFireTime = previous;
        }
        return dates;
    }


}
