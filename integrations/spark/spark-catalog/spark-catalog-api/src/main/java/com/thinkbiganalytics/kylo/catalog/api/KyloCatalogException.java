package com.thinkbiganalytics.kylo.catalog.api;

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

import javax.annotation.Nullable;

/**
 * Throw to indicate an problem with the Kylo Catalog.
 */
public class KyloCatalogException extends RuntimeException {

    private static final long serialVersionUID = 5054890442630358142L;

    /**
     * Construct a {@code KyloCatalogException} with the specified message.
     */
    public KyloCatalogException(@Nullable final String message) {
        super(message);
    }

    /**
     * Construct a {@code KyloCatalogException} with the specified message and cause.
     */
    public KyloCatalogException(@Nullable final String message, @Nullable final Throwable cause) {
        super(message, cause);
    }
}
