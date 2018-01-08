package com.thinkbiganalytics.spark.jdbc;

/*-
 * #%L
 * kylo-spark-shell-client-app
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
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

import org.apache.spark.sql.jdbc.JdbcDialect;

import java.sql.Types;

import javax.annotation.Nonnull;

/**
 * Provides compatibility with a JDBC database.
 */
public abstract class Dialect extends JdbcDialect {

    /**
     * Generates the {@code CREATE TABLE AS SELECT} query into the specified table with the specified {@code SELECT} query.
     *
     * @param table  the target table name
     * @param select the {@code SELECT} query
     * @return the {@code CREATE TABLE AS SELECT} query
     */
    public String createTableAs(@Nonnull final String table, @Nonnull final String select) {
        return "CREATE TABLE " + table + " AS (" + select + ")";
    }

    /**
     * Gets the converter for the specified SQL type.
     */
    @Nonnull
    public Converter getConverter(final int sqlType) {
        switch (sqlType) {
            case Types.BLOB:
                return Converters.blobType();

            case Types.CLOB:
                return Converters.clobType();

            case Types.TIME:
                return Converters.timestampType();

            default:
                return Converters.identity();
        }
    }
}
