package com.thinkbiganalytics.kylo.catalog.spark.sources;

/*-
 * #%L
 * Kylo Catalog Core
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
import com.thinkbiganalytics.kylo.catalog.spark.SparkUtil;
import com.thinkbiganalytics.kylo.catalog.spark.sources.spark.BooleanOrAccumulatorParam;
import com.thinkbiganalytics.kylo.catalog.spark.sources.spark.DeletePathsListener;
import com.thinkbiganalytics.kylo.catalog.spark.sources.spark.FlaggingVisitor;
import com.thinkbiganalytics.kylo.catalog.spark.sources.spark.SparkDataSetContext;
import com.thinkbiganalytics.kylo.catalog.spark.sources.spark.SparkDataSetDelegate;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetProvider;

import org.apache.spark.Accumulable;
import org.apache.spark.AccumulableParam;
import org.apache.spark.scheduler.SparkListenerJobEnd;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.StructType;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Function1;
import scala.Unit;
import scala.collection.JavaConversions;
import scala.collection.Seq;
import scala.collection.Seq$;

/**
 * Base implementation of a data set provider that can read from and write to any Spark data source.
 *
 * <p>A high water mark is supported for file formats to ensure that files are only ingested once. When the {@code highwatermark} option is given, the paths are listed and filtered by their
 * modification time for changes occurring after the previous use of the data set. The high water mark is then updated and written to {@link KyloCatalogClient}.</p>
 *
 * @param <T> Spark {@code DataFrame} class
 */
abstract class AbstractSparkDataSetProvider<T> implements DataSetProvider<T>, SparkDataSetDelegate<T> {

    /**
     * Option key to specify that source files should not be deleted
     */
    private static final String KEEP_SOURCE_FILE_OPTION = "keepsourcefile";

    @Override
    public final boolean supportsFormat(@Nonnull final String source) {
        return true;  // supports any format supported by Spark
    }

    @Nonnull
    @Override
    public final T read(@Nonnull final KyloCatalogClient<T> client, @Nonnull final DataSetOptions options) {
        // Prepare reader
        final SparkDataSetContext<T> context = new SparkDataSetContext<>(options, client, this);
        final DataFrameReader reader = SparkUtil.prepareDataFrameReader(getDataFrameReader(client, context), context, client);
        final List<String> paths = context.getPaths();

        // Load and union data sets
        T dataSet = null;

        if (paths == null || paths.isEmpty() || context.isFileFormat()) {
            final Seq<String> pathSeq = (paths != null) ? JavaConversions.asScalaBuffer(paths) : null;
            dataSet = load(reader, pathSeq);
        } else {
            for (final String path : paths) {
                T load = load(reader, Seq$.MODULE$.<String>newBuilder().$plus$eq(path).result());
                dataSet = (dataSet == null) ? load : union(dataSet, load);
            }
        }

        // Delete files on job end
        if (context.isFileFormat() && "false".equalsIgnoreCase(SparkUtil.getOrElse(context.getOption(KEEP_SOURCE_FILE_OPTION), ""))) {
            final StructType schema = schema(dataSet);
            if (!schema.isEmpty()) {
                // Watch for when data set is read
                final Accumulable<Boolean, Boolean> accumulator = accumulable(Boolean.FALSE, UUID.randomUUID().toString(), new BooleanOrAccumulatorParam(), client);
                final FlaggingVisitor visitor = new FlaggingVisitor(accumulator);
                dataSet = map(dataSet, schema.apply(0).name(), visitor, schema.apply(0).dataType());

                // Delete paths on job end
                final DeletePathsListener jobListener = new DeletePathsListener(context.getPaths(), accumulator, getHadoopConfiguration(client));
                onJobEnd(jobListener, client);
            }
        }

        return dataSet;
    }

    @Override
    public final void write(@Nonnull final KyloCatalogClient<T> client, @Nonnull final DataSetOptions options, @Nonnull final T dataSet) {
        final DataFrameWriter writer = SparkUtil.prepareDataFrameWriter(getDataFrameWriter(dataSet, options), options, client);
        writer.save();
    }

    /**
     * Creates an {@link Accumulable} shared variable with a name for display in the Spark UI.
     */
    @Nonnull
    protected abstract <R, P1> Accumulable<R, P1> accumulable(@Nonnull R initialValue, @Nonnull String name, @Nonnull AccumulableParam<R, P1> param, @Nonnull KyloCatalogClient<T> client);

    /**
     * Gets a reader from the specified client.
     *
     * <p>The options, format, and scheme will be applied to the reader before loading.</p>
     */
    @Nonnull
    protected abstract DataFrameReader getDataFrameReader(@Nonnull KyloCatalogClient<T> client, @Nonnull DataSetOptions options);

    /**
     * Gets a writer for the specified data set.
     *
     * <p>The options, format, mode, and partitioning will be applied to the writer before saving.</p>
     */
    @Nonnull
    protected abstract DataFrameWriter getDataFrameWriter(@Nonnull T dataSet, @Nonnull DataSetOptions options);

    /**
     * Loads a data set using the specified reader and paths.
     */
    @Nonnull
    protected abstract T load(@Nonnull DataFrameReader reader, @Nullable Seq<String> paths);

    /**
     * Applies the specified function to the specified field of the data set.
     */
    @Nonnull
    protected abstract T map(@Nonnull T dataSet, @Nonnull String fieldName, @Nonnull Function1 function, @Nonnull DataType returnType);

    /**
     * Executes the specified function when a Spark job ends.
     */
    protected abstract void onJobEnd(@Nonnull Function1<SparkListenerJobEnd, Unit> function, @Nonnull KyloCatalogClient<T> client);

    /**
     * Returns the schema of the specified data set.
     */
    @Nonnull
    protected abstract StructType schema(@Nonnull T dataSet);

    /**
     * Returns the union of rows for the specified data sets.
     */
    @Nonnull
    protected abstract T union(@Nonnull T left, @Nonnull T right);
}
