/**
 * 
 */
package com.thinkbiganalytics.metadata.api.event.feed;

import com.thinkbiganalytics.metadata.api.event.AbstractMetadataEvent;
import com.thinkbiganalytics.metadata.api.feed.Feed;

/**
 *
 * @author Sean Felten
 */
public class PreconditionTriggerEvent extends AbstractMetadataEvent<Feed.ID> {

    private static final long serialVersionUID = 1L;

    public PreconditionTriggerEvent(Feed.ID id) {
        super(id);
    }

}
