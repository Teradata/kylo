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

import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogDataSetAccess;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * A skeletal implementation of {@link KyloCatalogDataSetAccess} that provides default implementations for several methods.
 *
 * @param <R> builder type
 */
@SuppressWarnings("unchecked")
public abstract class AbstractKyloCatalogDataSetAccess<R extends KyloCatalogDataSetAccess> implements KyloCatalogDataSetAccess<R> {

    @Nonnull
    @Override
    public final R addFiles(@Nullable final List<String> paths) {
        if (paths != null) {
            for (final String path : paths) {
                addFile(path);
            }
        }
        return (R) this;
    }

    @Nonnull
    @Override
    public final R addFiles(@Nullable final Seq<String> paths) {
        if (paths != null) {
            addFiles(JavaConversions.seqAsJavaList(paths));
        }
        return (R) this;
    }

    @Nonnull
    @Override
    public final R addJars(@Nullable final Seq<String> paths) {
        if (paths != null) {
            addJars(JavaConversions.seqAsJavaList(paths));
        }
        return (R) this;
    }

    @Nonnull
    @Override
    public final R option(@Nonnull final String key, final boolean value) {
        return option(key, Boolean.toString(value));
    }

    @Nonnull
    @Override
    public final R option(@Nonnull final String key, final double value) {
        return option(key, Double.toString(value));
    }

    @Nonnull
    @Override
    public final R option(@Nonnull final String key, final long value) {
        return option(key, Long.toString(value));
    }

    @Nonnull
    @Override
    public final R options(@Nullable final Map<String, String> options) {
        if (options != null) {
            for (final Map.Entry<String, String> entry : options.entrySet()) {
                option(entry.getKey(), entry.getValue());
            }
        }
        return (R) this;
    }

    @Nonnull
    @Override
    public final R options(@Nullable final scala.collection.Map<String, String> options) {
        if (options != null) {
            options(JavaConversions.mapAsJavaMap(options));
        }
        return (R) this;
    }
}
