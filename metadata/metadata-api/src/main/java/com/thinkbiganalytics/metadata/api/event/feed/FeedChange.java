/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event.feed;

import java.util.Objects;

import com.thinkbiganalytics.metadata.api.event.MetadataChange;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.Feed.ID;
import com.thinkbiganalytics.metadata.api.feed.Feed.State;

/**
 *
 * @author Sean Felten
 */
public class FeedChange extends MetadataChange {

    private static final long serialVersionUID = 1L;
    
    private final Feed.ID feedId;
    private final Feed.State feedState;
    
    public FeedChange(ChangeType change, ID feedId, State feedState) {
        this(change, "", feedId, feedState);
    }
    
    public FeedChange(ChangeType change, String descr, ID feedId, State feedState) {
        super(change, descr);
        this.feedId = feedId;
        this.feedState = feedState;
    }
    
    public Feed.ID getFeedId() {
        return feedId;
    }

    public Feed.State getFeedState() {
        return feedState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.feedState, this.feedId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FeedChange) {
            FeedChange that = (FeedChange) obj;
            return super.equals(that) &&
                   Objects.equals(this.feedId, that.feedId) &&
                   Objects.equals(this.feedState, that.feedState);
        } else {
            return false;
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Feed change ");
        return sb
                        .append("(").append(getChange()).append(") - ")
                        .append("ID: ").append(this.feedId)
                        .append(" feed state: ").append(this.feedState)
                        .toString();
            
    }
}
