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

import com.thinkbiganalytics.discovery.schema.JdbcCatalog;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nonnull;

/**
 * A table catalog.
 */
public class DefaultJdbcCatalog implements JdbcCatalog {

    /**
     * Catalog name
     */
    @Nonnull
    private final String catalog;

    /**
     * Returns a function that converts the current result set item to a {@code DefaultJdbcCatalog}.
     */
    @Nonnull
    public static <T extends JdbcCatalog> JdbcFunction<ResultSet, T> fromResultSet() {
        return new JdbcFunction<ResultSet, T>() {
            @Override
            @SuppressWarnings("unchecked")
            public T apply(final ResultSet resultSet) throws SQLException {
                return (T) DefaultJdbcCatalog.fromResultSet(resultSet);
            }
        };
    }

    /**
     * Creates a {@code DefaultJdbcCatalog} from the current result set.
     */
    @Nonnull
    public static DefaultJdbcCatalog fromResultSet(@Nonnull final ResultSet resultSet) throws SQLException {
        return new DefaultJdbcCatalog(resultSet.getString(JdbcConstants.TABLE_CAT));
    }

    /**
     * Constructs a {@code DefaultJdbcCatalog} with the specified catalog name.
     */
    public DefaultJdbcCatalog(@Nonnull final String catalog) {
        this.catalog = catalog;
    }

    @Nonnull
    @Override
    public String getCatalog() {
        return catalog;
    }
}
