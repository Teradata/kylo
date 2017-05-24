package com.thinkbiganalytics.metadata.jpa.feed;

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

import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.jpa.feed.security.FeedOpsAccessControlRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring data repository for accessing {@link JpaOpsManagerFeed}
 */
@RepositoryType(FeedSecuringRepository.class)
public interface OpsManagerFeedRepository extends JpaRepository<JpaOpsManagerFeed, JpaOpsManagerFeed.ID>, QueryDslPredicateExecutor<JpaOpsManagerFeed> {

    JpaOpsManagerFeed findByName(String name);

    @Query("select feed from JpaOpsManagerFeed as feed "
                    + FeedOpsAccessControlRepository.JOIN_ACL_TO_FEED
                    + " where feed.id in(:ids)"
                    + " and "+FeedOpsAccessControlRepository.WHERE_PRINCIPALS_MATCH)
    List<JpaOpsManagerFeed> findByFeedIds(@Param("ids") List<OpsManagerFeed.ID> ids);

    @Query("SELECT distinct feed.name FROM JpaOpsManagerFeed AS feed "
           + FeedOpsAccessControlRepository.JOIN_ACL_TO_FEED
           + "where "+FeedOpsAccessControlRepository.WHERE_PRINCIPALS_MATCH)
    List<String> getFeedNames();

    @Procedure(name = "OpsManagerFeed.deleteFeedJobs")
    Integer deleteFeedJobs(@Param("category") String category, @Param("feed") String feed);

    @Procedure(name = "OpsManagerFeed.abandonFeedJobs")
    Integer abandonFeedJobs(@Param("feed") String feed, @Param("exitMessage") String exitMessage);
}
