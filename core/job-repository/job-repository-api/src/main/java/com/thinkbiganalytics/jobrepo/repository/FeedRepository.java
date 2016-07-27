package com.thinkbiganalytics.jobrepo.repository;

import com.thinkbiganalytics.jobrepo.query.model.ExecutedFeed;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.FeedHealth;
import com.thinkbiganalytics.jobrepo.query.model.FeedStatus;
import com.thinkbiganalytics.jobrepo.query.model.JobStatusCount;
import com.thinkbiganalytics.jobrepo.query.model.SearchResult;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitution;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created by Will Peterson on 6/6/15. <p> A simple repository interface for feed
 */
public interface FeedRepository {


  /**
   * For each Feed it will return the Last Feed  that successfully processed data Optionally pass in a Filter if you want to
   * Filter on a specific column of the Feeds
   */
  public List<ExecutedFeed> findLastCompletedFeeds(List<ColumnFilter> filters);

  /**
   * For a specific Feed find the last feed that successfully processed data
   */
  public ExecutedFeed findLastCompletedFeed(String feedName);


  /**
   * Search the Feeds and for some records
   */
  public List<ExecutedFeed> findFeeds(List<ColumnFilter> conditions, List<OrderBy> order, final Integer start,
                                      final Integer limit);

  public List<Object> selectDistinctColumnValues(List<ColumnFilter> filters, String columnName);

  public SearchResult getDataTablesSearchResult(List<ColumnFilter> conditions, List<ColumnFilter> defaultFilters,
                                                List<OrderBy> order, Integer start, Integer limit);

  public Long selectCount(List<ColumnFilter> filters);

  /**
   * Return a list containing the feed with the specified feedInstanceId
   *
   * @param feedInstanceId The feed value
   * @return A list of executed jobs
   */
  List<ExecutedFeed> findFeedByInstanceId(String feedInstanceId);

  /**
   * Return a list of all feeds with the specified feed value
   *
   * @param feed  The feed value
   * @param limit The maximum number of results to return
   * @return A list of executed jobs
   */
  public List<ExecutedJob> findJobsByFeed(final String feed, final int limit);


  public List<JobStatusCount> getFeedStatusCountByDay(String feedName, DatabaseQuerySubstitution.DATE_PART datePart,
                                                      Integer interval);

  /**
   * Return a list of all unique feed names that have been executed at least once
   *
   * @return A list of job names
   */
  public List<String> uniqueFeedNames();

  /**
   * Returns generic statistics about current and historically run feeds;
   */
  public FeedStatus getFeedStatusAndSummary(List<ColumnFilter> filters);


  public List<FeedHealth> getFeedHealthCounts();

  public List<FeedHealth> getFeedHealthCounts(String feedName);

  public List<String> getFeedNames();

  /**
   * Deletes the specified feed.
   *
   * @param category the system category name
   * @param feed the system feed name
   * @throws IllegalStateException if the feed cannot be deleted
   */
  void deleteFeed(@Nonnull final String category, @Nonnull final String feed);
}
