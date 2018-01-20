package com.thinkbiganalytics.spark;

/*-
 * #%L
 * thinkbig-commons-spark-1.6
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
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.storage.StorageLevel;

import java.util.List;

/**
 * Manages a collection of objects for Spark 1.6.
 */
public class DataSet16 implements DataSet {

    private final DataFrame dataframe;

    public DataSet16(Object adaptee) {
        this((DataFrame) adaptee);
    }

    public DataSet16(DataFrame dataframe) {
        this.dataframe = dataframe;
    }

    @Override
    public RDD<Row> rdd() {
        return dataframe.rdd();
    }

    @Override
    public JavaRDD<Row> javaRDD() {
        return dataframe.javaRDD();
    }

    @Override
    public DataSet filter(String condition) {
        return new DataSet16(dataframe.filter(condition));
    }

    @Override
    public DataSet drop(String condition) {
        return new DataSet16(dataframe.drop(condition));
    }

    @Override
    public DataSet toDF() {
        return new DataSet16(dataframe.toDF());
    }

    @Override
    public long count() {
        return dataframe.count();
    }

    @Override
    public void registerTempTable(String tableName) {
        dataframe.registerTempTable(tableName);
    }

    @Override
    public StructType schema() {
        return dataframe.schema();
    }

    @Override
    public void writeToTable(String partitionColumn, String fqnTable) {
        dataframe.write().partitionBy(partitionColumn).mode(SaveMode.Append).saveAsTable(fqnTable);
    }

    @Override
    public List<Row> collectAsList() {
        return dataframe.collectAsList();
    }

    @Override
    public DataSet repartition(int numPartitions) {
        return new DataSet16(dataframe.repartition(numPartitions));
    }

    @Override
    public DataSet persist(final StorageLevel newLevel) {
        return new DataSet16(dataframe.persist(newLevel));
    }

    @Override
    public DataSet unpersist(final boolean blocking) {
        return new DataSet16(dataframe.unpersist(blocking));
    }

    @Override
    public DataFrameWriter write() {
        return dataframe.write();
    }
}
