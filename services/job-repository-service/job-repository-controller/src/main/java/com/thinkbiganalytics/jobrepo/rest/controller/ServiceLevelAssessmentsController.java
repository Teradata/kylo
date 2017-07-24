package com.thinkbiganalytics.jobrepo.rest.controller;

import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.SearchResult;
import com.thinkbiganalytics.jobrepo.query.model.transform.JobModelTransform;
import com.thinkbiganalytics.jobrepo.query.model.transform.ModelUtils;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessmentProvider;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.sla.model.DefaultServiceLevelAssessment;
import com.thinkbiganalytics.sla.model.ServiceLevelAssessmentTransform;

import org.springframework.data.domain.Page;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Created by sr186054 on 7/23/17.
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
    @ApiOperation("Lists all jobs.")
    @ApiResponses({
                      @ApiResponse(code = 200, message = "Returns the jobs.", response = SearchResult.class),
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
            Page<DefaultServiceLevelAssessment> page = serviceLevelAssessmentProvider.findAll(filter, QueryUtils.pageRequest(start, limit, sort)).map(serviceLevelAssessment -> ServiceLevelAssessmentTransform.toModel(serviceLevelAssessment));
            return ModelUtils.toSearchResult(page);
        });


    }

}
