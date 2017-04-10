package com.thinkbiganalytics.metadata.jpa.feed;

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

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import java.util.List;

/**
 * This is an example repository which shows how to refer to principal roles in @Query annotations and also
 * to extend SecuredFeedRepository which knows how to construct queries with reference to FeedAclIndex table
 */
public interface TestOpsManagerFeedRepository extends SecuredFeedRepository<JpaOpsManagerFeed, JpaOpsManagerFeed.ID>, QueryDslPredicateExecutor<JpaOpsManagerFeed> {

    @Query("select feed.name from JpaOpsManagerFeed as feed where feed.name = :#{principal.username}")
    List<String> getFeedNamesWithPrincipal();

    @Query("select f.name from JpaOpsManagerFeed as f where "
           + "exists ("
           + "select 1 from JpaFeedOpsAclEntry as x where f.id = x.feedId and x.principalName in :#{principal.roleSet}"
           + ")")
    List<String> getFeedNames();

}
