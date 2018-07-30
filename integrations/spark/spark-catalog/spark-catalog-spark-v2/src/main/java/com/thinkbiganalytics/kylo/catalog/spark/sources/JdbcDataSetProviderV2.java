package com.thinkbiganalytics.kylo.catalog.spark.sources;

/*-
 * #%L
 * Kylo Catalog for Spark 2
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.spark.KyloCatalogClientV2;
import com.thinkbiganalytics.kylo.catalog.spark.SparkSqlUtilV2;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.spark.Accumulable;
import org.apache.spark.AccumulableParam;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructType;

import javax.annotation.Nonnull;

import scala.Function1;
import scala.Option$;
import scala.collection.Seq;
import scala.collection.Seq$;

/**
 * A data set provider that can read from and write to JDBC tables using Spark 2.
 */
public class JdbcDataSetProviderV2 extends AbstractJdbcDataSetProvider<Dataset<Row>> {

    @Nonnull
    @Override
    protected <R, P1> Accumulable<R, P1> accumulable(@Nonnull final R initialValue, @Nonnull final String name, @Nonnull final AccumulableParam<R, P1> param,
                                                     @Nonnull final KyloCatalogClient<Dataset<Row>> client) {
        return ((KyloCatalogClientV2) client).getSparkSession().sparkContext().accumulable(initialValue, name, param);
    }

    @Nonnull
    @Override
    protected Dataset<Row> filter(@Nonnull final Dataset<Row> dataSet, @Nonnull final Column condition) {
        return dataSet.filter(condition);
    }

    @Nonnull
    @Override
    protected DataFrameReader getDataFrameReader(@Nonnull final KyloCatalogClient<Dataset<Row>> client, @Nonnull final DataSetOptions options) {
        return ((KyloCatalogClientV2) client).getSparkSession().read();
    }

    @Nonnull
    @Override
    protected DataFrameWriter getDataFrameWriter(@Nonnull final Dataset<Row> dataSet, @Nonnull final DataSetOptions options) {
        return SparkSqlUtilV2.prepareDataFrameWriter(dataSet.write(), options);
    }

    @Nonnull
    @Override
    protected Dataset<Row> load(@Nonnull final DataFrameReader reader) {
        return reader.load();
    }

    @Nonnull
    @Override
    protected Dataset<Row> map(@Nonnull Dataset<Row> dataSet, @Nonnull String fieldName, @Nonnull Function1 function, @Nonnull DataType returnType) {
        final Seq<Column> inputs = Seq$.MODULE$.<Column>newBuilder().$plus$eq(dataSet.col(fieldName)).result();
        final UserDefinedFunction udf = new UserDefinedFunction(function, returnType, Option$.MODULE$.<Seq<DataType>>empty());
        return dataSet.withColumn(fieldName, udf.apply(inputs));
    }

    @Override
    protected StructType schema(@Nonnull final Dataset<Row> dataSet) {
        return dataSet.schema();
    }
}
