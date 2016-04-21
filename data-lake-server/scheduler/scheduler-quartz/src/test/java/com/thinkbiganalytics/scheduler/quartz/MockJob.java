package com.thinkbiganalytics.scheduler.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by matthutton on 3/11/16.
 */
public class MockJob implements Job {
    public MockJob() {

    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // ok
    }
}
