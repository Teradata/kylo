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

import com.google.common.base.Function;
import com.google.common.base.Throwables;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.jdbc.JdbcDialect;
import org.apache.spark.sql.jdbc.JdbcDialects$;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Converts a JDBC ResultSet into a Spark SQL Row.
 */
public class RowTransform implements Function<ResultSet, Row>, Serializable {

    private static final long serialVersionUID = -2975559337323162214L;

    /**
     * Data converters
     */
    @Nullable
    private transient Converter[] converters;

    @Nullable
    @Override
    public Row apply(@Nullable final ResultSet rs) {
        try {
            return (rs != null) ? mapRow(rs) : null;
        } catch (final SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Gets the converters for the specified ResultSet.
     *
     * @param rs the result set
     * @return the converters
     * @throws SQLException if a SQL error occurs
     */
    @Nonnull
    private Converter[] getConverters(@Nonnull final ResultSet rs) throws SQLException {
        if (converters == null) {
            final String url = rs.getStatement().getConnection().getMetaData().getURL();
            final JdbcDialect dialect = JdbcDialects$.MODULE$.get(url);
            final ResultSetMetaData rsmd = rs.getMetaData();

            final int columnCount = rsmd.getColumnCount();
            final List<Converter> converters = new ArrayList<>(columnCount);

            for (int i = 1; i <= columnCount; ++i) {
                final int columnType = rsmd.getColumnType(i);

                if (dialect instanceof Dialect) {
                    converters.add(((Dialect) dialect).getConverter(columnType));
                } else {
                    converters.add(Converters.identity());
                }
            }

            this.converters = converters.toArray(new Converter[0]);
        }
        return converters;
    }

    /**
     * Converts the specified JDBC ResultSet into a Spark SQL Row.
     *
     * @param rs the result set
     * @return the Spark SQL row
     * @throws SQLException if a SQL error occurs
     */
    @Nonnull
    private Row mapRow(@Nonnull final ResultSet rs) throws SQLException {
        final Converter[] converters = getConverters(rs);
        final int columnCount = converters.length;
        final Object[] values = new Object[columnCount];

        for (int i = 0; i < columnCount; ++i) {
            values[i] = converters[i].convert(rs, i + 1);
        }

        return RowFactory.create(values);
    }
}
