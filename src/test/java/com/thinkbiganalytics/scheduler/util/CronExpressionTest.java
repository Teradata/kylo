package com.thinkbiganalytics.scheduler.util;

import org.junit.Test;
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
                LOG.info(count+". "+date);
                count++;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
