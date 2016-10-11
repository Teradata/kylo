package com.thinkbiganalytics.feedmgr.rest.model.schema;

import com.thinkbiganalytics.feedmgr.metadata.MetadataField;

/**
 * Created by sr186054 on 10/10/16.
 */
public class FeedProcessingOptions {


    public FeedProcessingOptions() {

    }

    @MetadataField(description = "When processing data should the system skip the header row? (true/false)")
    private boolean skipHeader;

    public boolean isSkipHeader() {
        return skipHeader;
    }

    public void setSkipHeader(boolean skipHeader) {
        this.skipHeader = skipHeader;
    }
}
