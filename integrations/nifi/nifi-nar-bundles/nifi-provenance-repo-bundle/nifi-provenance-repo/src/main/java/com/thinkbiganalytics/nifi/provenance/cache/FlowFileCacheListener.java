package com.thinkbiganalytics.nifi.provenance.cache;

import com.thinkbiganalytics.nifi.provenance.model.ActiveFlowFile;

/**
 * Created by sr186054 on 12/28/16.
 */
public interface FlowFileCacheListener {


    void onInvalidate(ActiveFlowFile flowFile);

}
