/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import java.util.List;

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
}
