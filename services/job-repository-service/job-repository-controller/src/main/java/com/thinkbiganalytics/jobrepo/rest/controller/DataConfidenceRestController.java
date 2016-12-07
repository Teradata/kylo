/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.rest.controller;

import com.thinkbiganalytics.jobrepo.query.model.CheckDataJob;
import com.thinkbiganalytics.jobrepo.query.model.DataConfidenceSummary;
import com.thinkbiganalytics.jobrepo.query.model.transform.JobModelTransform;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.LatestFeedJobExecution;
import com.thinkbiganalytics.metadata.jpa.feed.OpsFeedManagerFeedProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;


@Api(value = "data-confidence", produces = "application/json")
@Path("/v1/data-confidence")
public class DataConfidenceRestController {

    private static final Logger LOG = LoggerFactory.getLogger(DataConfidenceRestController.class);


    @Inject
    OpsFeedManagerFeedProvider feedManagerFeedProvider;

    @Inject
    private MetadataAccess metadataAccess;

    @GET
    @Path("/summary")
    @Produces({MediaType.APPLICATION_JSON})
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
