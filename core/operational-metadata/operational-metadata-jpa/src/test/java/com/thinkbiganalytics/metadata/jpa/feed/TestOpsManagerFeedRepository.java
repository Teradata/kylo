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

import com.thinkbiganalytics.metadata.jpa.feed.security.FeedOpsAccessControlRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import java.util.List;

/**
 * This is an example repository which shows how to refer to principal roles in @Query annotations and also
 * to how secure repository methods with QueryAugmentor
 */
@RepositoryType(TestFeedSecuringRepository.class)
public interface TestOpsManagerFeedRepository extends JpaRepository<JpaOpsManagerFeed, JpaOpsManagerFeed.ID>, QueryDslPredicateExecutor<JpaOpsManagerFeed> {

    @Query("select feed.name from JpaOpsManagerFeed as feed where feed.name = :#{user.name}")
    List<String> getFeedNamesForCurrentUser();

    @Query("select distinct feed.name from JpaOpsManagerFeed as feed "
           + FeedOpsAccessControlRepository.JOIN_ACL_TO_FEED
           +" where "+FeedOpsAccessControlRepository.WHERE_PRINCIPALS_MATCH)
    List<String> getFeedNames();

    List<JpaOpsManagerFeed> findByName(String name);
}
