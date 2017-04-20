/**
 * 
 */
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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 */
public interface FeedOpsAccessControlRepository extends JpaRepository<JpaFeedOpsAclEntry, JpaFeedOpsAclEntry.EntryId> {
    
    @Query("select entry from JpaFeedOpsAclEntry as entry where entry.feedId = :id")
    List<JpaFeedOpsAclEntry> findForFeed(@Param("id") UUID feedId); 
    
    @Modifying
    @Query("delete from JpaFeedOpsAclEntry as entry where entry.principalName in (:names)")
    int deleteForPrincipals(@Param("names") Set<String> principalNames); 
    
    @Modifying
    @Query("delete from JpaFeedOpsAclEntry as entry where entry.feedId = :id")
    int deleteForFeed(@Param("id") UUID feedId); 

}
