/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.metadata;

/**
 * Temporary
 */
public interface MetadataClient {

    // incremental/batch/latest
    BatchLoadStatus getLastLoad(String feed);

    void recordLastSuccessfulLoad(String feed, BatchLoadStatus loadStatus);

}


