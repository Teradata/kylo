package com.thinkbiganalytics.feedmgr.rest.model;

import com.thinkbiganalytics.feedmgr.rest.model.schema.EditFeedAction;

/**
 * Created by sr186054 on 3/6/17.
 */
public class EditFeedEntity {


    private EditFeedAction action;
    private FeedMetadata feedMetadata;

    public EditFeedAction getAction() {
        return action;
    }

    public void setAction(EditFeedAction action) {
        this.action = action;
    }

    public FeedMetadata getFeedMetadata() {
        return feedMetadata;
    }

    public void setFeedMetadata(FeedMetadata feedMetadata) {
        this.feedMetadata = feedMetadata;
    }
}
