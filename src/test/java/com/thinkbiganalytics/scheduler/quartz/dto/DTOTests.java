package com.thinkbiganalytics.scheduler.quartz.dto;

import com.thinkbiganalytics.scheduler.util.JavaBeanTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.*;

import java.util.Vector;

import static org.junit.Assert.assertTrue;

/**
 * Created by matthutton on 3/11/16.
 */
public class DTOTests {

    private JobDetailDTO jobDetailDTO;
    private JobTriggerDetail detail;
    private TriggerDTO triggerDTO;
    private JobDetail jobDetail;
    private Trigger trigger;

    @Before
    public void setUp() throws Exception {

        jobDetail = Mockito.mock(JobDetail.class);
        Mockito.when(jobDetail.getKey()).thenReturn(new JobKey("name", "group"));
        jobDetailDTO = new JobDetailDTO();

        trigger = Mockito.mock(CronTrigger.class);
        Mockito.when(trigger.getKey()).thenReturn(new TriggerKey("name", "group"));
        Mockito.when(trigger.getJobKey()).thenReturn(new JobKey("name", "group"));
    }

    @Test
    public void testJobDetail() throws Exception {
        JavaBeanTester.test(JobDetailDTO.class, "jobClass");
        jobDetailDTO = new JobDetailDTO(jobDetail);
        jobDetailDTO.setJobClass(Job.class);
        assertTrue(jobDetailDTO.getJobClass() == Job.class);
        jobDetailDTO.setIsPersistJobDataAfterExecution(true);
        jobDetailDTO.setIsConcurrentExectionDisallowed(true);
        jobDetailDTO.setRequestsRecovery(true);
        jobDetailDTO.setShouldRecover(true);
        assertTrue(jobDetailDTO.isPersistJobDataAfterExecution());
        assertTrue(jobDetailDTO.isConcurrentExectionDisallowed());
        assertTrue(jobDetailDTO.isPersistJobDataAfterExecution());
        assertTrue(jobDetailDTO.isShouldRecover());

    }

    @Test
    public void testTriggerDetail() throws Exception {
        JavaBeanTester.test(JobTriggerDetail.class, "triggers");
        JobTriggerDetail detail = new JobTriggerDetail();
        Vector<TriggerDTO> v = new Vector<>();
        detail.setTriggers(v);
        assertTrue(detail.getTriggers() == v);

    }


    @Test
    public void testTriggerDTO() throws Exception {
        JavaBeanTester.test(TriggerDTO.class);
        new TriggerDTO(trigger);
    }

}