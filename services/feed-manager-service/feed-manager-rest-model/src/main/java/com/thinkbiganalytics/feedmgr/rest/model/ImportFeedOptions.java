package com.thinkbiganalytics.feedmgr.rest.model;

/**
 * Created by sr186054 on 3/11/17.
 */
public class ImportFeedOptions extends ImportOptions {

    private boolean overwriteFeedTemplate;

    public boolean isOverwriteFeedTemplate() {
        return overwriteFeedTemplate;
    }

    public void setOverwriteFeedTemplate(boolean overwriteFeedTemplate) {
        this.overwriteFeedTemplate = overwriteFeedTemplate;
    }
}
