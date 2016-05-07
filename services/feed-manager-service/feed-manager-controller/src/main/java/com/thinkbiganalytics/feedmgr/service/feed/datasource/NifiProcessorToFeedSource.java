package com.thinkbiganalytics.feedmgr.service.feed.datasource;

import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;

/**
 * Created by sr186054 on 5/5/16.
 */
public interface NifiProcessorToFeedSource {
    Datasource transform();

    String getNifiProcessorType();
}
