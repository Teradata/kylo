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
import org.apache.spark.sql.types.StructField;
import org.apache.spark.storage.StorageLevel;

import javax.annotation.Nonnull;

public class DataValidatorResult {

    @Nonnull
    private final JavaRDD<CleansedRowResult> cleansedRowResultRDD;

    @Nonnull
    private final FieldPolicy[] policies;

    @Nonnull
    private final StructField[] schema;

    public DataValidatorResult(@Nonnull final JavaRDD<CleansedRowResult> cleansedRowResultRDD, @Nonnull final FieldPolicy[] policies, @Nonnull final StructField[] schema) {
        this.cleansedRowResultRDD = cleansedRowResultRDD;
        this.policies = policies;
        this.schema = schema;
    }

    @Nonnull
    public JavaRDD<CleansedRowResult> getCleansedRowResultRDD() {
        return cleansedRowResultRDD;
    }

    @Nonnull
    public FieldPolicy[] getPolicies() {
        return policies;
    }

    @Nonnull
    public StructField[] getSchema() {
        return schema;
    }

    public void persist(@Nonnull final StorageLevel newLevel) {
        cleansedRowResultRDD.persist(newLevel);
    }

    public void unpersist() {
        cleansedRowResultRDD.unpersist();
    }
}


