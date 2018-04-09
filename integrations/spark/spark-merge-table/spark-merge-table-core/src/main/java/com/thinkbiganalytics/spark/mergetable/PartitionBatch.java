/**
 * 
 */
package com.thinkbiganalytics.spark.mergetable;

import java.io.Serializable;

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
import java.util.Collection;
import java.util.List;

/**
 * Stores the identifier of a partition and the record count
 */
public class PartitionBatch implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long records;
    private List<String> partitionValues;
    private PartitionSpec partitionSpec;
    
    public PartitionBatch(PartitionBatch orig) {
        this(orig.getRecordCount(), orig.getPartitionSpec(), orig.getPartitionValues());
    }

    public PartitionBatch(Long records, PartitionSpec partitionSpec, Collection<String> partitionValues) {
        this.records = records;
        this.partitionValues = new ArrayList<>(partitionValues);
        this.partitionSpec = partitionSpec;
    }

    public static List<PartitionBatch> toPartitionBatchesForAlias(List<PartitionBatch> batches, String alias) {
        ArrayList<PartitionBatch> newBatches = new ArrayList<>();
        for (PartitionBatch batch : batches) {
            newBatches.add(batch.newForAlias(alias));
        }
        return newBatches;
    }

    public Long getRecordCount() {
        return records;
    }

    public List<String> getPartitionValues() {
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
        PartitionBatch batch = new PartitionBatch(this);
        batch.setPartitionSpec(partitionSpec.withAlias(alias));
        return batch;
    }

}
