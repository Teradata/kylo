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
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import org.apache.spark.Accumulable;
import org.apache.spark.AccumulableParam;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructType;

import javax.annotation.Nonnull;

import scala.Function1;

/**
 * A data set provider that can read from and write to JDBC tables using Spark 2.
 */
public class JdbcDataSetProviderV2 extends AbstractJdbcDataSetProvider<Dataset<Row>> {

    @Nonnull
    @Override
    protected <R, P1> Accumulable<R, P1> accumulable(@Nonnull final R initialValue, @Nonnull final String name, @Nonnull final AccumulableParam<R, P1> param,
                                                     @Nonnull final KyloCatalogClient<Dataset<Row>> client) {
        return DataSetProviderUtilV2.accumulable(initialValue, name, param, client);
    }

    @Nonnull
    @Override
    protected Dataset<Row> filter(@Nonnull final Dataset<Row> dataSet, @Nonnull final Column condition) {
        return dataSet.filter(condition);
    }

    @Nonnull
    @Override
    protected DataFrameReader getDataFrameReader(@Nonnull final KyloCatalogClient<Dataset<Row>> client, @Nonnull final DataSetOptions options) {
        return DataSetProviderUtilV2.getDataFrameReader(client);
    }

    @Nonnull
    @Override
    protected DataFrameWriter getDataFrameWriter(@Nonnull final Dataset<Row> dataSet, @Nonnull final DataSetOptions options) {
        return DataSetProviderUtilV2.getDataFrameWriter(dataSet, options);
    }

    @Nonnull
    @Override
    protected Dataset<Row> load(@Nonnull final DataFrameReader reader) {
        return DataSetProviderUtilV2.load(reader, null);
    }

    @Nonnull
    @Override
    protected Dataset<Row> map(@Nonnull final Dataset<Row> dataSet, @Nonnull final String fieldName, @Nonnull final Function1 function, @Nonnull final DataType returnType) {
        return DataSetProviderUtilV2.map(dataSet, fieldName, function, returnType);
    }

    @Nonnull
    @Override
    protected StructType schema(@Nonnull final Dataset<Row> dataSet) {
        return DataSetProviderUtilV2.schema(dataSet);
    }
}
