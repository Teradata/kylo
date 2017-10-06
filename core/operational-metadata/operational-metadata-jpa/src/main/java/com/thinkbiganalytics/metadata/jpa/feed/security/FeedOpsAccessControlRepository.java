package com.thinkbiganalytics.metadata.jpa.feed.security;

/*-
 * #%L
 * kylo-operational-metadata-jpa
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

import com.thinkbiganalytics.metadata.jpa.feed.FeedHealthSecuringRepository;
import com.thinkbiganalytics.metadata.jpa.feed.RepositoryType;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Repository for feed access control lists.
 */
public interface FeedOpsAccessControlRepository extends JpaRepository<JpaFeedOpsAclEntry, JpaFeedOpsAclEntry.EntryId> {

    /**
     * Predicate for selecting matching principals in WHERE clause.
     */
    String WHERE_PRINCIPALS_MATCH = " ((acl.principalType = 'USER' AND acl.principalName = :#{user.name}) "
                    + " OR (acl.principalType = 'GROUP' AND acl.principalName in :#{user.groups})) ";


    /**
     * Join statement for selecting only feeds accessible to the current principal.
     */
    String JOIN_ACL_TO_FEED = " join JpaFeedOpsAclEntry as acl on feed.id = acl.feedId ";

    /**
     * Join statement for selecting only jobs accessible to the current principal.
     */
    String JOIN_ACL_TO_JOB =" join JpaFeedOpsAclEntry as acl on job.jobInstance.feed.id = acl.feedId ";

    /**
     * Join statement for selecting only jobs executions accessible to the current principal.
     */
    String JOIN_ACL_TO_JOB_EXECUTION =" join JpaFeedOpsAclEntry as acl on jobExecution.feed.id = acl.feedId ";


    @Query("select entry from JpaFeedOpsAclEntry as entry where entry.feedId = :id")
    List<JpaFeedOpsAclEntry> findForFeed(@Param("id") UUID feedId);

   // @Query("select distinct entry.feedId from JpaFeedOpsAclEntry as entry where entry.principalName in (:names)")
  //  Set<UUID> findFeedIdsForPrincipals(@Param("names") Set<String> principalNames);

    @Query("select entry from JpaFeedOpsAclEntry as entry where entry.principalName in (:names)")
    Set<JpaFeedOpsAclEntry> findForPrincipals(@Param("names") Set<String> principalNames);

    @Modifying
    @Query("delete from JpaFeedOpsAclEntry as entry where entry.principalName in (:names)")
    int deleteForPrincipals(@Param("names") Set<String> principalNames);

    @Modifying
    @Query("delete from JpaFeedOpsAclEntry as entry where entry.feedId = :id")
    int deleteForFeed(@Param("id") UUID feedId);


}
