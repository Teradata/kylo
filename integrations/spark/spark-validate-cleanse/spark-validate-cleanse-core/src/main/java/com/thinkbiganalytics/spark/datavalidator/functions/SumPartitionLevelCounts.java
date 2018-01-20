package com.thinkbiganalytics.spark.datavalidator.functions;

/*-
 * #%L
 * kylo-spark-validate-cleanse-app
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


import org.apache.spark.api.java.function.Function2;

/**
 * Sum up the individual partition level counts
 */
public class SumPartitionLevelCounts implements Function2<long[], long[], long[]> {

    @Override
    public long[] call(long[] countsA, long[] countsB) throws Exception {
        long[] countsResult = new long[countsA.length];

        for (int idx = 0; idx < countsA.length; idx++) {
            countsResult[idx] = countsA[idx] + countsB[idx];
        }
        return countsResult;
    }
}
