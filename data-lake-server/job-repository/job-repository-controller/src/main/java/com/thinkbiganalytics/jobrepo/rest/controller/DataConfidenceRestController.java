/*
 * Copyright (c) 2016.
 */

package com.thinkbiganalytics.jobrepo.rest.controller;

import com.thinkbiganalytics.jobrepo.query.model.CheckDataJob;
import com.thinkbiganalytics.jobrepo.query.model.DataConfidenceSummary;
import com.thinkbiganalytics.jobrepo.repository.CheckDataJobRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by sr186054 on 8/28/15. All Exceptions are handled by the CustomResponseEntityExceptionHandler.java class and
 * AngularHttpInterceptor.js
 */
@Path("/v1/data-confidence")
public class DataConfidenceRestController {

  private static final Logger LOG = LoggerFactory.getLogger(DataConfidenceRestController.class);

  @Inject
  CheckDataJobRepository checkDataJobRepository;

  @GET
  @Path("/summary")
  @Produces({MediaType.APPLICATION_JSON})
  public DataConfidenceSummary getDataConfidenceSummary() {
    DataConfidenceSummary summary = null;
    List<CheckDataJob> feeds = checkDataJobRepository.findLatestCheckDataJobs();
    summary = new DataConfidenceSummary(feeds, 60);
    return summary;
  }

  public void setCheckDataJobRepository(CheckDataJobRepository checkDataJobRepository) {
    this.checkDataJobRepository = checkDataJobRepository;
  }
}
