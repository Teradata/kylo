package com.thinkbiganalytics.spark.datavalidator;

/*-
 * #%L
 * kylo-spark-validate-cleanse-api
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
import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.dataprofiler.output.OutputRow;

import org.apache.spark.sql.hive.HiveContext;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public interface DataValidator {

    /**
     * Gets the validation profile statistics for the specified result.
     */
    List<OutputRow> getProfileStats(@Nonnull DataValidatorResult result);

    /**
     * Validates the specified dataset and returns the results.
     */
    @Nonnull
    DataValidatorResult validate(@Nonnull DataSet dataset, @Nonnull Map<String, FieldPolicy> policies);

    /**
     * Validates the specified Hive table and returns the results.
     *
     * @param databaseName    source database name
     * @param sourceTableName source table name
     * @param targetTableName target table name
     * @param partition       source processing_dttm partition value
     * @param numPartitions   target number of Spark partitions, or 0 to disable repartitioning
     * @param policyMap       map of field names to policies
     * @param hiveContext     Hive context
     */
    @Nonnull
    DataValidatorResult validateTable(@Nonnull String databaseName, @Nonnull String sourceTableName, @Nonnull String targetTableName, @Nonnull String partition, int numPartitions,
                                      @Nonnull Map<String, FieldPolicy> policyMap, @Nonnull HiveContext hiveContext);

    /**
     * Saves the invalid rows to the specified Hive table.
     */
    void saveInvalidToTable(@Nonnull String databaseName, @Nonnull String tableName, @Nonnull DataValidatorResult result, @Nonnull HiveContext hiveContext);

    /**
     * Saves profile data to the specified Hive table.
     *
     * @param databaseName target database name
     * @param tableName    target table name
     * @param partition    target processing_dttm partition value
     * @param result       validation results
     * @param hiveContext  Hive context
     */
    void saveProfileToTable(@Nonnull String databaseName, @Nonnull String tableName, @Nonnull String partition, @Nonnull DataValidatorResult result, @Nonnull HiveContext hiveContext);

    /**
     * Saves the valid rows to the specified Hive table.
     */
    void saveValidToTable(@Nonnull String databaseName, @Nonnull String sourceTableName, @Nonnull String targetTableName, @Nonnull DataValidatorResult result, @Nonnull HiveContext hiveContext);
}
