/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event.feed;

import java.io.Serializable;

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;

/**
 *
 * @author Sean Felten
 */
public class OperationStatus implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final Feed.ID feedId;
    private final String feedName; // {category}.{feedSystemName}
    private final FeedOperation.ID operationId;
    private final FeedOperation.State state;
    private final String status;
    
    public OperationStatus(String feedName, FeedOperation.ID opId, FeedOperation.State state, String status) {
        this.feedId = null;
        this.feedName = feedName;
        this.operationId = opId;
        this.state = state;
        this.status = status;
    }
    
    public OperationStatus(Feed.ID id, FeedOperation.ID opId, FeedOperation.State state, String status) {
        this.feedId = id;
        this.feedName = null;
        this.operationId = opId;
        this.state = state;
        this.status = status;
    }

    public Feed.ID getFeedId() {
        return feedId;
    }
    
    public String getFeedName() {
        return feedName;
    }

    public FeedOperation.State getState() {
        return state;
    }
    
    public FeedOperation.ID getOperationId() {
        return operationId;
    }

    public String getStatus() {
        return status;
    }
}
