package com.thinkbiganalytics.schema;

/*-
 * #%L
 * kylo-schema-discovery-rdbms
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

import java.sql.DatabaseMetaData;

public class JdbcConstants {

    /**
     * Explanatory comment on the table from {@link DatabaseMetaData#getTables(String, String, String, String[]) getTables}.
     */
    public static final String REMARKS = "REMARKS";

    /**
     * Catalog name from {@link DatabaseMetaData#getCatalogs() getCatalogs} and {@link DatabaseMetaData#getTables(String, String, String, String[]) getTables}.
     */
    public static final String TABLE_CAT = "TABLE_CAT";

    /**
     * Catalog name from {@link DatabaseMetaData#getSchemas(String, String) getSchemas}.
     */
    public static final String TABLE_CATALOG = "TABLE_CATALOG";

    /**
     * Table name from {@link DatabaseMetaData#getTables(String, String, String, String[]) getTables}.
     */
    public static final String TABLE_NAME = "TABLE_NAME";

    /**
     * Schema name from {@link DatabaseMetaData#getSchemas(String, String) getSchemas} and {@link DatabaseMetaData#getTables(String, String, String, String[]) getTables}.
     */
    public static final String TABLE_SCHEM = "TABLE_SCHEM";

    /**
     * Table type from {@link DatabaseMetaData#getTables(String, String, String, String[]) getTables}. Typical types are "TABLE" and "VIEW".
     */
    public static final String TABLE_TYPE = "TABLE_TYPE";

    /**
     * Instances of {@code JdbcConstants} should not be constructed.
     */
    private JdbcConstants() {
        throw new UnsupportedOperationException();
    }
}
