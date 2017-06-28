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
    public DataSet toDataSet(SQLContext context, JavaRDD<?> rdd, Class<?> beanClass) {
        return toDataSet(context.createDataFrame(rdd, beanClass));
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
