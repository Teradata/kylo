package com.thinkbiganalytics.nifi.provenance.cache;

import com.thinkbiganalytics.nifi.provenance.model.FeedFlowFile;

/**
 * Created by sr186054 on 12/28/16.
 */
public interface FeedFlowFileCacheListener {


    void onInvalidate(FeedFlowFile flowFile);

}
