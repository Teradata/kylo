package com.thinkbiganalytics.metadata.jpa.feed;

import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by sr186054 on 8/23/16.
 */
public interface OpsManagerFeedRepository extends JpaRepository<JpaOpsManagerFeed, JpaOpsManagerFeed.ID>, QueryDslPredicateExecutor<JpaOpsManagerFeed> {


    JpaOpsManagerFeed findByName(String name);

    @Query("select feed from JpaOpsManagerFeed as feed where feed.id in(:ids)")
    List<JpaOpsManagerFeed> findByFeedIds(@Param("ids") List<OpsManagerFeed.ID> ids);


    @Query("select feed.name from JpaOpsManagerFeed as feed")
    List<String> getFeedNames();


    @Procedure(name = "OpsManagerFeed.deleteFeedJobs")
    void deleteFeedJobs(@Param("category") String category,@Param("feed") String feed);

}
