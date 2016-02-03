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

    private Map<String, Boolean> feedRegistrationMap = new HashMap<>();

    private String uniqueName(String category, String feed) {
        String uniqueName = category + "." + feed;
        return uniqueName;
    }

    @Override
    public BatchLoadStatus getLastLoad(String category, String feed) {
        String uniqueName = uniqueName(category, feed);
        return batchLoadStatusMap.get(uniqueName);
    }

    @Override
    public void recordLastSuccessfulLoad(String category, String feed, BatchLoadStatus loadStatus) {

        BatchLoadStatus status = getLastLoad(category,feed);
        batchLoadStatusMap.put(feed, loadStatus);
    }

    @Override
    public boolean isRegistrationRequired(String category, String feed) {
        String uniqueName = uniqueName(category, feed);
        Boolean b = feedRegistrationMap.get(uniqueName);
        return (b == null || b == false ? true : false);
    }

    @Override
    public void recordRegistration(String category, String feed, boolean result) {
        String uniqueName = uniqueName(category, feed);
        feedRegistrationMap.put(uniqueName, result);
    }


}
