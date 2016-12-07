package com.thinkbiganalytics.metadata.api.feed;


import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobInstance;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by sr186054 on 9/14/16.
 */
public interface OpsManagerFeed {

    enum FeedType {
        FEED, CHECK
    }

    interface ID extends Serializable {

    }

    /**
     * @return the unique ID of this Feed
     */
    ID getId();

    /**
     * @return the name of this Feed
     */
    String getName();


    /**
     * CHECK or FEED
     * @return
     */
    FeedType getFeedType();


}
