package com.thinkbiganalytics.scheduler;

/*-
 * #%L
 * thinkbig-scheduler-quartz
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

import com.thinkbiganalytics.scheduler.model.DefaultTriggerIdentifier;
import com.thinkbiganalytics.scheduler.quartz.MockJob;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuartzSchedulerTest {

    QuartzScheduler toTest;

    @Mock
    QuartzClusterMessageSender clusterMessageSender;

    @Mock
    QuartzClusterMessageReceiver clusterMessageReceiver;

    @Mock
    JobIdentifier jobIdentifier;
    @Mock
    SchedulerFactoryBean schedulerFactoryBean;
    @Mock
    Scheduler scheduler;

    Runnable task = new Runnable() {
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
        Whitebox.setInternalState(toTest, "clusterMessageSender", clusterMessageSender);
        mockJobIdentifier("job-name", "group");
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

        toTest.scheduleAtFixedRate(jobIdentifier, task, 1l);

        verify(scheduler).scheduleJob((JobDetail) any(), (Trigger) any());
    }

    @Test
    public void scheduleAtFixedRateTest2() throws Exception {
        mockJobIdentifier("job-name", "group");

        toTest.scheduleAtFixedRate(jobIdentifier, task, new Date(), 1l);

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

    @SuppressWarnings({"rawtypes", "unchecked"})
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

    @SuppressWarnings({"rawtypes", "unchecked"})
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

        toTest.updateTrigger(new DefaultTriggerIdentifier("trigger-key-name", "trigger-key-name"),
                             "0 0 12 * * ?");
        verify(scheduler).rescheduleJob(eq(new TriggerKey("trigger-key-name", "trigger-key-name")),
                                        (Trigger) any());

        doThrow(new SchedulerException()).when(scheduler).rescheduleJob((TriggerKey) any(),
                                                                        (Trigger) any());
        try {
            toTest.updateTrigger(new DefaultTriggerIdentifier("trigger-key-name", "trigger-key-name"),
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

    @Test
    public void scheduleWithCronExpression() throws Exception {
        mockJobIdentifier("job-name", "group");

        toTest.scheduleWithCronExpression(jobIdentifier, task, "0 0 12 * * ?");

        verify(jobIdentifier).getName();
        verify(jobIdentifier, times(2)).getGroup();
    }

    @Test
    public void testGetJobs() throws Exception {
        Vector<String> jobGroupNames = new Vector<>();
        jobGroupNames.add("group");
        Mockito.when(scheduler.getJobGroupNames()).thenReturn(jobGroupNames);
        Set<JobKey> set = new HashSet<>();
        set.add(new JobKey("name", "group"));
        Mockito.when(scheduler.getJobKeys((GroupMatcher) anyObject())).thenReturn(set);
        Mockito.when(scheduler.getJobDetail((JobKey) anyObject())).thenReturn(new JobDetailImpl("name", "group", MockJob.class));
        List v = new Vector<>();
        addMockTrigger(v, "name", "group");

        Mockito.when(scheduler.getTriggerState((TriggerKey) Mockito.anyObject())).thenReturn(Trigger.TriggerState.BLOCKED);
        Mockito.when(scheduler.getTriggersOfJob((JobKey) anyObject())).thenReturn(v);
        assertTrue(toTest.getJobs().size() == 1);
    }

    @Test
    public void scheduleAtFixedDelayTest1() throws Exception {
        mockJobIdentifier("job-name", "group");

        toTest.scheduleWithFixedDelay(jobIdentifier, task, 0l);
        verify(scheduler).scheduleJob((JobDetail) any(), (Trigger) any());
    }

    @Test
    public void scheduleAtFixedRateWithDelayTest() throws Exception {
        mockJobIdentifier("job-name", "group");

        toTest.scheduleAtFixedRateWithDelay(jobIdentifier, task, "run", new Date(), 1l, 1L);
        verify(scheduler).scheduleJob((JobDetail) any(), (Trigger) any());
    }


    @Test
    public void scheduleAtFixedDelayTest2() throws Exception {
        mockJobIdentifier("job-name", "group");

        toTest.scheduleWithFixedDelay(jobIdentifier, task, new Date(), 0l);
        verify(scheduler).scheduleJob((JobDetail) any(), (Trigger) any());
    }

    @Test
    public void staticHelperTest() throws Exception {
        JobIdentifier identifier = QuartzScheduler.jobIdentifierForJobKey(new JobKey("job-name", "group"));
        JobKey jobKey = QuartzScheduler.jobKeyForJobIdentifier(identifier);

        assertEquals(jobKey.getName(), identifier.getName());

        TriggerKey triggerKey = new TriggerKey("trigger-key-name", "trigger-key-name");
        TriggerIdentifier triggerIdentifier = QuartzScheduler.triggerIdentifierForTriggerKey(triggerKey);
        triggerKey = QuartzScheduler.triggerKeyForTriggerIdentifier(triggerIdentifier);

        assertEquals(triggerKey.getName(), triggerIdentifier.getName());
    }

    @Test
    public void buildTriggerInfo() throws Exception {
        JobDetailImpl detail = new JobDetailImpl();
        detail.setName("job-name");
        JobInfo info = toTest.buildJobInfo(detail);
        assertNotNull(info);
    }

    @Test
    public void scheduleAtFixedDelayTest3() throws Exception {
        mockJobIdentifier("job-name", "group");

        toTest.scheduleWithFixedDelay(jobIdentifier, task, "run", new Date(), 0l);
        verify(scheduler).scheduleJob((JobDetail) any(), (Trigger) any());
    }

    @Test
    public void controlTests() throws Exception {
        toTest.pauseAll();
        toTest.resumeAll();
        toTest.getMetaData();
    }

    @Test
    public void existsTests() throws Exception {
        Set<JobKey> set = new HashSet<>();
        set.add(new JobKey("name", "group"));
        Mockito.when(scheduler.getJobKeys((GroupMatcher) anyObject())).thenReturn(set);

        toTest.jobExists(jobIdentifier);
        TriggerIdentifier triggerIdentifer = new DefaultTriggerIdentifier("trigger-key-name", "trigger-key-name");
        toTest.triggerExists(triggerIdentifer);
        toTest.resumeAll();
        toTest.getMetaData();
    }

    private void mockJobIdentifier(String jobName, String groupName) {
        when(jobIdentifier.getName()).thenReturn(jobName);
        when(jobIdentifier.getGroup()).thenReturn(groupName);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void addMockTrigger(final List list, String triggerKeyName, String triggerKeyGroup) {
        final Trigger trigger = mock(Trigger.class);
        final TriggerKey triggerKey = new TriggerKey(triggerKeyName, triggerKeyGroup);
        when(trigger.getKey()).thenReturn(triggerKey);
        list.add(trigger);
    }
}
