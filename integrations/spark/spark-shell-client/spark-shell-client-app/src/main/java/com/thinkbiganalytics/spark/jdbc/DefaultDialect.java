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
import org.apache.spark.sql.jdbc.JdbcType;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.MetadataBuilder;

import java.sql.Connection;

import javax.annotation.Nonnull;

import scala.Option;
import scala.collection.immutable.Map;

/**
 * Wraps a {@code JdbcDialect} and provides default implementations of Dialect methods.
 */
public class DefaultDialect extends Dialect {

    private static final long serialVersionUID = 3850581911354957856L;

    /**
     * JDBC dialect to delegate to
     */
    @Nonnull
    private final JdbcDialect delegate;

    /**
     * Constructs a {@code DefaultDialect} that forwards to the specified JDBC dialect.
     */
    public DefaultDialect(@Nonnull final JdbcDialect delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean canHandle(String url) {
        return delegate.canHandle(url);
    }

    @Override
    public Option<DataType> getCatalystType(int sqlType, String typeName, int size, MetadataBuilder md) {
        return delegate.getCatalystType(sqlType, typeName, size, md);
    }

    @Override
    public Option<JdbcType> getJDBCType(DataType dt) {
        return delegate.getJDBCType(dt);
    }

    @Override
    public String quoteIdentifier(String colName) {
        return delegate.quoteIdentifier(colName);
    }

    @Override
    public String getTableExistsQuery(String table) {
        return delegate.getTableExistsQuery(table);
    }

    @Override
    public void beforeFetch(Connection connection, Map<String, String> properties) {
        delegate.beforeFetch(connection, properties);
    }
}
