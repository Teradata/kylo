package com.thinkbiganalytics.metadata.api.feed;


import java.io.Serializable;

/**
 * Created by sr186054 on 9/14/16.
 */
public interface OpsManagerFeed {

    interface ID extends Serializable {

    }

    ;

    /**
     * @return the unique ID of this Feed
     */
    ID getId();

    /**
     * @return the name of this Feed
     */
    String getName();

    enum FeedType {
        FEED, CHECK
    }


}
