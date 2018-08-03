package com.thinkbiganalytics.kylo.catalog.spi;

/*-
 * #%L
 * Kylo Catalog API
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

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Option;

/**
 * Options for reading from or writing to a data set.
 *
 * @see DataSetProvider
 */
public class DataSetOptions {

    private List<String> bucketColumnNames;
    private String format;
    private List<String> jars = new ArrayList<>();
    private SaveMode mode;
    private int numBuckets;
    @SuppressWarnings("unchecked")
    private final Map<String, String> options = new CaseInsensitiveMap();
    private List<String> paths;
    private List<String> partitioningColumns;
    private StructType schema;
    private List<String> sortColumnNames;

    /**
     * Constructs a {@code DataSetOptions}.
     */
    public DataSetOptions() {
        // use defaults
    }

    /**
     * Constructs a {@code DataSetOptions} by copying values from another.
     */
    public DataSetOptions(@Nonnull final DataSetOptions other) {
        bucketColumnNames = (other.bucketColumnNames != null) ? new ArrayList<>(other.bucketColumnNames) : null;
        format = other.format;
        jars.addAll(other.jars);
        mode = other.mode;
        numBuckets = other.numBuckets;
        options.putAll(other.options);
        paths = (other.paths != null) ? new ArrayList<>(other.paths) : null;
        partitioningColumns = (other.partitioningColumns != null) ? new ArrayList<>(other.partitioningColumns) : null;
        schema = other.schema;
        sortColumnNames = (other.sortColumnNames != null) ? new ArrayList<>(other.sortColumnNames) : null;
    }

    @Nullable
    public List<String> getBucketColumnNames() {
        return bucketColumnNames;
    }

    public void setBucketColumnNames(@Nullable final List<String> bucketColumnNames) {
        this.bucketColumnNames = bucketColumnNames;
    }

    @Nullable
    public String getFormat() {
        return format;
    }

    public void setFormat(@Nonnull final String format) {
        this.format = format;
    }

    public void addJar(@Nonnull final String path) {
        jars.add(path);
    }

    public void addJars(@Nonnull final List<String> paths) {
        jars.addAll(paths);
    }

    @Nonnull
    public List<String> getJars() {
        return jars;
    }

    @Nullable
    public SaveMode getMode() {
        return mode;
    }

    public void setMode(@Nullable final SaveMode mode) {
        this.mode = mode;
    }

    public int getNumBuckets() {
        return numBuckets;
    }

    public void setNumBuckets(final int numBuckets) {
        this.numBuckets = numBuckets;
    }

    @Nonnull
    public Option<String> getOption(@Nonnull final String key) {
        return Option.apply(options.get(key));
    }

    @Nonnull
    public Map<String, String> getOptions() {
        return options;
    }

    public void setOption(@Nonnull final String key, @Nullable final String value) {
        options.put(key, value);
    }

    public void setOptions(@Nonnull final Map<String, String> options) {
        this.options.putAll(options);
    }

    @Nullable
    public List<String> getPartitioningColumns() {
        return partitioningColumns;
    }

    public void setPartitioningColumns(@Nullable final List<String> partitioningColumns) {
        this.partitioningColumns = partitioningColumns;
    }

    @Nullable
    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(@Nonnull final List<String> paths) {
        this.paths = paths;
    }

    @Nullable
    public StructType getSchema() {
        return schema;
    }

    public void setSchema(@Nullable final StructType schema) {
        this.schema = schema;
    }

    @Nullable
    public List<String> getSortColumnNames() {
        return sortColumnNames;
    }

    public void setSortColumnNames(@Nullable final List<String> sortColumnNames) {
        this.sortColumnNames = sortColumnNames;
    }
}
