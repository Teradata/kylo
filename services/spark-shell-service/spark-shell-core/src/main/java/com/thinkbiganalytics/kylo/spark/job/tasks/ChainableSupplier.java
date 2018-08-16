package com.thinkbiganalytics.kylo.spark.job.tasks;

/*-
 * #%L
 * Spark Shell Core
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

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * Represents a supplier of results. Adds utility methods to Java's {@link Supplier} interface.
 *
 * @param <T> type of results supplied by this supplier
 */
@FunctionalInterface
public interface ChainableSupplier<T> extends Supplier<T> {

    /**
     * Chains the specified function to execute after this supplier.
     */
    @Nonnull
    default <R> ChainableSupplier<R> andThen(@Nonnull final Function<? super T, ? extends R> after) {
        Objects.requireNonNull(after);
        return () -> after.apply(get());
    }
}
