/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.feed.Feed;

/**
 *
 * @author Sean Felten
 */
public interface FeedOperationsProvider {
    
    FeedOperationCriteria criteria();

    FeedOperation getOperation(FeedOperation.ID id);
    
    List<FeedOperation> find(FeedOperationCriteria criteria);
    
    List<FeedOperation> find(Feed.ID feedId);
    List<FeedOperation> find(Feed.ID feedId, int limit);
    
    Map<DateTime, Map<String, Object>> getDependentDeltaResults(Feed.ID feedId, Set<String> props);
    
    Map<DateTime, Map<String, Object>> getAllResults(FeedOperationCriteria criteria, Set<String> props);
}
