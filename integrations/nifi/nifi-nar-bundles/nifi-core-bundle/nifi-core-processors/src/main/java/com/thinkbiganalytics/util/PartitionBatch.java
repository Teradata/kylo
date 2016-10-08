/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the identifier of a partition and the record count
 */
public class PartitionBatch implements Cloneable {

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

    private void setPartitionSpec(PartitionSpec spec) {
        this.partitionSpec = spec;
    }

    public String getBatchDescription() {
        return partitionSpec.toPartitionSpec(partionValues);
    }

    public PartitionBatch newForAlias(String alias) {
        PartitionBatch batch = null;
        try {
            batch = (PartitionBatch) this.clone();
            batch.setPartitionSpec(partitionSpec.newForAlias(alias));
            return batch;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Unable to create for alias ", e);
        }

    }

    public static List<PartitionBatch> toPartitionBatchesForAlias(List<PartitionBatch> batches, String alias) {
        ArrayList<PartitionBatch> newBatches = new ArrayList<>();
        batches.stream().forEach(batch -> newBatches.add(batch.newForAlias(alias)));
        return newBatches;

    }

}
