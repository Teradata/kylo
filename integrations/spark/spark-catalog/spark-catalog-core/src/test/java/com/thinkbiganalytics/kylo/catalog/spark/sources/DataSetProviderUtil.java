package com.thinkbiganalytics.kylo.catalog.spark.sources;

/*-
 * #%L
 * Kylo Catalog Core
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

import org.apache.spark.Accumulable;
import org.apache.spark.AccumulableParam;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.UserDefinedFunction;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructType;
import org.mockito.Mockito;

import javax.annotation.Nonnull;

import scala.Function1;
import scala.Option$;
import scala.collection.Seq;
import scala.collection.Seq$;

/**
 * Static utility methods for data set providers
 */
public class DataSetProviderUtil {

    /**
     * Creates an {@link Accumulable} shared variable with a name for display in the Spark UI.
     */
    @Nonnull
    static <R, P1> Accumulable<R, P1> accumulable(@Nonnull final R initialValue, @Nonnull final String name, @Nonnull final AccumulableParam<R, P1> param) {
        return new Accumulable<>(initialValue, param, Option$.MODULE$.apply(name));
    }

    /**
     * Loads a data set using the specified reader and filter.
     */
    @Nonnull
    static DataFrame load() {
        return Mockito.mock(DataFrame.class);
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
     * Instances of {@code DataSetProviderUtil} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private DataSetProviderUtil() {
        throw new UnsupportedOperationException();
    }
}
