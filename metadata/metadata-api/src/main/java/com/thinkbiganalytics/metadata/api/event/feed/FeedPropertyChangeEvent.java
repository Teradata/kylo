package com.thinkbiganalytics.metadata.api.event.feed;

import com.thinkbiganalytics.metadata.api.event.AbstractMetadataEvent;

public class FeedPropertyChangeEvent extends AbstractMetadataEvent<PropertyChange> {

    private static final long serialVersionUID = 1L;

    public FeedPropertyChangeEvent(PropertyChange change) {
        super(change);
    }
}
