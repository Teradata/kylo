package com.thinkbiganalytics.kylo.catalog.spark.sources;

/*-
 * #%L
 * Kylo Catalog for Spark 1
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
import com.thinkbiganalytics.kylo.catalog.spark.KyloCatalogClientV1;

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
import javax.annotation.Nullable;

import scala.Function1;
import scala.collection.Seq;
import scala.collection.Seq$;

/**
 * Static utility methods for Spark 1.6 data set providers.
 */
public class DataSetProviderUtilV1 {

    /**
     * Creates an {@link Accumulable} shared variable with a name for display in the Spark UI.
     */
    @Nonnull
    static <R, P1> Accumulable<R, P1> accumulable(@Nonnull final R initialValue, @Nonnull final String name, @Nonnull final AccumulableParam<R, P1> param,
                                                  @Nonnull final KyloCatalogClient<DataFrame> client) {
        return ((KyloCatalogClientV1) client).getSQLContext().sparkContext().accumulable(initialValue, name, param);
    }

    /**
     * Gets a reader from the specified client.
     */
    @Nonnull
    static DataFrameReader getDataFrameReader(@Nonnull final KyloCatalogClient<DataFrame> client) {
        return ((KyloCatalogClientV1) client).getSQLContext().read();
    }

    /**
     * Gets a writer for the specified data set.
     */
    @Nonnull
    static DataFrameWriter getDataFrameWriter(@Nonnull final DataFrame dataSet) {
        return dataSet.write();
    }

    /**
     * Loads a data set using the specified reader and paths.
     */
    @Nonnull
    static DataFrame load(@Nonnull final DataFrameReader reader, @Nullable final Seq<String> paths) {
        if (paths == null || paths.isEmpty()) {
            return reader.load();
        } else if (paths.size() == 1) {
            return reader.load(paths.apply(0));
        } else {
            return reader.load(paths);
        }
    }

    /**
     * Applies the specified function to the specified field of the data set.
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    static DataFrame map(@Nonnull final DataFrame dataSet, @Nonnull final String fieldName, @Nonnull final Function1 function, @Nonnull final DataType returnType) {
        final Seq<Column> inputs = Seq$.MODULE$.<Column>newBuilder().$plus$eq(dataSet.col(fieldName)).result();
        final UserDefinedFunction udf = new UserDefinedFunction(function, returnType, (Seq<DataType>) Seq$.MODULE$.<DataType>empty());
        return dataSet.withColumn(fieldName, udf.apply(inputs));
    }

    /**
     * Returns the schema of the specified data set.
     */
    @Nonnull
    static StructType schema(@Nonnull final DataFrame dataSet) {
        return dataSet.schema();
    }

    /**
     * Instances of {@code DataSetProviderUtilV1} should not be constructed.
     */
    private DataSetProviderUtilV1() {
        throw new UnsupportedOperationException();
    }
}
