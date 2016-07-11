/**
 * 
 */
package com.thinkbiganalytics.metadata.api.op;

import org.joda.time.DateTime;

import com.thinkbiganalytics.metadata.api.MetadataCriteria;
import com.thinkbiganalytics.metadata.api.feed.Feed;

/**
 *
 * @author Sean Felten
 */
public interface FeedOperationCriteria extends MetadataCriteria<FeedOperationCriteria> {

    FeedOperationCriteria state(FeedOperation.State... states);
    FeedOperationCriteria feed(Feed.ID... feedIds);
    
    FeedOperationCriteria startedSince(DateTime time);
    FeedOperationCriteria startedBefore(DateTime time);
    FeedOperationCriteria startedBetween(DateTime after, DateTime before);
    
    FeedOperationCriteria stoppedSince(DateTime time);
    FeedOperationCriteria stoppedBefore(DateTime time);
    FeedOperationCriteria stoppedBetween(DateTime after, DateTime before);
}
