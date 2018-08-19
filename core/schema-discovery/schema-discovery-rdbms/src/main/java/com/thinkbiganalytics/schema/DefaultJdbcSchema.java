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

import com.thinkbiganalytics.discovery.schema.JdbcSchema;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A table schema.
 */
public class DefaultJdbcSchema implements JdbcSchema {

    /**
     * Catalog name
     */
    @Nullable
    private String catalog;

    /**
     * Schema name
     */
    @Nonnull
    private final String schema;

    /**
     * Returns a function that converts the current result set item to a {@code DefaultJdbcSchema}.
     */
    @Nonnull
    public static <T extends JdbcSchema> JdbcFunction<ResultSet, T> fromResultSet() {
        return new JdbcFunction<ResultSet, T>() {
            @Override
            @SuppressWarnings("unchecked")
            public T apply(final ResultSet resultSet) throws SQLException {
                return (T) DefaultJdbcSchema.fromResultSet(resultSet);
            }
        };
    }

    /**
     * Creates a {@code DefaultJdbcSchema} from the current result set.
     */
    @Nonnull
    public static DefaultJdbcSchema fromResultSet(@Nonnull final ResultSet resultSet) throws SQLException {
        final DefaultJdbcSchema schema = new DefaultJdbcSchema(resultSet.getString(JdbcConstants.TABLE_SCHEM));
        schema.setCatalog(resultSet.getString(JdbcConstants.TABLE_CATALOG));
        return schema;
    }

    /**
     * Constructs a {@code DefaultJdbcSchema} with the specified schema name.
     */
    public DefaultJdbcSchema(@Nonnull final String schema) {
        this.schema = schema;
    }

    @Nullable
    @Override
    public String getCatalog() {
        return catalog;
    }

    /**
     * Sets the catalog name.
     */
    public void setCatalog(@Nullable final String catalog) {
        this.catalog = catalog;
    }

    @Nonnull
    @Override
    public String getSchema() {
        return schema;
    }
}
