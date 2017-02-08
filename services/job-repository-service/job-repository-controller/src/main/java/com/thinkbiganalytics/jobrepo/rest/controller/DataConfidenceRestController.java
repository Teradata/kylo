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

import com.thinkbiganalytics.jobrepo.query.model.CheckDataJob;
import com.thinkbiganalytics.jobrepo.query.model.DataConfidenceSummary;
import com.thinkbiganalytics.jobrepo.query.model.transform.JobModelTransform;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.LatestFeedJobExecution;
import com.thinkbiganalytics.metadata.jpa.feed.OpsFeedManagerFeedProvider;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Operations Manager - Data Confidence", produces = "application/json")
@Path("/v1/data-confidence")
public class DataConfidenceRestController {

    @Inject
    OpsFeedManagerFeedProvider feedManagerFeedProvider;

    @Inject
    private MetadataAccess metadataAccess;

    @GET
    @Path("/summary")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation("Gets the data confidence metrics.")
    @ApiResponses(
        @ApiResponse(code = 200, message = "Returns the data confidence metrics.", response = DataConfidenceSummary.class)
    )
    public DataConfidenceSummary getDataConfidenceSummary() {
        DataConfidenceSummary summary = null;
        return metadataAccess.read(() -> {

            List<? extends LatestFeedJobExecution> latestCheckDataJobs = feedManagerFeedProvider.findLatestCheckDataJobs();

            if (latestCheckDataJobs != null) {
                List<CheckDataJob> checkDataJobs = latestCheckDataJobs.stream().map(latestFeedJobExecution -> JobModelTransform.checkDataJob(latestFeedJobExecution)).collect(Collectors.toList());
                return new DataConfidenceSummary(checkDataJobs, 60);
            } else {
                return new DataConfidenceSummary(Collections.emptyList(), 60);
            }
        });
    }
}
