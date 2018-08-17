package com.thinkbiganalytics.kylo.catalog.spark;

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

import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClientBuilder;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogDataSetBuilder;
import com.thinkbiganalytics.kylo.catalog.rest.model.DefaultDataSetTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Builds a data set and adds it to a {@link KyloCatalogClientBuilder}.
 *
 * @param <T> Spark {@code DataFrame} class
 * @see KyloCatalogClientBuilder#addDataSet(String)
 */
public class DefaultKyloCatalogDataSetBuilder<T> extends AbstractKyloCatalogDataSetAccess<KyloCatalogDataSetBuilder<T>> implements KyloCatalogDataSetBuilder<T> {

    /**
     * Parent Kylo Catalog client builder
     */
    @Nonnull
    private final AbstractKyloCatalogClientBuilder<T> clientBuilder;

    /**
     * Data set
     */
    @Nonnull
    private final DefaultDataSetTemplate dataSet = new DefaultDataSetTemplate();

    /**
     * Data set identifier
     */
    @Nonnull
    private final String id;

    /**
     * Constructs a {@code DefaultKyloCatalogDataSetBuilder}.
     */
    DefaultKyloCatalogDataSetBuilder(@Nonnull final String id, @Nonnull final AbstractKyloCatalogClientBuilder<T> clientBuilder) {
        this.id = id;
        this.clientBuilder = clientBuilder;
    }

    @Nonnull
    @Override
    public KyloCatalogDataSetBuilder<T> addFile(@Nullable final String path) {
        if (path != null) {
            if (dataSet.getFiles() == null) {
                dataSet.setFiles(new ArrayList<String>());
            }
            dataSet.getFiles().add(path);
        }
        return this;
    }

    @Nonnull
    @Override
    public KyloCatalogDataSetBuilder<T> addJar(@Nullable final String path) {
        if (path != null) {
            if (dataSet.getJars() == null) {
                dataSet.setJars(new ArrayList<String>());
            }
            dataSet.getJars().add(path);
        }
        return this;
    }

    @Nonnull
    @Override
    public KyloCatalogDataSetBuilder<T> addJars(@Nullable final List<String> paths) {
        if (paths != null) {
            if (dataSet.getJars() == null) {
                dataSet.setJars(new ArrayList<>(paths));
            } else {
                dataSet.getJars().addAll(paths);
            }
        }
        return this;
    }

    @Nonnull
    @Override
    public KyloCatalogDataSetBuilder<T> format(@Nonnull final String source) {
        dataSet.setFormat(source);
        return this;
    }

    @Nonnull
    @Override
    public KyloCatalogDataSetBuilder<T> option(@Nonnull final String key, @Nullable final String value) {
        if (dataSet.getOptions() == null) {
            dataSet.setOptions(new HashMap<String, String>());
        }
        dataSet.getOptions().put(key, value);
        return this;
    }

    @Nonnull
    @Override
    public KyloCatalogDataSetBuilder<T> paths(@Nonnull final String... paths) {
        dataSet.setPaths(Arrays.asList(paths));
        return this;
    }

    @Nonnull
    @Override
    public KyloCatalogClientBuilder<T> save() {
        clientBuilder.addDataSet(id, dataSet);
        return clientBuilder;
    }
}
