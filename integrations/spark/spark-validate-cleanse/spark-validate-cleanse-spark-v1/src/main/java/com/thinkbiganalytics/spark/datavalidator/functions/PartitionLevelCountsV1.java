package com.thinkbiganalytics.spark.datavalidator.functions;

/*-
 * #%L
 * kylo-spark-validate-cleanse-spark-v1
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

import com.thinkbiganalytics.spark.datavalidator.CleansedRowResult;

import org.apache.spark.api.java.function.FlatMapFunction;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Get partition-level counts of invalid columns, and total valid and invalid rows (Spark 1)
 */
public class PartitionLevelCountsV1 implements FlatMapFunction<Iterator<CleansedRowResult>, long[]> {

    private int schemaLen = 0;

    public PartitionLevelCountsV1(int schemaLength) {
        this.schemaLen = schemaLength;
    }

    @Override
    public Iterable<long[]> call(Iterator<CleansedRowResult> cleansedRowResultIterator) throws Exception {
        long[] validationCounts = new long[schemaLen + 2];

        while (cleansedRowResultIterator.hasNext()) {

            CleansedRowResult cleansedRowResult = cleansedRowResultIterator.next();

            for (int idx = 0; idx < schemaLen; idx++) {
                if (!cleansedRowResult.columnsValid[idx]) {
                    validationCounts[idx] = validationCounts[idx] + 1L;
                }
            }
            if (cleansedRowResult.rowIsValid) {
                validationCounts[schemaLen] = validationCounts[schemaLen] + 1L;
            } else {
                validationCounts[schemaLen + 1] = validationCounts[schemaLen + 1] + 1L;
            }
        }

        List<long[]> results = new LinkedList<>();
        results.add(validationCounts);
        return results;
    }
}
