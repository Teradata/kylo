/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

/**
 * Stores the identifier of a partition and the record count
 */
public class PartitionBatch {

    private Long records;

    private String[] partionValues;

    private PartitionSpec partitionSpec;

    public PartitionBatch(Long records, PartitionSpec partitionSpec, String[] partionValues) {
        this.records = records;
        this.partionValues = partionValues;
        this.partitionSpec = partitionSpec;
    }

    public Long getRecordCount() {
        return records;
    }

    public String[] getPartionValues() {
        return partionValues;
    }

    public PartitionSpec getPartitionSpec() {
        return partitionSpec;
    }

    public String getBatchDescription() {
        return partitionSpec.toPartitionSpec(partionValues);
    }
}
