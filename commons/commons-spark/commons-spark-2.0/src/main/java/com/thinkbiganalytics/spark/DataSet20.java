package com.thinkbiganalytics.spark;

/*-
 * #%L
 * thinkbig-commons-spark-2.0
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
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.storage.StorageLevel;

import java.util.List;

/**
 * Manages a collection of objects for Spark 2.0.
 */
public class DataSet20 implements DataSet {

    private final Dataset<Row> dataset;

    DataSet20(Object adaptee) {
        this((Dataset) adaptee);
    }

    private DataSet20(Dataset<Row> dataset) {
        this.dataset = dataset;
    }

    @Override
    public RDD<Row> rdd() {
        return dataset.rdd();
    }

    @Override
    public JavaRDD<Row> javaRDD() {
        return dataset.javaRDD();
    }

    @Override
    public DataSet filter(String condition) {
        return new DataSet20(dataset.filter(condition));
    }

    @Override
    public DataSet drop(String condition) {
        return new DataSet20(dataset.drop(condition));
    }

    @Override
    public DataSet toDF() {
        return new DataSet20(dataset.toDF());
    }

    @Override
    public long count() {
        return dataset.count();
    }

    @Override
    public void registerTempTable(String tableName) {
        dataset.registerTempTable(tableName);
    }

    @Override
    public StructType schema() {
        return dataset.schema();
    }

    @Override
    public List<Row> collectAsList() {
        return dataset.collectAsList();
    }

    @Override
    public void writeToTable(String partitionColumn, String fqnTable) {
        dataset.write().mode(SaveMode.Append).insertInto(fqnTable);
    }

    @Override
    public DataSet repartition(int numPartitions) {
        return new DataSet20(dataset.repartition(numPartitions));
    }

    @Override
    public DataSet persist(final StorageLevel newLevel) {
        return new DataSet20(dataset.persist(newLevel));
    }

    @Override
    public DataSet unpersist(final boolean blocking) {
        return new DataSet20(dataset.unpersist(blocking));
    }

    @Override
    public DataFrameWriter write() {
        return dataset.write();
    }
}
