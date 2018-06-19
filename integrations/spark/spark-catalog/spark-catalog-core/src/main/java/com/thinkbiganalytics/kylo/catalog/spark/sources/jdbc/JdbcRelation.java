package com.thinkbiganalytics.kylo.catalog.spark.sources.jdbc;

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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.thinkbiganalytics.kylo.catalog.spark.DataSourceResourceLoader;

import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.BaseRelation;
import org.apache.spark.sql.sources.Filter;
import org.apache.spark.sql.sources.PrunedFilteredScan;
import org.apache.spark.sql.types.StructType;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A data source for reading from and writing to JDBC tables.
 */
public class JdbcRelation extends BaseRelation implements PrunedFilteredScan {

    /**
     * JDBC relation
     */
    @Nonnull
    private final BaseRelation delegate;

    /**
     * Class loader with JDBC drivers
     */
    private final DataSourceResourceLoader loader;

    /**
     * Constructs a {@code JdbcRelation} using the specified JDBC relation and class loader.
     */
    public JdbcRelation(@Nonnull final BaseRelation delegate, @Nonnull final ClassLoader classLoader) {
        this.delegate = delegate;
        loader = (classLoader instanceof DataSourceResourceLoader) ? (DataSourceResourceLoader) classLoader : null;
    }

    @Nonnull
    @Override
    public RDD<Row> buildScan(@Nonnull final String[] requiredColumns, @Nonnull final Filter[] filters) {
        if (!(delegate instanceof PrunedFilteredScan)) {
            throw new IllegalStateException("Not a supported JDBC relation: " + delegate);
        } else if (loader == null) {
            return ((PrunedFilteredScan) delegate).buildScan(requiredColumns, filters);
        } else {
            try {
                return loader.runWithThreadContext(new Callable<RDD<Row>>() {
                    @Override
                    public RDD<Row> call() {
                        return ((PrunedFilteredScan) delegate).buildScan(requiredColumns, filters);
                    }
                });
            } catch (final Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    @Override
    public boolean needConversion() {
        return delegate.needConversion();
    }

    @Nonnull
    @Override
    public StructType schema() {
        return delegate.schema();
    }

    @Nonnull
    @Override
    public SQLContext sqlContext() {
        return delegate.sqlContext();
    }

    @Nonnull
    @Override
    public Filter[] unhandledFilters(Filter[] filters) {
        return delegate.unhandledFilters(filters);
    }

    /**
     * Gets the wrapped JDBC relation.
     */
    @Nonnull
    @VisibleForTesting
    BaseRelation getDelegate() {
        return delegate;
    }

    /**
     * Gets the class loader for JDBC drivers.
     */
    @Nullable
    @VisibleForTesting
    DataSourceResourceLoader getLoader() {
        return loader;
    }
}
