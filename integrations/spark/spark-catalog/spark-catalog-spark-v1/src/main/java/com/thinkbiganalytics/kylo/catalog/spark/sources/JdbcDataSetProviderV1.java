package com.thinkbiganalytics.kylo.catalog.spark.sources;

/*-
 * #%L
 * Kylo Catalog for Spark 1
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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
import com.thinkbiganalytics.kylo.catalog.spark.KyloCatalogClientV1;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.spark.Accumulable;
import org.apache.spark.AccumulableParam;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.UserDefinedFunction;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructType;

import javax.annotation.Nonnull;

import scala.Function1;
import scala.collection.Seq;
import scala.collection.Seq$;

/**
 * A data set provider that can read from and write to JDBC tables using Spark 1.
 */
public class JdbcDataSetProviderV1 extends AbstractJdbcDataSetProvider<DataFrame> {

    @Nonnull
    @Override
    protected <R, P1> Accumulable<R, P1> accumulable(@Nonnull final R initialValue, @Nonnull final String name, @Nonnull final AccumulableParam<R, P1> param,
                                                     @Nonnull final KyloCatalogClient<DataFrame> client) {
        return ((KyloCatalogClientV1) client).getSQLContext().sparkContext().accumulable(initialValue, name, param);
    }

    @Nonnull
    @Override
    protected DataFrame filter(@Nonnull final DataFrame dataSet, @Nonnull final Column condition) {
        return dataSet.filter(condition);
    }

    @Nonnull
    @Override
    protected DataFrameReader getDataFrameReader(@Nonnull final KyloCatalogClient client, @Nonnull final DataSetOptions options) {
        return ((KyloCatalogClientV1) client).getSQLContext().read();
    }

    @Nonnull
    @Override
    protected DataFrameWriter getDataFrameWriter(@Nonnull final DataFrame dataSet, @Nonnull final DataSetOptions options) {
        return dataSet.write();
    }

    @Nonnull
    @Override
    protected DataFrame load(@Nonnull final DataFrameReader reader) {
        return reader.load();
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    protected DataFrame map(@Nonnull final DataFrame dataSet, @Nonnull final String fieldName, @Nonnull final Function1 function, @Nonnull final DataType returnType) {
        final Seq<Column> inputs = Seq$.MODULE$.<Column>newBuilder().$plus$eq(dataSet.col(fieldName)).result();
        final UserDefinedFunction udf = new UserDefinedFunction(function, returnType, (Seq<DataType>) Seq$.MODULE$.<DataType>empty());
        return dataSet.withColumn(fieldName, udf.apply(inputs));
    }

    @Override
    protected StructType schema(@Nonnull final DataFrame dataSet) {
        return dataSet.schema();
    }
}
