package com.thinkbiganalytics.spark.datavalidator;

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

import org.apache.spark.api.java.JavaRDD;

import java.io.Serializable;

/**
 * Validator Strategy interface (to enable support for Spark 1 and 2)
 */
public interface IValidatorStrategy extends Serializable {

    /**
     * Get count of invalid columns, and total valid and invalid rows on cleansed rows
     *
     * @param cleansedRowResultJavaRDD RDD containing result of cleaning rows of type {@link CleansedRowResult}
     * @param schemaLength             number of columns in schema
     * @return RDD containing counts of invalid columns, and total valid and invalid rows
     */
    JavaRDD<long[]> getCleansedRowResultPartitionCounts(JavaRDD<CleansedRowResult> cleansedRowResultJavaRDD, int schemaLength);
}
