package com.thinkbiganalytics.spark;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.StructType;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Creates structured data sets using a {@link SQLContext}.
 */
@Service
public interface SparkContextService extends Serializable {

    /**
     * Creates a copy of the specified data set.
     * @param adaptee an existing data set
     * @return a copy
     */
    DataSet toDataSet(Object adaptee);

    /**
     * Creates a data set from the specified table.
     * @param context the Spark SQL context
     * @param tableName the name of the table
     * @return the table data
     */
    DataSet toDataSet(SQLContext context, String tableName);

    /**
     * Creates a data set from a Spark RDD.
     * @param context the Spark SQL context
     * @param rdd the Spark RDD
     * @param schema the schema for the RDD
     * @return a data set
     */
    DataSet toDataSet(SQLContext context, JavaRDD<Row> rdd, StructType schema);

    /**
     * Creates a data set from the specified Hive query.
     * @param context the Hive context
     * @param sql the Hive query
     * @return a data set
     */
    DataSet sql(HiveContext context, String sql);

    /**
     * Creates a data set from the specified Spark SQL query.
     * @param context the Spark SQL context
     * @param sql the Spark SQL query
     * @return a data set
     */
    DataSet sql(SQLContext context, String sql);
}
