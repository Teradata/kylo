package com.thinkbiganalytics.discovery.schema;

/*-
 * #%L
 * kylo-schema-discovery-api
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A database table.
 */
public interface JdbcTable {

    /**
     * Returns the table catalog name.
     */
    @Nullable
    String getCatalog();

    /**
     * Returns the table name.
     */
    @Nonnull
    String getName();

    /**
     * Returns the quoted identifier and database qualifier.
     */
    @Nonnull
    String getQualifiedIdentifier();

    /**
     * Returns the explanatory comment on the table.
     */
    @Nullable
    String getRemarks();

    /**
     * Returns the table schema name.
     */
    @Nullable
    String getSchema();

    /**
     * Returns the table type. Typical types are "TABLE" and "VIEW".
     */
    @Nonnull
    String getType();
}
