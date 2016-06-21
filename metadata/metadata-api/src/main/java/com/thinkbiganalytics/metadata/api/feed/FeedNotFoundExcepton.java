/**
 * 
 */
package com.thinkbiganalytics.metadata.api.feed;

import com.thinkbiganalytics.metadata.api.MetadataException;

/**
 *
 * @author Sean Felten
 */
public class FeedNotFoundExcepton extends MetadataException {

    private static final long serialVersionUID = 3867336790441208367L;

    private Feed.ID id;

    public FeedNotFoundExcepton(Feed.ID id) {
        super();
        this.id = id;
    }
    
    public FeedNotFoundExcepton(String message, Feed.ID id) {
        super(message);
        this.id = id;
    }

    public Feed.ID getId() {
        return id;
    }
}
