package com.thinkbiganalytics.nifi.provenance.v2.cache.flowfile;

import com.thinkbiganalytics.nifi.provenance.model.FlowFile;

import java.util.List;

/**
 * Created by sr186054 on 8/19/16.
 */
public interface FlowFileCache {

    FlowFile getEntry(String id);

    List<FlowFile> getRootFlowFiles();

    void printSummary();

    void invalidate(FlowFile flowFile);

}
