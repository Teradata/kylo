package com.thinkbiganalytics.feedmgr.rest.controller;

import com.thinkbiganalytics.feedmgr.service.UIService;
import com.thinkbiganalytics.rest.JerseyClientException;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;

import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;

/**
 * Created by sr186054 on 1/13/16.
 */
@Api(value = "feed-manager-util", produces = "application/json")
@Path("/v1/feedmgr/util")
@Component
public class UtilityRestController {

    @Autowired
    Environment env;

    public UtilityRestController() {
    }



    @GET
    @Path("/cron-expression/validate")
    @Produces({MediaType.APPLICATION_JSON })
    public Response validateCronExpression(@QueryParam("cronExpression") String cronExpression) throws JerseyClientException{
        boolean valid = CronExpression.isValidExpression(cronExpression);
       return Response.ok("{\"valid\":"+valid+"}").build();
    }
    @GET
    @Path("/cron-expression/preview")
    @Produces({MediaType.APPLICATION_JSON })
    public Response previewCronExpression(@QueryParam("cronExpression") String cronExpression, @QueryParam("number") @DefaultValue("3")  Integer number) throws JerseyClientException{
        List<Date> dates = new ArrayList<>();
        try {
            dates = CronExpressionUtil.getNextFireTimes(cronExpression, number);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Response.ok(dates).build();
    }


    @GET
    @Path("/codemirror-types")
    @Produces({MediaType.APPLICATION_JSON })
    public Response codeMirrorTypes() throws JerseyClientException{
       Map<String,String> types = UIService.getInstance().getCodeMirrorTypes();
        return Response.ok(types).build();
    }


    @GET
    @Path("/pipeline-controller/url")
    @Produces(MediaType.TEXT_PLAIN)
    public Response pipelineControllerUrl(){
       String url = env.getProperty("thinkbig.pipelinecontroller.url");
        return Response.ok(url).build();
    }

}
