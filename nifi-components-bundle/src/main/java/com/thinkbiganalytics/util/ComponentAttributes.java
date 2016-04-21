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
    PREVIOUS_HIGHWATER_DATE("previous.highwater.date"),
    NEW_HIGHWATER_DATE("new.highwater.date"),
    NUM_MERGED_PARTITIONS("num.merged.partitions"),
    MERGED_PARTITION("merged.partition"),
    MERGED_PARTITION_ROWCOUNT("merged.partition.rowcount"),
    HDFS_FILE("hdfs.file");

    private final String key;

    private ComponentAttributes(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }

}
