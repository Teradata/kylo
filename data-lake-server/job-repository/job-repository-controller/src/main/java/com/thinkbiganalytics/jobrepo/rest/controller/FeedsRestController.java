/*
 * Copyright (c) 2015.
 */

package com.thinkbiganalytics.jobrepo.rest.controller;

import com.thinkbiganalytics.jobrepo.query.feed.FeedQueryConstants;
import com.thinkbiganalytics.jobrepo.query.job.JobQueryConstants;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedFeed;
import com.thinkbiganalytics.jobrepo.query.model.FeedHealth;
import com.thinkbiganalytics.jobrepo.query.model.FeedStatus;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.SearchResult;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitution;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;
import com.thinkbiganalytics.jobrepo.query.support.QueryColumnFilterSqlString;
import com.thinkbiganalytics.jobrepo.repository.FeedRepository;
import com.thinkbiganalytics.jobrepo.rest.support.DataTableColumnFactory;
import com.thinkbiganalytics.jobrepo.rest.support.RestUtil;
import com.thinkbiganalytics.jobrepo.rest.support.WebColumnFilterUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
import javax.ws.rs.core.Response;

@Path("/v1/feeds")
public class FeedsRestController {

  private static final Logger LOG = LoggerFactory.getLogger(FeedsRestController.class);

  @Inject
  private FeedRepository feedRepository;


  @GET
  @Path("/{feedName}/latest")
  @Produces({MediaType.APPLICATION_JSON})
  public ExecutedFeed findLatestFeedsByName(@PathParam("feedName") String feedName, @Context HttpServletRequest request) {
    return feedRepository.findLastCompletedFeed(feedName);
  }


  @GET
  @Path("/")
  @Produces({MediaType.APPLICATION_JSON})
  public Response findFeeds(@QueryParam("sort") @DefaultValue("") String sort,
                            @QueryParam("limit") @DefaultValue("10") Integer limit,
                            @QueryParam("start") @DefaultValue("1") Integer start, @Context HttpServletRequest request) {
    List<ColumnFilter>
        filters =
        WebColumnFilterUtil.buildFiltersFromRequestForDatatable(request, DataTableColumnFactory.PIPELINE_DATA_TYPE.FEED);
    List<OrderBy> orderByList = RestUtil.buildOrderByList(sort, DataTableColumnFactory.PIPELINE_DATA_TYPE.FEED);
    SearchResult searchResult = feedRepository.getDataTablesSearchResult(filters, null, orderByList, start, limit);
    return Response.ok(searchResult).build();
  }

  @GET
  @Path("/since/{timeframe}")
  @Produces({MediaType.APPLICATION_JSON})
  public SearchResult findFeedActivity(@PathParam("timeframe") String timeframe,
                                       @QueryParam("sort") @DefaultValue("") String sort,
                                       @QueryParam("limit") @DefaultValue("10") Integer limit,
                                       @QueryParam("start") @DefaultValue("1") Integer start,
                                       @Context HttpServletRequest request) {
    List<ColumnFilter>
        filters =
        WebColumnFilterUtil.buildFiltersFromRequestForDatatable(request, DataTableColumnFactory.PIPELINE_DATA_TYPE.FEED);
    if (StringUtils.isNotBlank(timeframe)) {
      DatabaseQuerySubstitution.DATE_PART datePart = DatabaseQuerySubstitution.DATE_PART.valueOf(timeframe);
      if (datePart != null) {
        ColumnFilter filter = new QueryColumnFilterSqlString();
        String filterName = JobQueryConstants.DAY_DIFF_FROM_NOW;
        switch (datePart) {
          case DAY:
            filterName = JobQueryConstants.DAY_DIFF_FROM_NOW;
          case WEEK:
            filterName = JobQueryConstants.WEEK_DIFF_FROM_NOW;
          case MONTH:
            filterName = JobQueryConstants.MONTH_DIFF_FROM_NOW;
          case YEAR:
            filterName = JobQueryConstants.YEAR_DIFF_FROM_NOW;

        }
        filter.setName(filterName);


      }
    }
    List<OrderBy> orderByList = RestUtil.buildOrderByList(sort, DataTableColumnFactory.PIPELINE_DATA_TYPE.FEED);
    SearchResult searchResult = feedRepository.getDataTablesSearchResult(filters, null, orderByList, start, limit);
    return searchResult;
  }

  @GET
  @Path("/{feedName}/daily-status-count/{timeframe}")
  @Produces({MediaType.APPLICATION_JSON})
  public List<JobStatusCount> findFeedDailyStatusCount(@PathParam("feedName") String feedName,
                                                       @PathParam("timeframe") String timeframe) {
    return findFeedDailyStatusCount(feedName, timeframe, 1);
  }


  @GET
  @Path("/{feedName}/daily-status-count/{timeframe}/{amount}")
  @Produces({MediaType.APPLICATION_JSON})
  public List<JobStatusCount> findFeedDailyStatusCount(@PathParam("feedName") String feedName,
                                                       @PathParam("timeframe") String timeframe,
                                                       @PathParam("amount") Integer amount) {
    DatabaseQuerySubstitution.DATE_PART datePart = DatabaseQuerySubstitution.DATE_PART.DAY;
    if (StringUtils.isNotBlank(timeframe)) {
      try {
        datePart = DatabaseQuerySubstitution.DATE_PART.valueOf(timeframe.toUpperCase());
      } catch (IllegalArgumentException e) {

      }
    }
    ;

    List<JobStatusCount> list = feedRepository.getFeedStatusCountByDay(feedName, datePart, amount);
    return list;
  }


  @GET
  @Path("/running")
  @Produces({MediaType.APPLICATION_JSON})
  public SearchResult findRunningFeeds(@QueryParam("sort") @DefaultValue("") String sort,
                                       @QueryParam("limit") @DefaultValue("10") Integer limit,
                                       @QueryParam("start") @DefaultValue("1") Integer start,
                                       @Context HttpServletRequest request) {
    List<ColumnFilter>
        filters =
        WebColumnFilterUtil.buildFiltersFromRequestForDatatable(request, DataTableColumnFactory.PIPELINE_DATA_TYPE.FEED);
    ColumnFilter filter = new QueryColumnFilterSqlString();
    filter.setSqlString(" AND END_TIME IS NULL");
    filters.add(filter);
    List<OrderBy> orderByList = RestUtil.buildOrderByList(sort, DataTableColumnFactory.PIPELINE_DATA_TYPE.FEED);
    SearchResult searchResult = feedRepository.getDataTablesSearchResult(filters, null, orderByList, start, limit);
    return searchResult;
  }

  @GET
  @Path("/completed")
  @Produces({MediaType.APPLICATION_JSON})
  public SearchResult findCompletedFeeds(@QueryParam("sort") @DefaultValue("") String sort,
                                         @QueryParam("limit") @DefaultValue("10") Integer limit,
                                         @QueryParam("start") @DefaultValue("1") Integer start,
                                         @Context HttpServletRequest request) {
    List<ColumnFilter>
        filters =
        WebColumnFilterUtil.buildFiltersFromRequestForDatatable(request, DataTableColumnFactory.PIPELINE_DATA_TYPE.FEED);
    ColumnFilter filter = new QueryColumnFilterSqlString();
    filter.setSqlString(" AND STATUS = 'COMPLETED' AND EXIT_CODE = 'COMPLETED' ");
    filters.add(filter);
    List<OrderBy> orderByList = RestUtil.buildOrderByList(sort, DataTableColumnFactory.PIPELINE_DATA_TYPE.FEED);
    SearchResult searchResult = feedRepository.getDataTablesSearchResult(filters, null, orderByList, start, limit);
    return searchResult;
  }


  @GET
  @Path("/health")
  @Produces({MediaType.APPLICATION_JSON})
  public FeedStatus getFeedHealth(@Context HttpServletRequest request) {

    List<ColumnFilter> filters = WebColumnFilterUtil.buildFiltersFromRequest(request, null);
    if (filters == null) {
      filters = new ArrayList<ColumnFilter>();
    }
    return this.feedRepository.getFeedStatusAndSummary(filters);
  }

  @GET
  @Path("/health-count")
  @Produces({MediaType.APPLICATION_JSON})
  public List<FeedHealth> getFeedHealthCounts(@Context HttpServletRequest request) {

    return this.feedRepository.getFeedHealthCounts();
  }


  @GET
  @Path("/health-count/{feedName}")
  @Produces({MediaType.APPLICATION_JSON})
  public FeedHealth getFeedHealthCounts(@Context HttpServletRequest request, @PathParam("feedName") String feedName) {
    List<FeedHealth> feedHealthList = this.feedRepository.getFeedHealthCounts(feedName);
    if (feedHealthList != null && !feedHealthList.isEmpty()) {
      return feedHealthList.get(0);
    } else {
      return null;
    }
  }


  @GET
  @Path("/health/{feedName}")
  @Produces({MediaType.APPLICATION_JSON})
  public FeedStatus getFeedHealthForFeed(@Context HttpServletRequest request, @PathParam("feedName") String feedName) {

    List<ColumnFilter> filters = WebColumnFilterUtil.buildFiltersFromRequest(request, null);
    if (filters == null) {
      filters = new ArrayList<ColumnFilter>();
    }
    filters.add(new QueryColumnFilterSqlString(FeedQueryConstants.QUERY_FEED_NAME_COLUMN, feedName));
    return this.feedRepository.getFeedStatusAndSummary(filters);
  }

  @GET
  @Path("/names")
  @Produces({MediaType.APPLICATION_JSON})
  public List<String> getFeedNames() {
    return feedRepository.getFeedNames();
  }

}
