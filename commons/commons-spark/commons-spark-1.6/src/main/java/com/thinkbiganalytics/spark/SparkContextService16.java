package com.thinkbiganalytics.spark;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.StructType;
import org.springframework.stereotype.Service;

/**
 * Creates structured data sets for Spark 1.6.
 */
@Service
public class SparkContextService16 implements SparkContextService {

    @Override
    public DataSet toDataSet(Object adaptee) {
        return new DataSet16(adaptee);
    }

    @Override
    public DataSet toDataSet(SQLContext context, String tableName) {
        return toDataSet(context.table(tableName));
    }

    @Override
    public DataSet toDataSet(SQLContext context, JavaRDD<Row> rdd, StructType schema) {
        return toDataSet(context.createDataFrame(rdd, schema).toDF());
    }

    @Override
    public DataSet sql(HiveContext context, String sql) {
        return toDataSet(context.sql(sql));
    }

    @Override
    public DataSet sql(SQLContext context, String sql) {
        return toDataSet(context.sql(sql));
    }
}
