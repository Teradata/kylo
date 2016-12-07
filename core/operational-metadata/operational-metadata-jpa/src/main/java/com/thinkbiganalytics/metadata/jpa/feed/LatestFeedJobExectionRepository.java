package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.metadata.api.feed.LatestFeedJobExecution;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by sr186054 on 12/6/16.
 */
public interface LatestFeedJobExectionRepository extends JpaRepository<JpaLatestFeedJobExecution, Long> {


    List<JpaLatestFeedJobExecution> findByFeedType(String feedType);

    @Query("select v from JpaLatestFeedJobExecution as v where v.feedType = 'CHECK'")
    List<JpaLatestFeedJobExecution> findCheckDataJobs();

}
