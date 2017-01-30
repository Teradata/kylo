package com.thinkbiganalytics.util;

/*-
 * #%L
 * thinkbig-nifi-hadoop-processors
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

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * An adapter class that provides certain default actions for objects of type ResultSetMetaData
 */
public class ResultSetMetaAdapter implements ResultSetMetaData {

    private ResultSetMetaData meta;

    public ResultSetMetaAdapter(ResultSetMetaData meta) {
        this.meta = meta;
    }

    public String getCatalogName(int column) throws SQLException {
        return meta.getCatalogName(column);
    }

    public String getColumnClassName(int column) throws SQLException {
        return meta.getColumnClassName(column);
    }

    public int getColumnCount() throws SQLException {
        return meta.getColumnCount();
    }

    public int getColumnDisplaySize(int column) throws SQLException {
        return meta.getColumnDisplaySize(column);
    }

    public String getColumnLabel(int column) throws SQLException {
        return meta.getColumnLabel(column);
    }

    public String getColumnName(int column) throws SQLException {
        String name = meta.getColumnName(column);
        if (name.contains(".")) {
            return name.split("\\.")[1];
        }
        return name;
    }

    public int getColumnType(int column) throws SQLException {
        return meta.getColumnType(column);
    }

    public String getColumnTypeName(int column) throws SQLException {
        return meta.getColumnTypeName(column);
    }

    public int getPrecision(int column) throws SQLException {
        return meta.getPrecision(column);
    }

    public int getScale(int column) throws SQLException {
        return meta.getScale(column);
    }

    public String getSchemaName(int column) throws SQLException {
        return meta.getSchemaName(column);
    }

    public String getTableName(int column) throws SQLException {
        String name = meta.getColumnName(column);
        if (name.contains(".")) {
            return name.split("\\.")[0];
        }
        return meta.getTableName(column);
    }

    public boolean isAutoIncrement(int column) throws SQLException {
        return meta.isAutoIncrement(column);
    }

    public boolean isCaseSensitive(int column) throws SQLException {
        return meta.isCaseSensitive(column);
    }

    public boolean isCurrency(int column) throws SQLException {
        return meta.isCurrency(column);
    }

    public boolean isDefinitelyWritable(int column) throws SQLException {
        return meta.isDefinitelyWritable(column);
    }

    public int isNullable(int column) throws SQLException {
        return meta.isNullable(column);
    }

    public boolean isReadOnly(int column) throws SQLException {
        return meta.isReadOnly(column);
    }

    public boolean isSearchable(int column) throws SQLException {
        return meta.isSearchable(column);
    }

    public boolean isSigned(int column) throws SQLException {
        // Currently an unsupported method should just return true
        try {
            return meta.isSigned(column);
        } catch (SQLException e) {
            return true;
        }
    }

    public boolean isWritable(int column) throws SQLException {
        return meta.isWritable(column);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return meta.isWrapperFor(iface);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return meta.unwrap(iface);
    }
}
