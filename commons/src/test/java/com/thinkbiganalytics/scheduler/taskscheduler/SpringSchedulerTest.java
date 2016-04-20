package com.thinkbiganalytics.scheduler.taskscheduler;

import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.JobSchedulerException;
import com.thinkbiganalytics.scheduler.TriggerIdentifier;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * Created by matthutton on 3/12/16.
 */
public class SpringSchedulerTest {

    private SpringScheduler scheduler;
    private TaskScheduler taskScheduler;

    @Before
    public void setUp() throws Exception {
        scheduler = new SpringScheduler();
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        scheduler.setTaskScheduler(taskScheduler);
    }


    public void setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }


    class MockRunnable implements Runnable {
        @Override
        public void run() {

        }
    }

    @Test
    public void testScheduler() throws Exception {

        scheduler.scheduleWithCronExpression(new JobIdentifier(), new MockRunnable(), "*/5 * * * * *");
        scheduler.scheduleWithCronExpressionInTimeZone(new JobIdentifier(), new MockRunnable(), "*/5 * * * * *", TimeZone.getTimeZone(""));
        scheduler.scheduleAtFixedRate(new JobIdentifier(), new MockRunnable(), 0L);
        scheduler.scheduleAtFixedRate(new JobIdentifier(), new MockRunnable(), new Date(), 0L);
        scheduler.scheduleWithFixedDelay(new JobIdentifier(), new MockRunnable(), new Date(), 0L);
        scheduler.scheduleWithFixedDelay(new JobIdentifier(), new MockRunnable(), 0L);
        scheduler.schedule(new JobIdentifier(), new MockRunnable(), new Date());


        assertNull(scheduler.getMetaData());

        DateTime dt = DateTime.parse("2016-01-01");
        Date baseDate = dt.toDate();

        Date nextFire = scheduler.getNextFireTime(baseDate, "0 0 12 * * ?");
        assertTrue(nextFire != null && nextFire.getTime() > baseDate.getTime());

        nextFire = scheduler.getNextFireTime("0 0 12 * * ?");
        assertTrue(nextFire != null && nextFire.getTime() > baseDate.getTime());

        List<Date> nextDates = scheduler.getNextFireTimes("0 0 12 * * ?", 10);
        assertTrue(nextDates.size() == 10);

        Date prevFire = scheduler.getPreviousFireTime(baseDate, "0 0 12 * * ?");
        assertTrue(prevFire != null && prevFire.getTime() < baseDate.getTime());

        prevFire = scheduler.getPreviousFireTime("0 0 12 * * ?");
        assertTrue(prevFire != null && prevFire.getTime() > baseDate.getTime());

        List dates = scheduler.getPreviousFireTimes("0 0 12 * * ?", 10);
        assertTrue(dates.size() == 10);

        assertNull(scheduler.getJobs());


    }

    @Test
    public void testUnsupported() throws Exception {

        try {
            scheduler.resumeAll();
            fail();
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e);
        }

        try {
            scheduler.pauseTrigger(null);
            fail();
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e);
        }


        try {
            scheduler.deleteJob(new JobIdentifier());
            fail();
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e);
        }

        try {
            scheduler.startScheduler();
            fail();
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e);
        }

        try {
            scheduler.updateTrigger(new TriggerIdentifier(), "cron");
            fail();
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e);
        }

        try {
            scheduler.triggerJob(new JobIdentifier());
            fail();
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e);
        }

        try {
            scheduler.resumeTrigger(new TriggerIdentifier());
            fail();
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e);
        }

        try {
            scheduler.resumeTriggersOnJob(new JobIdentifier());
            fail();
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e);
        }

        try {
            scheduler.pauseScheduler();
            fail();
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e);
        }

        try {
            scheduler.pauseAll();
            fail();
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e);
        }

        try {
            scheduler.pauseTriggersOnJob(new JobIdentifier());
            fail();
        } catch (UnsupportedOperationException e) {
            Assert.assertNotNull(e);
        }

    }
}