package com.thinkbiganalytics.spark.rest;

/*-
 * #%L
 * kylo-spark-shell-client-app
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobRequest;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResponse;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResult;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.spark.metadata.SparkJob;
import com.thinkbiganalytics.spark.service.TransformService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
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

@Api(tags = "spark")
@Component
@Path("/api/v1/spark/shell/job")
@Profile("kylo-shell")
@SuppressWarnings("WeakerAccess")
public class SparkJobController {

    private static final Logger log = LoggerFactory.getLogger(SparkJobController.class);

    @Autowired
    @Qualifier("sparkShellMessages")
    MessageSource messages;

    @Inject
    HttpServletRequest request;

    @Inject
    TransformService transformService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the job.", response = SparkJobResponse.class),
                      @ApiResponse(code = 400, message = "The requested job does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem processing the job.", response = SparkJobResponse.class)
                  })
    public Response createJob(final SparkJobRequest request) {
        // Validate request
        if (request == null || request.getScript() == null) {
            throw new BadRequestException(getMessage("job.missing-script"));
        }
        if (request.getParent() != null && request.getParent().getId() == null) {
            throw new BadRequestException(getMessage("job.missing-parent-id"));
        }

        // Execute request
        try {
            SparkJobResponse response = transformService.submit(request);
            return Response.ok(response).build();
        } catch (final ScriptException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @GET
    @Path("/{job}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Fetches the status of a job")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the status of the job.", response = SparkJobResponse.class),
                      @ApiResponse(code = 404, message = "The job does not exist.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "There was a problem accessing the data.", response = SparkJobResponse.class)
                  })
    public Response getJobResult(@PathParam("job") final String id) {
        try {
            final SparkJob job = transformService.getSparkJob(id);
            final SparkJobResponse response = new SparkJobResponse();
            response.setId(job.getGroupId());

            if (job.isDone()) {
                final SparkJobResult result = job.get();
                response.setResult(result);
                response.setStatus(SparkJobResponse.Status.SUCCESS);
            } else {
                response.setStatus(SparkJobResponse.Status.PENDING);
            }

            return Response.ok(response).build();
        } catch (final IllegalArgumentException e) {
            throw new NotFoundException(getMessage("job.not-found"));
        } catch (final Exception e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Nonnull
    private String getMessage(@Nonnull final String code) {
        try {
            return messages.getMessage(code, null, request.getLocale());
        } catch (final NoSuchMessageException e) {
            log.debug("Missing message for code: {}", code);
            return code;
        }
    }
}
