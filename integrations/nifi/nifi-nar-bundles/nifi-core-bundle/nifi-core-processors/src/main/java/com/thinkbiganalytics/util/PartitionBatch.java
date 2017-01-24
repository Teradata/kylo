package com.thinkbiganalytics.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the identifier of a partition and the record count
 */
public class PartitionBatch implements Cloneable {

    private Long records;

    private String[] partitionValues;

    private PartitionSpec partitionSpec;

    public PartitionBatch(Long records, PartitionSpec partitionSpec, String[] partitionValues) {
        this.records = records;
        this.partitionValues = partitionValues;
        this.partitionSpec = partitionSpec;
    }

    public Long getRecordCount() {
        return records;
    }

    public String[] getPartitionValues() {
        return partitionValues;
    }

    public PartitionSpec getPartitionSpec() {
        return partitionSpec;
    }

    private void setPartitionSpec(PartitionSpec spec) {
        this.partitionSpec = spec;
    }

    public String getBatchDescription() {
        return partitionSpec.toPartitionSpec(partitionValues);
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
