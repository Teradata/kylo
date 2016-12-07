package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface BatchFeedSummaryCountsRepository extends JpaRepository<JpaBatchFeedSummaryCounts, JpaOpsManagerFeed.ID> {



}
