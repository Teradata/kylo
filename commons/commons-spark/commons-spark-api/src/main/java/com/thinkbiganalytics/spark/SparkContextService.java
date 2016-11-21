package com.thinkbiganalytics.spark;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.StructType;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Created by ru186002 on 18/10/2016.
 */
@Service
public interface SparkContextService extends Serializable {

    DataSet toDataSet(Object adaptee);

    DataSet toDataSet(HiveContext context, String tableName);

    DataSet toDataSet(HiveContext context, JavaRDD<Row> rdd, StructType schema);

    DataSet sql(HiveContext context, String sql);

    DataSet sql(SQLContext context, String sql);
}
