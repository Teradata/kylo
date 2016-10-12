/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

import org.apache.nifi.flowfile.attributes.FlowFileAttributeKey;

/**
 * Attributes used to record operational metadata in flow file
 */
public enum ComponentAttributes implements FlowFileAttributeKey {
    NUM_SOURCE_RECORDS("source.record.count"),
    HIGH_WATER_DATE("high.water.date"),
    NUM_MERGED_PARTITIONS("num.merged.partitions"),
    MERGED_PARTITION("merged.partition"),
    MERGED_PARTITION_ROWCOUNT("merged.partition.rowcount"),
    HDFS_FILE("hdfs.file"),
    FEED_DEPENDENT_RESULT_DELTAS("feed.dependent.results.deltas");

    private final String key;

    ComponentAttributes(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }

}
