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
public interface FeedOperationProvider {

    FeedOperation getOperation(FeedOperation.ID id);
    
    List<FeedOperation> getOperations(Feed.ID feedId);
}
