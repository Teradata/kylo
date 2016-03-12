package com.thinkbiganalytics.scheduler.quartz.dto;

import com.thinkbiganalytics.scheduler.util.JavaBeanTester;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by matthutton on 3/11/16.
 */
public class DTOTests {

    private JobDetailDTO jobDetailDTO;
    private JobTriggerDetail detail;
    private TriggerDTO triggerDTO;

    @Before
    public void setUp() throws Exception {
        jobDetailDTO = new JobDetailDTO();

    }

    @Test
    public void testJobDetail() throws Exception {
        JavaBeanTester.test(JobDetailDTO.class, "jobClass");
    }

    @Test
    public void testTriggerDetail() throws Exception {
        JavaBeanTester.test(JobTriggerDetail.class, "triggers");
    }


    @Test
    public void testTriggerDTO() throws Exception {
        JavaBeanTester.test(TriggerDTO.class);
    }

}