package com.thinkbiganalytics.spark;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.StructType;
import org.springframework.stereotype.Service;

/**
 * Created by ru186002 on 18/10/2016.
 */
@Service
public class SparkContextService20 implements SparkContextService {
    @Override
    public DataSet toDataSet(Object adaptee) {
        return new DataSet20(adaptee);
    }

    @Override
    public DataSet toDataSet(SQLContext context, String tableName) {
        return toDataSet(context.table(tableName));
    }

    @Override
    public DataSet toDataSet(SQLContext context, JavaRDD<Row> rdd, StructType schema) {
        return toDataSet(context.createDataFrame(rdd, schema));
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
