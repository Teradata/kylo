/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event;

import com.thinkbiganalytics.metadata.api.MetadataException;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.Feed.ID;

/**
 *
 * @author Sean Felten
 */
public class FeedNotFoundExcepton extends MetadataException {

    private static final long serialVersionUID = 3867336790441208367L;

    private Feed.ID id;

    public FeedNotFoundExcepton(ID id) {
        super();
        this.id = id;
    }
    
    public FeedNotFoundExcepton(String message, ID id) {
        super(message);
        this.id = id;
    }

    public Feed.ID getId() {
        return id;
    }
}
