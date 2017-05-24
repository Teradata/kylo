package com.thinkbiganalytics.spark.datavalidator;

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

import com.thinkbiganalytics.spark.datavalidator.functions.PartitionLevelCountsV1;

import org.apache.spark.api.java.JavaRDD;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Validator Strategy for Spark 1
 */
@Component
public class ValidatorStrategyV1 implements IValidatorStrategy, Serializable {

    @Override
    public JavaRDD<long[]> getCleansedRowResultPartitionCounts(JavaRDD<CleansedRowResult> cleansedRowResultJavaRDD, int schemaLength) {
        return cleansedRowResultJavaRDD.mapPartitions(new PartitionLevelCountsV1(schemaLength));
    }
}
