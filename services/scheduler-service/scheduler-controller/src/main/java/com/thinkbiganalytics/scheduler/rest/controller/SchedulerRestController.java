/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.scheduler.rest.controller;

import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.scheduler.JobInfo;
import com.thinkbiganalytics.scheduler.JobScheduler;
import com.thinkbiganalytics.scheduler.JobSchedulerException;
import com.thinkbiganalytics.scheduler.rest.Model;
import com.thinkbiganalytics.scheduler.rest.model.ScheduleIdentifier;
import com.thinkbiganalytics.scheduler.rest.model.ScheduledJob;
import com.thinkbiganalytics.scheduler.rest.model.TriggerInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;

/**
 * Rest Controller for Quartz Scheduler
 */
@Api(value = "scheduler",  produces = "application/json")
@Path("/v1/scheduler")
public class SchedulerRestController {

  private static final Logger LOG = LoggerFactory.getLogger(SchedulerRestController.class);


  @Autowired
  private JobScheduler quartzScheduler;

  @GET
  @Path("/metadata")
  @Produces({MediaType.APPLICATION_JSON})
  public Map<String, Object> getMetaData() throws JobSchedulerException {
    return quartzScheduler.getMetaData();

  }

  @POST
  @Path("/pause")
  @Produces({MediaType.APPLICATION_JSON})
  public RestResponseStatus standByScheduler() throws JobSchedulerException {
    quartzScheduler.pauseScheduler();
    return RestResponseStatus.SUCCESS;

  }

  @POST
  @Path("/resume")
  @Produces({MediaType.APPLICATION_JSON})
  public RestResponseStatus startScheduler() throws JobSchedulerException {
    quartzScheduler.startScheduler();
    return RestResponseStatus.SUCCESS;


  }


  @POST
  @Path("/triggers/update")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
  @Produces({MediaType.APPLICATION_JSON})
  public RestResponseStatus rescheduleTrigger(TriggerInfo trigger) throws JobSchedulerException {

    //convert to Domain Code

    quartzScheduler
        .updateTrigger(Model.TRIGGER_IDENTIFIER_TO_DOMAIN.apply(trigger.getTriggerIdentifier()), trigger.getCronExpression());
    return RestResponseStatus.SUCCESS;
  }


  @POST
  @Path("/triggers/pause")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
  @Produces({MediaType.APPLICATION_JSON})
  public RestResponseStatus pauseTrigger( ScheduleIdentifier triggerIdentifier) throws JobSchedulerException {

    quartzScheduler.pauseTrigger(Model.TRIGGER_IDENTIFIER_TO_DOMAIN.apply(triggerIdentifier));
    return RestResponseStatus.SUCCESS;

  }

  @POST
  @Path("/triggers/resume")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
  @Produces({MediaType.APPLICATION_JSON})
  public RestResponseStatus resumeTrigger(ScheduleIdentifier triggerIdentifier) throws JobSchedulerException {

    quartzScheduler.resumeTrigger(Model.TRIGGER_IDENTIFIER_TO_DOMAIN.apply(triggerIdentifier));
    return RestResponseStatus.SUCCESS;

  }

/*
    @RequestMapping(value = "/api/v1/scheduler/job/delete", method = RequestMethod.POST)
    @ResponseBody
    public RestResponseStatus deleteJob(@RequestBody JobIdentifier jobIdentifier) throws JobSchedulerException {

        quartzScheduler.deleteJob(jobIdentifier);
        return RestResponseStatus.SUCCESS;

    }
*/

  @POST
  @Path("/jobs/trigger")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
  @Produces({MediaType.APPLICATION_JSON})
  public RestResponseStatus triggerJob( ScheduleIdentifier jobIdentifier) throws JobSchedulerException {
    quartzScheduler.triggerJob(Model.JOB_IDENTIFIER_TO_DOMAIN.apply(jobIdentifier));
    return RestResponseStatus.SUCCESS;
  }

  @POST
  @Path("/jobs/pause")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
  @Produces({MediaType.APPLICATION_JSON})
  public RestResponseStatus pauseJob(ScheduleIdentifier jobIdentifier) throws JobSchedulerException {
    quartzScheduler.pauseTriggersOnJob(Model.JOB_IDENTIFIER_TO_DOMAIN.apply(jobIdentifier));
    return RestResponseStatus.SUCCESS;
  }

  @POST
  @Path("/jobs/resume")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
  @Produces({MediaType.APPLICATION_JSON})
  public RestResponseStatus resumeJob(ScheduleIdentifier jobIdentifier) throws JobSchedulerException {
    quartzScheduler.resumeTriggersOnJob(Model.JOB_IDENTIFIER_TO_DOMAIN.apply(jobIdentifier));
    return RestResponseStatus.SUCCESS;
  }

  @GET
  @Path("/jobs")
  @Produces({MediaType.APPLICATION_JSON})
  public List<ScheduledJob> getJobs() throws JobSchedulerException {
    List<ScheduledJob> quartzScheduledJobs = new ArrayList<ScheduledJob>();
    List<JobInfo> jobs = quartzScheduler.getJobs();
    if (jobs != null && !jobs.isEmpty()) {
      for (JobInfo jobInfo : jobs) {
        ScheduledJob jobBean = Model.DOMAIN_TO_SCHEDULED_JOB.apply(jobInfo);
        quartzScheduledJobs.add(jobBean);
      }
    }
    return quartzScheduledJobs;


  }

}
