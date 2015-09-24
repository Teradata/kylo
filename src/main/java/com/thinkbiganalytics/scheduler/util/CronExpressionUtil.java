package com.thinkbiganalytics.scheduler.util;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sr186054 on 9/23/15.
 */
public class CronExpressionUtil {
    private static Logger LOG = LoggerFactory.getLogger(CronExpressionUtil.class);

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


}
