package com.thinkbiganalytics.scheduler.rest.controller;

import com.thinkbiganalytics.jobrepo.security.OperationsAccessControl;

/*-
 * #%L
 * thinkbig-scheduler-controller
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

import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.scheduler.JobInfo;
import com.thinkbiganalytics.scheduler.JobScheduler;
import com.thinkbiganalytics.scheduler.JobSchedulerException;
import com.thinkbiganalytics.scheduler.QuartzScheduler;
import com.thinkbiganalytics.scheduler.model.DefaultJobIdentifier;
import com.thinkbiganalytics.scheduler.rest.Model;
import com.thinkbiganalytics.scheduler.rest.model.ScheduleIdentifier;
import com.thinkbiganalytics.scheduler.rest.model.ScheduledJob;
import com.thinkbiganalytics.scheduler.rest.model.TriggerInfo;
import com.thinkbiganalytics.security.AccessController;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Rest Controller for Quartz Scheduler
 */
@Api(tags = "Operations Manager - Scheduler", produces = "application/json")
@Path(SchedulerRestController.V1_SCHEDULER)
public class SchedulerRestController {

    public static final String V1_SCHEDULER = "/v1/scheduler";

    @Inject
    private JobScheduler quartzScheduler;

    @Inject
    private AccessController accessController;

    
    @GET
    @Path("/metadata")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the scheduler status.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the metadata.", response = Map.class)
    )
    public Map<String, Object> getMetaData() throws JobSchedulerException {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        
        return quartzScheduler.getMetaData();
    }

    @POST
    @Path("/pause")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Pauses the scheduler.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "The scheduler is paused.", response = RestResponseStatus.class)
    )
    public RestResponseStatus standByScheduler() throws JobSchedulerException {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);
        
        quartzScheduler.pauseScheduler();
        return RestResponseStatus.SUCCESS;
    }

    @POST
    @Path("/resume")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Starts the scheduler.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "The scheduler is started.", response = RestResponseStatus.class)
    )
    public RestResponseStatus startScheduler() throws JobSchedulerException {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);
        
        quartzScheduler.startScheduler();
        return RestResponseStatus.SUCCESS;
    }


    @POST
    @Path("/triggers/update")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Updates the trigger for a job.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "The trigger is updated.", response = RestResponseStatus.class)
    )
    public RestResponseStatus rescheduleTrigger(TriggerInfo trigger) throws JobSchedulerException {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);
        
        //convert to Domain Code
        quartzScheduler
            .updateTrigger(Model.TRIGGER_IDENTIFIER_TO_DOMAIN.apply(trigger.getTriggerIdentifier()), trigger.getCronExpression());
        return RestResponseStatus.SUCCESS;
    }


    @POST
    @Path("/triggers/pause")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Pauses the trigger for a job.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "The trigger is paused.", response = RestResponseStatus.class)
    )
    public RestResponseStatus pauseTrigger(ScheduleIdentifier triggerIdentifier) throws JobSchedulerException {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);
        
        quartzScheduler.pauseTrigger(Model.TRIGGER_IDENTIFIER_TO_DOMAIN.apply(triggerIdentifier));
        return RestResponseStatus.SUCCESS;

    }

    @POST
    @Path("/triggers/resume")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Starts the trigger for a job.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "The trigger is started.", response = RestResponseStatus.class)
    )
    public RestResponseStatus resumeTrigger(ScheduleIdentifier triggerIdentifier) throws JobSchedulerException {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);
        
        quartzScheduler.resumeTrigger(Model.TRIGGER_IDENTIFIER_TO_DOMAIN.apply(triggerIdentifier));
        return RestResponseStatus.SUCCESS;

    }

    @POST
    @Path("/jobs/trigger")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Executes the specified job.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "The job is started.", response = RestResponseStatus.class)
    )
    public RestResponseStatus triggerJob(ScheduleIdentifier jobIdentifier) throws JobSchedulerException {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);
        
        quartzScheduler.triggerJob(Model.JOB_IDENTIFIER_TO_DOMAIN.apply(jobIdentifier));
        return RestResponseStatus.SUCCESS;
    }

    @POST
    @Path("/jobs/pause")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Pauses all triggers for the specified job.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "The triggers are paused.", response = RestResponseStatus.class)
    )
    public RestResponseStatus pauseJob(ScheduleIdentifier jobIdentifier) throws JobSchedulerException {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);
        
        quartzScheduler.pauseTriggersOnJob(Model.JOB_IDENTIFIER_TO_DOMAIN.apply(jobIdentifier));
        return RestResponseStatus.SUCCESS;
    }

    @POST
    @Path("/jobs/resume")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Starts all triggers for the specified job.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "The triggers are started.", response = RestResponseStatus.class)
    )
    public RestResponseStatus resumeJob(ScheduleIdentifier jobIdentifier) throws JobSchedulerException {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ADMIN_OPS);
        
        quartzScheduler.resumeTriggersOnJob(Model.JOB_IDENTIFIER_TO_DOMAIN.apply(jobIdentifier));
        return RestResponseStatus.SUCCESS;
    }

    @GET
    @Path("/jobs")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the list of all jobs")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the list of jobs.", response = RestResponseStatus.class)
    )
    public List<ScheduledJob> getJobs() throws JobSchedulerException {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
        
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

    @DELETE
    @Path("/job/{group}/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Deletes a specific job by its group and name")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Deletes a job by its group and name", response = RestResponseStatus.class)
    )
    public Response deleteJob(@PathParam("group") String group, @PathParam("name")String name) throws JobSchedulerException {
        this.accessController.checkPermission(AccessController.SERVICES, OperationsAccessControl.ACCESS_OPS);
       if(StringUtils.isNotBlank(group) && StringUtils.isNotBlank(name)) {
           DefaultJobIdentifier jobIdentifier = new DefaultJobIdentifier(name,group);
           if( ((QuartzScheduler)quartzScheduler).jobExists(jobIdentifier)){
               quartzScheduler.deleteJob(jobIdentifier);
               return Response.ok(RestResponseStatus.SUCCESS).build();
           }
       }
       return Response.ok(new RestResponseStatus.ResponseStatusBuilder().message("Unable to find a scheduled job for the group: "+group+" and name: "+name).buildError()).build();
    }

}
