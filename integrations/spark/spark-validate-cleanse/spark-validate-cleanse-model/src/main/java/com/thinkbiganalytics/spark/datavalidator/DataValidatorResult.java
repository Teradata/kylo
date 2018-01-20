package com.thinkbiganalytics.spark.datavalidator;

/*-
 * #%L
 * kylo-spark-validate-cleanse-model
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

import com.thinkbiganalytics.policy.FieldPolicy;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.storage.StorageLevel;

import javax.annotation.Nonnull;

/**
 * Contains the result of validating a dataset.
 */
public class DataValidatorResult {

    @Nonnull
    private final JavaRDD<CleansedRowResult> cleansedRowResultRDD;

    @Nonnull
    private final FieldPolicy[] policies;

    @Nonnull
    private final StructType schema;

    public DataValidatorResult(@Nonnull final JavaRDD<CleansedRowResult> cleansedRowResultRDD, @Nonnull final FieldPolicy[] policies, @Nonnull final StructType schema) {
        this.cleansedRowResultRDD = cleansedRowResultRDD;
        this.policies = policies;
        this.schema = schema;
    }

    /**
     * Gets the standardized and validated rows.
     */
    @Nonnull
    public JavaRDD<CleansedRowResult> getCleansedRowResultRDD() {
        return cleansedRowResultRDD;
    }

    /**
     * Gets the field policies applied to the input.
     */
    @Nonnull
    public FieldPolicy[] getPolicies() {
        return policies;
    }

    /**
     * Gets the schema of the cleansed rows.
     */
    @Nonnull
    public StructType getSchema() {
        return schema;
    }

    /**
     * Persists the cleansed rows with the specified storage level.
     */
    public void persist(@Nonnull final StorageLevel newLevel) {
        cleansedRowResultRDD.persist(newLevel);
    }

    /**
     * Removes all blocks of the cleansed rows from memory and disk.
     */
    public void unpersist() {
        cleansedRowResultRDD.unpersist();
    }
}


