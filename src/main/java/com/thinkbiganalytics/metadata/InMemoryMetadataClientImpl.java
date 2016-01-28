/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock
 */
public class InMemoryMetadataClientImpl implements MetadataClient {

    private Map<String, BatchLoadStatus> batchLoadStatusMap = new HashMap<>();

    @Override
    public BatchLoadStatus getLastLoad(String feed) {
        return batchLoadStatusMap.get(feed);
    }

    @Override
    public void recordLastSuccessfulLoad(String feed, BatchLoadStatus loadStatus) {
        BatchLoadStatus status = getLastLoad(feed);
        batchLoadStatusMap.put(feed, loadStatus);
    }

}
