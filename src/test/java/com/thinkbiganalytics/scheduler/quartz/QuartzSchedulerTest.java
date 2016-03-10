package com.thinkbiganalytics.scheduler.quartz;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.thinkbiganalytics.scheduler.JobIdentifier;
import com.thinkbiganalytics.scheduler.JobSchedulerException;
import com.thinkbiganalytics.scheduler.TriggerIdentifier;

public class QuartzSchedulerTest {

    QuartzScheduler toTest;

    @Mock
    JobIdentifier   jobIdentifier;
    @Mock
    SchedulerFactoryBean schedulerFactoryBean;
    @Mock
    Scheduler            scheduler;

    Runnable        task = new Runnable() {
                             @Override
                             public void run() {
                                 // TODO Auto-generated method stub
                             }
                         };

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);

        when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);

        toTest = new QuartzScheduler();
        toTest.schedulerFactoryBean = schedulerFactoryBean;
    }

    @Test
    public void scheduleWithCronExpressionInTimeZoneTest() throws Exception {
        mockJobIdentifier("job-name", "group");

        toTest.scheduleWithCronExpressionInTimeZone(jobIdentifier, task, "0 0 12 * * ?",
                TimeZone.getTimeZone("UTC"));

        verify(jobIdentifier).getName();
        verify(jobIdentifier, times(2)).getGroup();
    }

    @Test
    public void scheduleTest() throws Exception {
        mockJobIdentifier("job-name", "group");

        toTest.schedule(jobIdentifier, task, new Date());

        verify(scheduler).scheduleJob((JobDetail) any(), (Trigger) any());
    }

    @Test
    public void scheduleAtFixedRateTest() throws Exception {
        mockJobIdentifier("job-name", "group");

        toTest.scheduleAtFixedRate(jobIdentifier, task, 0l);

        verify(scheduler).scheduleJob((JobDetail) any(), (Trigger) any());
    }

    @Test
    public void startSchedulerTest() throws Exception {

        toTest.startScheduler();
        verify(scheduler).start();

        doThrow(new SchedulerException()).when(scheduler).start();
        try {
            toTest.startScheduler();
        } catch (final JobSchedulerException e) {
            return;
        }
        fail();
    }

    @Test
    public void pauseSchedulerTest() throws Exception {

        toTest.pauseScheduler();
        verify(scheduler).standby();

        doThrow(new SchedulerException()).when(scheduler).standby();
        try {
            toTest.pauseScheduler();
        } catch (final JobSchedulerException e) {
            return;
        }
        fail();
    }

    @Test
    public void triggerJobTest() throws Exception {
        mockJobIdentifier("job-name", "group");

        toTest.triggerJob(jobIdentifier);
        verify(scheduler).triggerJob(eq(new JobKey("job-name", "group")));

        doThrow(new SchedulerException()).when(scheduler).triggerJob((JobKey) any());
        try {
            toTest.triggerJob(jobIdentifier);
        } catch (final JobSchedulerException e) {
            return;
        }
        fail();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void pauseTriggersOnJobTest() throws Exception {
        mockJobIdentifier("job-name", "group");
        final List list = new ArrayList<Trigger>();
        addMockTrigger(list, "trigger-key-name", "trigger-key-name");
        when(scheduler.getTriggersOfJob(eq(new JobKey("job-name", "group")))).thenReturn(list);

        toTest.pauseTriggersOnJob(jobIdentifier);
        verify(scheduler).pauseTrigger(eq(new TriggerKey("trigger-key-name", "trigger-key-name")));

        doThrow(new SchedulerException()).when(scheduler).getTriggersOfJob((JobKey) any());
        try {
            toTest.pauseTriggersOnJob(jobIdentifier);
        } catch (final JobSchedulerException e) {
            return;
        }
        fail();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void resumeTriggersOnJobTest() throws Exception {
        mockJobIdentifier("job-name", "group");
        final List list = new ArrayList<Trigger>();
        addMockTrigger(list, "trigger-key-name", "trigger-key-name");
        when(scheduler.getTriggersOfJob(eq(new JobKey("job-name", "group")))).thenReturn(list);

        toTest.resumeTriggersOnJob(jobIdentifier);
        verify(scheduler).resumeTrigger(eq(new TriggerKey("trigger-key-name", "trigger-key-name")));

        doThrow(new SchedulerException()).when(scheduler).getTriggersOfJob((JobKey) any());
        try {
            toTest.resumeTriggersOnJob(jobIdentifier);
        } catch (final JobSchedulerException e) {
            return;
        }
        fail();
    }

    @Test
    public void updateTriggerTest() throws Exception {

        toTest.updateTrigger(new TriggerIdentifier("trigger-key-name", "trigger-key-name"),
                "0 0 12 * * ?");
        verify(scheduler).rescheduleJob(eq(new TriggerKey("trigger-key-name", "trigger-key-name")),
                (Trigger) any());

        doThrow(new SchedulerException()).when(scheduler).rescheduleJob((TriggerKey) any(),
                (Trigger) any());
        try {
            toTest.updateTrigger(new TriggerIdentifier("trigger-key-name", "trigger-key-name"),
                    "0 0 12 * * ?");
        } catch (final JobSchedulerException e) {
            return;
        }
        fail();
    }

    @Test
    public void deleteJobTest() throws Exception {
        mockJobIdentifier("job-name", "group");

        toTest.deleteJob(jobIdentifier);
        verify(scheduler).deleteJob(eq(new JobKey("job-name", "group")));

        doThrow(new SchedulerException()).when(scheduler).deleteJob((JobKey) any());
        try {
            toTest.deleteJob(jobIdentifier);
        } catch (final JobSchedulerException e) {
            return;
        }
        fail();
    }

    /*
     *
     */

    private void mockJobIdentifier(String jobName, String groupName) {
        when(jobIdentifier.getName()).thenReturn(jobName);
        when(jobIdentifier.getGroup()).thenReturn(groupName);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void addMockTrigger(final List list, String triggerKeyName, String triggerKeyGroup) {
        final Trigger trigger = mock(Trigger.class);
        final TriggerKey triggerKey = new TriggerKey(triggerKeyName, triggerKeyGroup);
        when(trigger.getKey()).thenReturn(triggerKey);
        list.add(trigger);
    }
}
