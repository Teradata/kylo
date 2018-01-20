package com.thinkbiganalytics.metadata.jpa.jobrepo.nifi;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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

import com.thinkbiganalytics.metadata.jpa.feed.security.FeedOpsAccessControlRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring data repository for {@link JpaNifiFeedStats}
 */
public interface NifiFeedStatisticsRepository extends JpaRepository<JpaNifiFeedStats, String>, QueryDslPredicateExecutor<JpaNifiFeedStats> {

    @Query(value = "select distinct stats from JpaNifiFeedStats as stats "
                   + "join JpaOpsManagerFeed as feed on feed.name = stats.feedName "
                   + FeedOpsAccessControlRepository.JOIN_ACL_TO_FEED + " and (" + FeedOpsAccessControlRepository.WHERE_PRINCIPALS_MATCH + ") "
                   + "where stats.feedName = :feedName ")
    JpaNifiFeedStats findLatestForFeedWithAcl(@Param("feedName") String feedName);

    @Query(value = "select distinct stats from JpaNifiFeedStats as stats "
                   + "join JpaOpsManagerFeed as feed on feed.name = stats.feedName "
                   + "where stats.feedName = :feedName ")
    JpaNifiFeedStats findLatestForFeedWithoutAcl(@Param("feedName") String feedName);

    @Query(value = "select distinct stats from JpaNifiFeedStats as stats "
                   + "where stats.feedName = :feedName ")
    List<JpaNifiFeedStats> findForFeedWithoutAcl(@Param("feedName") String feedName);

    @Query(value = "select distinct stats from JpaNifiFeedStats stats "
                   + "join JpaOpsManagerFeed as feed on feed.name = stats.feedName "
                   + FeedOpsAccessControlRepository.JOIN_ACL_TO_FEED + " and (" + FeedOpsAccessControlRepository.WHERE_PRINCIPALS_MATCH + ") "
                   + "where feed.isStream = true ")
    List<JpaNifiFeedStats> findStreamingFeedStatsWithAcl();

    @Query(value = "select distinct stats from JpaNifiFeedStats stats "
                   + "join JpaOpsManagerFeed as feed on feed.name = stats.feedName "
                   + "where feed.isStream = true ")
    List<JpaNifiFeedStats> findStreamingFeedStatsWithoutAcl();

    @Query(value = "select distinct stats from JpaNifiFeedStats stats "
                   + "join JpaOpsManagerFeed as feed on feed.name = stats.feedName "
                   + FeedOpsAccessControlRepository.JOIN_ACL_TO_FEED + " and (" + FeedOpsAccessControlRepository.WHERE_PRINCIPALS_MATCH + ") ")
    List<JpaNifiFeedStats> findFeedStatsWithAcl();

    @Query(value = "select distinct stats from JpaNifiFeedStats stats "
                   + "join JpaOpsManagerFeed as feed on feed.name = stats.feedName ")
    List<JpaNifiFeedStats> findFeedStatsWithoutAcl();


}
