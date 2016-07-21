package com.thinkbiganalytics.scheduler.quartz;

import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;

import org.junit.Test;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Created by sr186054 on 9/23/15.
 */
public class CronExpressionTest  {
    private static Logger LOG = LoggerFactory.getLogger(CronExpressionTest.class);

    @Test
public void testNextFireTimes(){

    String cronExpression = "0 0 12 1/1 * ? *"; // every day at 12:00 PM
        try {
            List<Date> dates = CronExpressionUtil.getNextFireTimes(cronExpression, 20);
            int count = 1;
            for(Date date:dates){
                System.out.println(count + ". " + date);
                count++;
            }

            dates = CronExpressionUtil.getPreviousFireTimes(cronExpression, 20);
            count = 1;
            for(Date date:dates){
                System.out.println(count + ". " + date);
                count++;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testTimerToCron() {
        String timer = "30 hours";
        try {
            CronExpression expression = CronExpressionUtil.timerToCronExpression(timer);
            String cron = expression.getCronExpression();
            int i = 0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
