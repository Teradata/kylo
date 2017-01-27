package com.thinkbiganalytics.util;

/*-
 * #%L
 * thinkbig-nifi-core-processors
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

    public static List<PartitionBatch> toPartitionBatchesForAlias(List<PartitionBatch> batches, String alias) {
        ArrayList<PartitionBatch> newBatches = new ArrayList<>();
        batches.stream().forEach(batch -> newBatches.add(batch.newForAlias(alias)));
        return newBatches;

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

}
