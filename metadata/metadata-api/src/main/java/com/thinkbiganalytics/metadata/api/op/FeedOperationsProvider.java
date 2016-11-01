/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import com.thinkbiganalytics.metadata.api.feed.Feed;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Sean Felten
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
    List<FeedOperation> find(Feed.ID feedId);

    boolean isFeedRunning(Feed.ID feedId);

    // List<FeedOperation> find(Feed.ID feedId, int limit);

    /**
     * Get a listing of all the Dependent Job Executions and their associated executionContext data Map for the supplied {@code feedId}
     * @param feedId the feed that has dependents
     * @param props  filter to include only these property names from the respective job execution context.  null or empty set will return all data in the execution context
     * @return
     */
    FeedDependencyDeltaResults getDependentDeltaResults(Feed.ID feedId, Set<String> props);

    //  Map<DateTime, Map<String, Object>> getAllResults(FeedOperationCriteria criteria, Set<String> props);
}
