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

import com.thinkbiganalytics.discovery.schema.JdbcTable;

import org.apache.commons.lang3.StringUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A database table from JDBC.
 */
public class DefaultJdbcTable implements JdbcTable {

    private String catalog;
    private String catalogSeparator;
    private String identifierQuoteString;

    /**
     * Table name
     */
    @Nonnull
    private final String name;

    private String remarks;
    private String schema;

    /**
     * Table type
     */
    @Nonnull
    private final String type;

    /**
     * Returns a function that converts the current result set item to a {@code DefaultJdbcTable}.
     */
    @Nonnull
    public static <T extends JdbcTable> JdbcFunction<ResultSet, T> fromResultSet(@Nullable final DatabaseMetaData databaseMetaData) {
        return new JdbcFunction<ResultSet, T>() {
            @Override
            @SuppressWarnings("unchecked")
            public T apply(final ResultSet resultSet) throws SQLException {
                return (T) fromResultSet(resultSet, databaseMetaData);
            }
        };
    }

    /**
     * Extracts the table definition from the current row in the specified result set.
     */
    public static DefaultJdbcTable fromResultSet(@Nonnull final ResultSet resultSet, @Nullable final DatabaseMetaData databaseMetaData) throws SQLException {
        final DefaultJdbcTable table = new DefaultJdbcTable(resultSet.getString(JdbcConstants.TABLE_NAME), resultSet.getString(JdbcConstants.TABLE_TYPE));
        table.setCatalog(resultSet.getString(JdbcConstants.TABLE_CAT));
        table.setRemarks(resultSet.getString(JdbcConstants.REMARKS));
        table.setSchema(resultSet.getString(JdbcConstants.TABLE_SCHEM));
        if (databaseMetaData != null) {
            table.setMetaData(databaseMetaData);
        }
        return table;
    }

    /**
     * Constructs a {@code DefaultDBTable} with the specified table name and type.
     */
    public DefaultJdbcTable(@Nonnull final String name, @Nonnull final String type) {
        this.name = name;
        this.type = type;
    }

    @Nullable
    @Override
    public String getCatalog() {
        return catalog;
    }

    /**
     * Sets the table catalog name.
     */
    public void setCatalog(@Nullable final String catalog) {
        this.catalog = catalog;
    }

    /**
     * Sets the string used as the separator between a catalog and table name.
     */
    public void setCatalogSeparator(@Nullable final String catalogSeparator) {
        this.catalogSeparator = catalogSeparator;
    }

    /**
     * Sets the string used to quote SQL identifiers, or " " if identifier quoting is not supported.
     */
    public void setIdentifierQuoteString(@Nullable final String identifierQuoteString) {
        this.identifierQuoteString = identifierQuoteString;
    }

    /**
     * Sets the catalog separator and identifier quote string using the specified database metadata.
     */
    public DefaultJdbcTable setMetaData(@Nonnull final DatabaseMetaData metaData) throws SQLException {
        setCatalogSeparator(metaData.getCatalogSeparator());
        setIdentifierQuoteString(metaData.getIdentifierQuoteString());
        return this;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public String getQualifiedIdentifier() {
        final String separator = StringUtils.isNotEmpty(catalogSeparator) ? catalogSeparator : ".";
        final StringBuilder identifier = new StringBuilder();
        if (StringUtils.isNotEmpty(catalog)) {
            identifier.append(quoteIdentifier(catalog));
            identifier.append(separator);
        }
        if (StringUtils.isNotEmpty(schema)) {
            identifier.append(quoteIdentifier(schema));
            identifier.append(separator);
        }
        identifier.append(quoteIdentifier(name));
        return identifier.toString();
    }

    @Nullable
    @Override
    public String getRemarks() {
        return remarks;
    }

    /**
     * Sets the explanatory comment on the table.
     */
    public void setRemarks(@Nullable final String remarks) {
        this.remarks = remarks;
    }

    @Nullable
    @Override
    public String getSchema() {
        return schema;
    }

    /**
     * Sets the table schema name.
     */
    public void setSchema(@Nullable final String schema) {
        this.schema = schema;
    }

    @Nonnull
    @Override
    public String getType() {
        return type;
    }

    @Nonnull
    private String quoteIdentifier(@Nonnull final String identifier) {
        if (!StringUtils.equalsAny(identifierQuoteString, null, "", " ")) {
            return identifierQuoteString + identifier.replace(identifierQuoteString, identifierQuoteString + identifierQuoteString) + identifierQuoteString;
        } else {
            return identifier;
        }
    }
}
