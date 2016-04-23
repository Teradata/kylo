package com.thinkbiganalytics.feedmgr.rest.model;

import com.thinkbiganalytics.nifi.rest.model.NifiProcessGroup;

/**
 * Created by sr186054 on 2/5/16.
 */
public class NifiFeed {

    private FeedMetadata feedMetadata;
    private NifiProcessGroup feedProcessGroup;

    public NifiFeed(FeedMetadata feedMetadata, NifiProcessGroup feedProcessGroup) {
        this.feedMetadata = feedMetadata;
        this.feedProcessGroup = feedProcessGroup;
    }

    public FeedMetadata getFeedMetadata() {
        return feedMetadata;
    }

    public void setFeedMetadata(FeedMetadata feedMetadata) {
        this.feedMetadata = feedMetadata;
    }

    public NifiProcessGroup getFeedProcessGroup() {
        return feedProcessGroup;
    }

    public void setFeedProcessGroup(NifiProcessGroup feedProcessGroup) {
        this.feedProcessGroup = feedProcessGroup;
    }
}
