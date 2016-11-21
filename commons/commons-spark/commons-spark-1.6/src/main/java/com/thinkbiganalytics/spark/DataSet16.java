package com.thinkbiganalytics.spark;


import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;

import java.util.List;

/**
 * Created by ru186002 on 17/10/2016.
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
    public void show(int count) {
        dataframe.show(count);
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
    public List<Row> collectAsList() {
        return dataframe.collectAsList();
    }
}
