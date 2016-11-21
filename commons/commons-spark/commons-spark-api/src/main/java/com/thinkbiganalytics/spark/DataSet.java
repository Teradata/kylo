package com.thinkbiganalytics.spark;


import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;

import java.util.List;

/**
 * Created by ru186002 on 17/10/2016.
 */
public interface DataSet {

    JavaRDD<Row> javaRDD();

    DataSet filter(String condition);

    DataSet drop(String condition);

    DataSet toDF();

    void show(int count);

    long count();

    void registerTempTable(String tableName);

    StructType schema();

    List<Row> collectAsList();
}
