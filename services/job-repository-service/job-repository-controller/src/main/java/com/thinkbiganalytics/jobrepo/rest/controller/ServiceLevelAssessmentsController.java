package com.thinkbiganalytics.jobrepo.rest.controller;
/*-
 * #%L
 * thinkbig-job-repository-controller
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

import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessmentTransform;
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.jobrepo.query.model.transform.ModelUtils;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessmentProvider;
import com.thinkbiganalytics.rest.model.RestResponseStatus;

import org.springframework.data.domain.Page;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *TODO move to separate module
 */
@Api(tags = "Operations Manager - Service Level Assessments", produces = "application/json")
@Path(ServiceLevelAssessmentsController.BASE)
public class ServiceLevelAssessmentsController {

    public static final String BASE = "/v1/sla/assessments";


    @Inject
    ServiceLevelAssessmentProvider serviceLevelAssessmentProvider;

    @Inject
    private MetadataAccess metadataAccess;


    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Lists all sla assessments.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the sla assessments.", response = SearchResult.class),
                      @ApiResponse(code = 400, message = "The sort cannot be empty.", response = RestResponseStatus.class),
                      @ApiResponse(code = 404, message = "The start or limit is not a valid integer.", response = RestResponseStatus.class),
                      @ApiResponse(code = 500, message = "The sort contains an invalid value.", response = RestResponseStatus.class)
                  })
    public SearchResult getAssessments(@QueryParam("sort") @DefaultValue("") String sort,
                                 @QueryParam("limit") @DefaultValue("10") Integer limit,
                                 @QueryParam("start") @DefaultValue("1") Integer start,
                                 @QueryParam("filter") String filter,
                                 @Context HttpServletRequest request) {
        return metadataAccess.read(() -> {
            Page<ServiceLevelAssessment> page = serviceLevelAssessmentProvider.findAll(filter, QueryUtils.pageRequest(start, limit, sort)).map(serviceLevelAssessment -> ServiceLevelAssessmentTransform
                .toModel(serviceLevelAssessment));
            return ModelUtils.toSearchResult(page);
        });

    }

    @GET
    @Path("/{assessmentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("get an SLA Assessment by the assessmentId")
    public ServiceLevelAssessment getAssessment(@PathParam("assessmentId")String assessmentId){
        ServiceLevelAssessment assessment = null;
        if(assessmentId != null) {
            assessment = metadataAccess.read(() -> {
                com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment.ID id = serviceLevelAssessmentProvider.resolveId(assessmentId);
                com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment serviceLevelAssessment = serviceLevelAssessmentProvider.findServiceLevelAssessment(id);
                if (serviceLevelAssessment != null) {
                    return ServiceLevelAssessmentTransform.toModel(serviceLevelAssessment);
                }
                return null;
            });
        }
        return assessment;

    }

}
