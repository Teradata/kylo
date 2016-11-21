package com.thinkbiganalytics.spark;


import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by ru186002 on 17/10/2016.
 */
public class DataSet20 implements DataSet {

    private final Dataset<Row> dataset;

    public DataSet20(Object adaptee) {
        this((Dataset) adaptee);
    }

    public DataSet20(Dataset<Row> dataset) {
        this.dataset = dataset;
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
    public void show(int count) {
        dataset.show(count);
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
}
