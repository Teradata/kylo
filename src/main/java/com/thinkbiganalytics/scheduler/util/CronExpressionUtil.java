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
        return getCronInterval(new CronExpression(cronExpression));
    }

    public static Long getCronInterval(CronExpression cron) throws ParseException{
        Date nextValidTime = getNextFireTime(cron);
        Date subsequentNextValidTime = getNextFireTime(nextValidTime, cron);
        long interval = subsequentNextValidTime.getTime() - nextValidTime.getTime();
        return interval;
    }

    public static Date getPreviousFireTime(String cronExpression) throws ParseException{
        return getPreviousFireTime(new CronExpression(cronExpression));
    }

    public static Date getPreviousFireTime(CronExpression cron) throws ParseException{
        Long interval = getCronInterval(cron);
        Date nextValidTime = getNextFireTime(cron);
        return new Date(nextValidTime.getTime() - interval);
    }

    public static Date getPreviousFireTime(Date lastFireTime, String cronExpression) throws ParseException{
        return getPreviousFireTime(lastFireTime, new CronExpression(cronExpression));
    }

    public static Date getPreviousFireTime(Date lastFireTime, CronExpression cron) throws ParseException{
        Long interval = getCronInterval(cron);
        Date nextValidTime = getNextFireTime(lastFireTime,cron);
        return new Date(nextValidTime.getTime() - interval);
    }


    public static Date getNextFireTime(String cronExpression) throws ParseException{
        return getNextFireTime(new CronExpression(cronExpression));
    }

    public static Date getNextFireTime(CronExpression cron) throws ParseException{
       return  cron.getNextValidTimeAfter(new Date());
    }

    public static Date getNextFireTime(Date lastFireTime, String cronExpression) throws ParseException{
        return getNextFireTime(lastFireTime, new CronExpression(cronExpression));
    }

    public static Date getNextFireTime(Date lastFireTime, CronExpression cron) throws ParseException{
        return  cron.getNextValidTimeAfter(lastFireTime);
    }

    public static List<Date> getNextFireTimes(String cronExpression, Integer count) throws ParseException{
        return getNextFireTimes(new CronExpression(cronExpression), count);
    }

    public static List<Date> getNextFireTimes(CronExpression cron, Integer count) throws ParseException{
        List<Date> dates = new ArrayList<Date>();
        Date lastDate = new Date();
        for(int i=0; i<count; i++) {
            Date nextDate = cron.getNextValidTimeAfter(lastDate);
            dates.add(nextDate);
            lastDate = nextDate;
        }
        return dates;
    }

    public static List<Date> getPreviousFireTimes(String cronExpression, Integer count) throws ParseException{
        return getPreviousFireTimes(new CronExpression(cronExpression), count);
    }

    public static List<Date> getPreviousFireTimes(CronExpression cron, Integer count) throws ParseException{
        List<Date> dates = new ArrayList<Date>();
        Long interval = getCronInterval(cron);
        Date nextFireTime = getNextFireTime(cron);
        for(int i=0; i<count; i++) {
            Date previous = new Date(nextFireTime.getTime() - interval);
            dates.add(previous);
            nextFireTime = previous;
        }
        return dates;
    }


}
