package com.thinkbiganalytics.metadata.jpa.feed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import java.util.List;

/**
 * Created by ru186002 on 07/04/2017.
 */
public interface TestOpsManagerFeedRepository extends JpaRepository<JpaOpsManagerFeed, JpaOpsManagerFeed.ID>, QueryDslPredicateExecutor<JpaOpsManagerFeed> {

    @Query("select feed.name from JpaOpsManagerFeed as feed where feed.name = :#{principal.username}")
    List<String> getFeedNamesWithPrincipal();

    @Query("select f.name from JpaOpsManagerFeed as f where "
           + "exists ("
           + "select 1 from JpaFeedOpsAclEntry as x where f.id = x.feedId and x.principalName in :#{principal.roleSet}"
           + ")")
    List<String> getFeedNames();

}
