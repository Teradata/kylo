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

import com.thinkbiganalytics.kylo.catalog.api.MissingOptionException;
import com.thinkbiganalytics.kylo.catalog.spi.DataSetOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import scala.Option;

/**
 * Static support methods for interacting with data sets.
 */
@SuppressWarnings("WeakerAccess")
public class DataSetUtil {

    /**
     * Gets the specified option value or throws an exception.
     *
     * @throws MissingOptionException if the option is not defined
     */
    @Nonnull
    public static String getOptionOrThrow(@Nonnull final DataSetOptions options, @Nonnull final String key, @Nullable final String errorMessage) {
        final Option<String> value = options.getOption(key);
        if (value.isDefined()) {
            return value.get();
        } else {
            throw new MissingOptionException((errorMessage == null) ? "Missing required option: " + key : errorMessage);
        }
    }

    /**
     * Instances of {@code DataSetUtil} should not be constructed.
     */
    private DataSetUtil() {
        throw new UnsupportedOperationException();
    }
}
