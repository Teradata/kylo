package com.thinkbiganalytics.kylo.catalog.spark.sources.jdbc;

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

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Function1;
import scala.runtime.AbstractFunction1;

/**
 * A Spark user-defined function that accumulates every input value in a {@link JdbcHighWaterMark}.
 *
 * @param <T> value type
 */
public class JdbcHighWaterMarkVisitor<T> extends AbstractFunction1<T, T> implements Serializable {

    private static final long serialVersionUID = -3212596003086103873L;

    /**
     * High water mark accumulator
     */
    @Nonnull
    private final Accumulable<JdbcHighWaterMark, Long> accumulable;

    /**
     * Converts values to {@link Long}
     */
    @Nonnull
    @SuppressWarnings("squid:S1948")
    private final Function1<T, Long> toLong;

    /**
     * Constructs a {@code JdbcHighWaterMarkVisitor} with the specified accumulable and {@link Long} converter.
     */
    public JdbcHighWaterMarkVisitor(@Nonnull final Accumulable<JdbcHighWaterMark, Long> accumulable, @Nonnull final Function1<T, Long> toLong) {
        this.accumulable = accumulable;
        this.toLong = toLong;
    }

    @Nullable
    @Override
    public T apply(@Nullable final T value) {
        accumulable.$plus$eq(toLong.apply(value));
        return value;
    }
}
