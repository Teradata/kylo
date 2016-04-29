/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.nifi.v2.core.metadata;

import com.thinkbiganalytics.nifi.core.api.metadata.BatchLoadStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock
 */
@Deprecated
public class InMemoryLegacyMetadataProvider {

    private Map<String, BatchLoadStatus> batchLoadStatusMap = new HashMap<>();

    private Map<String, Boolean> feedRegistrationMap = new HashMap<>();

    private String uniqueName(String category, String feed) {
        String uniqueName = category + "." + feed;
        return uniqueName;
    }

    public BatchLoadStatus getLastLoad(String category, String feed) {
        String uniqueName = uniqueName(category, feed);
        return batchLoadStatusMap.get(uniqueName);
    }

    public void recordLastSuccessfulLoad(String category, String feed, BatchLoadStatus loadStatus) {

        String uniqueName = uniqueName(category, feed);
        batchLoadStatusMap.put(uniqueName, loadStatus);
    }

    public boolean isRegistrationRequired(String category, String feed) {
        String uniqueName = uniqueName(category, feed);
        Boolean b = feedRegistrationMap.get(uniqueName);
        return (b == null || b == false ? true : false);
    }

    public void recordRegistration(String category, String feed, boolean result) {
        String uniqueName = uniqueName(category, feed);
        feedRegistrationMap.put(uniqueName, result);
    }


}
