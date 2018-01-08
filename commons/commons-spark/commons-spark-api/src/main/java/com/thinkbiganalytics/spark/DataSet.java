package com.thinkbiganalytics.spark;

/*-
 * #%L
 * thinkbig-commons-spark-api
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
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.storage.StorageLevel;

import java.util.List;

/**
 * A collection of objects that can be transformed using Spark functions.
 */
public interface DataSet {

    /**
     * Returns the content of this data set as a Spark RDD.
     *
     * @return a Spark RDD
     */
    RDD<Row> rdd();

    /**
     * Returns the content of this data set as a Spark RDD.
     *
     * @return a Spark RDD
     */
    JavaRDD<Row> javaRDD();

    /**
     * Filters rows using the specified SQL expression.
     *
     * @param condition a SQL expression
     * @return the filtered data set
     */
    DataSet filter(String condition);

    /**
     * Drops the specified column from this data set.
     *
     * @param condition the column to be dropped
     * @return the data set without the column
     */
    DataSet drop(String condition);

    /**
     * Converts this strongly-typed data set to a generic data set.
     *
     * @return the generic data set
     */
    DataSet toDF();

    /**
     * Returns the number of rows in this data set.
     *
     * @return the row count
     */
    long count();

    /**
     * Registers this data set as a temporary table with the specified name.
     *
     * @param tableName the name for the temporary table
     */
    void registerTempTable(String tableName);

    /**
     * Returns the schema of this data set.
     *
     * @return the schema
     */
    StructType schema();

    /**
     * Returns a list that contains all rows in this data set.
     *
     * @return the rows
     */
    List<Row> collectAsList();

    /**
     * Saves the content of this data set as the specified table.
     *
     * @param partitionColumn the name of the partition column
     * @param fqnTable        the name for the table
     */
    void writeToTable(String partitionColumn, String fqnTable);

    /**
     * Returns a new DataSet that has exactly numPartitions partitions.
     */
    DataSet repartition(int numPartitions);

    /**
     * Persist this Dataset with the given storage level.
     */
    DataSet persist(StorageLevel newLevel);

    /**
     * Mark the Dataset as non-persistent, and remove all blocks for it from memory and disk.
     *
     * @param blocking whether to block until all blocks are deleted
     */
    DataSet unpersist(boolean blocking);

    /**
     * Interface for saving the content of the non-streaming DataSet out into external storage.
     */
    DataFrameWriter write();
}
