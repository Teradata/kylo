/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.metadata;

/**
 * Temporary
 */
public interface MetadataClient {

    // incremental/batch/latest
    BatchLoadStatus getLastLoad(String category, String feed);

    void recordLastSuccessfulLoad(String category, String feed, BatchLoadStatus loadStatus);

    boolean isRegistrationRequired(String category, String feed);

    void recordRegistration(String category, String feed, boolean result);

}


