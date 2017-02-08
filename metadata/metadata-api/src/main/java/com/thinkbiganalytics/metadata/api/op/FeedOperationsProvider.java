/**
 *
 */
package com.thinkbiganalytics.metadata.api.op;

/*-
 * #%L
 * thinkbig-metadata-api
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

import com.thinkbiganalytics.metadata.api.feed.Feed;

import java.util.List;
import java.util.Set;

/**
 *
 */
public interface FeedOperationsProvider {

    FeedOperationCriteria criteria();

    /**
     * Get the FeedOperation for the supplied id
     */
    FeedOperation getOperation(FeedOperation.ID id);

    //  List<FeedOperation> find(FeedOperationCriteria criteria);

    /**
     * Find the last Completed Feed Operation for the {@code feedId}
     */
    List<FeedOperation> findLatestCompleted(Feed.ID feedId);

    /**
     * Find the last Feed Operation of any status for the {@code feedId}
     */
    List<FeedOperation> findLatest(Feed.ID feedId);

    boolean isFeedRunning(Feed.ID feedId);

    // List<FeedOperation> find(Feed.ID feedId, int limit);

    /**
     * Get a listing of all the Dependent Job Executions and their associated executionContext data Map for the supplied {@code feedId}
     *
     * @param feedId the feed that has dependents
     * @param props  filter to include only these property names from the respective job execution context.  null or empty set will return all data in the execution context
     */
    FeedDependencyDeltaResults getDependentDeltaResults(Feed.ID feedId, Set<String> props);

    //  Map<DateTime, Map<String, Object>> getAllResults(FeedOperationCriteria criteria, Set<String> props);
}
