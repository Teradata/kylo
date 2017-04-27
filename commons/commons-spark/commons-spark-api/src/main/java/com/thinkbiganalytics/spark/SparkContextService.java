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
     *
     * @param adaptee an existing data set
     * @return a copy
     */
    DataSet toDataSet(Object adaptee);

    /**
     * Creates a data set from the specified table.
     *
     * @param context   the Spark SQL context
     * @param tableName the name of the table
     * @return the table data
     */
    DataSet toDataSet(SQLContext context, String tableName);

    /**
     * Creates a data set from a Spark RDD.
     *
     * @param context the Spark SQL context
     * @param rdd     the Spark RDD
     * @param schema  the schema for the RDD
     * @return a data set
     */
    DataSet toDataSet(SQLContext context, JavaRDD<Row> rdd, StructType schema);

    /**
     * Creates a data set from a Spark RDD.
     *
     * @param context   the Spark SQL context
     * @param rdd       the Spark RDD
     * @param beanClass the type of RDD
     * @return a data set
     */
    DataSet toDataSet(SQLContext context, JavaRDD<?> rdd, Class<?> beanClass);

    /**
     * Creates a data set from the specified Hive query.
     *
     * @param context the Hive context
     * @param sql     the Hive query
     * @return a data set
     */
    DataSet sql(HiveContext context, String sql);

    /**
     * Creates a data set from the specified Spark SQL query.
     *
     * @param context the Spark SQL context
     * @param sql     the Spark SQL query
     * @return a data set
     */
    DataSet sql(SQLContext context, String sql);
}
