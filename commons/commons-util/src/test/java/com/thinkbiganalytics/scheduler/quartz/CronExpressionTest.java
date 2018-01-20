package com.thinkbiganalytics.scheduler.quartz;

/*-
 * #%L
 * thinkbig-commons-util
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;

import org.junit.Assert;
import org.junit.Test;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Test CronExpression Utility
 */
public class CronExpressionTest {

    private static final Logger log = LoggerFactory.getLogger(CronExpressionTest.class);

    @Test
    public void testNextFireTimes() {

        String cronExpression = "0 0 12 1/1 * ? *"; // every day at 12:00 PM
        try {
            List<Date> dates = CronExpressionUtil.getNextFireTimes(cronExpression, 20);
            int count = 1;
            for (Date date : dates) {
                System.out.println(count + ". " + date);
                count++;
            }

            dates = CronExpressionUtil.getPreviousFireTimes(cronExpression, 20);
            count = 1;
            for (Date date : dates) {
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
            Assert.assertNotNull(cron);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
