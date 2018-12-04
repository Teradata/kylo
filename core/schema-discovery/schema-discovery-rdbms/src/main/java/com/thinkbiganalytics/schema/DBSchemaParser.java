package com.thinkbiganalytics.schema;

/*-
 * #%L
 * thinkbig-schema-discovery-rdbms
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

import com.thinkbiganalytics.discovery.model.DefaultField;
import com.thinkbiganalytics.discovery.model.DefaultTableSchema;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.discovery.util.ParserHelper;
import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.kerberos.KerberosUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.MetaDataAccessException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 * Provides database metadata and schema information from JDBC.
 */
public class DBSchemaParser {

    private static final Logger log = LoggerFactory.getLogger(DBSchemaParser.class);

    /**
     * Column name containing the catalog name
     */
    private static final String CATALOG_COLUMN = "TABLE_CAT";

    /**
     * Column name containing the schema name
     */
    private static final String SCHEMA_COLUMN = "TABLE_SCHEM";

    private final DataSource ds;
    private final KerberosTicketConfiguration kerberosTicketConfiguration;

    public DBSchemaParser(@Nonnull final DataSource ds, @Nonnull final KerberosTicketConfiguration kerberosTicketConfiguration) {
        this.ds = ds;
        this.kerberosTicketConfiguration = kerberosTicketConfiguration;
    }

    /**
     * This will list schemas for a connection.
     * NOTE it will not close the connection
     *
     * @return a list of schema names
     */
    @Nonnull
    private List<String> listSchemas(@Nonnull final Connection conn) throws SQLException {
        List<String> schemas = new ArrayList<>();
        String schemaQuery = "show databases";
        try (ResultSet rs = conn.createStatement().executeQuery(schemaQuery)) {
            while (rs.next()) {
                String schema = rs.getString(1);
                schemas.add(schema);
            }
            return schemas;
        }
    }

    /**
     * List the catalogs for a given connection.
     * NOTE it will not close the connection
     *
     * @return a list of catalog names
     */
    @Nonnull
    private List<String> listCatalogs(@Nonnull final Connection conn) throws SQLException {
        List<String> catalogs = new ArrayList<>();
        try (ResultSet rs = conn.getMetaData().getCatalogs()) {
            while (rs.next()) {
                String cat = rs.getString(CATALOG_COLUMN);
                catalogs.add(cat);
            }
            return catalogs;
        }
    }

    @Nonnull
    public List<String> listSchemas() {
        try (final Connection conn = KerberosUtil.getConnectionWithOrWithoutKerberos(ds, kerberosTicketConfiguration)) {
            return listSchemas(conn);
        } catch (SQLException e) {
            throw new SchemaParserException("Unable to list schemas", e);
        }
    }

    @Nonnull
    public List<String> listCatalogs() {
        try (final Connection conn = KerberosUtil.getConnectionWithOrWithoutKerberos(ds, kerberosTicketConfiguration)) {
            return listCatalogs(conn);
        } catch (SQLException e) {
            throw new SchemaParserException("Unable to list catalogs", e);
        }
    }

    @Nonnull
    public List<String> listSchemasOrCatalogs() {
        try (final Connection conn = KerberosUtil.getConnectionWithOrWithoutKerberos(ds, kerberosTicketConfiguration)) {
            if (conn.getMetaData().supportsCatalogsInIndexDefinitions()) {
                return listCatalogs(conn);
            } else {
//conn.getMetadata.supportsSchemasInIndexDefinitions
                return listSchemas(conn);
            }
        } catch (SQLException e) {
            throw new SchemaParserException("Unable to list schemas/catalogs", e);
        }
    }

    /**
     * Lists the tables in the specified schema that match the specified table pattern.d
     *
     * @param conn      the JDBC connection
     * @param catalog   the catalog name pattern, or {@code null}
     * @param schema    the schema name pattern, or {@code null}
     * @param tableName the table name pattern   @return a result set containing the matching table metadata
     * @return the list of tables or {@code null} if there was a problem
     */
    @Nullable
    private ResultSet getTables(@Nonnull final Connection conn, @Nullable String catalog, @Nullable final String schema, @Nonnull final String tableName) {
        try {
            DatabaseType databaseType = null;
            try {
                databaseType = DatabaseType.fromMetaData(conn);
            } catch (MetaDataAccessException e) {
                //if can't get the db type, then treat it as normal looking for TABLE and VIEW
            }

            if (DatabaseType.TERADATA.equals(databaseType)) {
                return conn.getMetaData().getTables(catalog, schema, tableName, null);  //Teradata-specific
            } else {
                return conn.getMetaData().getTables(catalog, schema, tableName, new String[]{"TABLE", "VIEW"});
            }
        } catch (final SQLException e) {
            log.debug("Failed to list tables for catalog:{} schema:{} tableName:{}", catalog, schema, tableName, e);
            return null;
        }
    }

    private void addTableToList(@Nonnull final ResultSet result, @Nonnull final List<String> tables) throws SQLException {
        final String tableName = result.getString("TABLE_NAME");
        final String tableSchem = result.getString(SCHEMA_COLUMN);
        final String tableCat = result.getString(CATALOG_COLUMN);
        tables.add((tableSchem != null ? tableSchem : tableCat) + "." + tableName);
    }


    /**
     * Lists the tables in the specified schema.
     * Some databases use the catalog (i.e. MySQL), some don't (i.e. Teradata)
     * This should work for all cases.
     *
     * @param schema the schema name, or {@code null}
     * @return the list of table names prepended with the schema name, like: {@code <schema>.<table>}
     * @throws RuntimeException if a database access error occurs
     */
    @Nonnull
    public List<String> listTables(@Nullable final String schema, @Nullable final String tableName) {
        final String schemaPattern = (schema != null) ? schema : "%";

        final String tableNamePattern = (tableName != null) ? tableName : "%";

        final List<String> tables = new ArrayList<>();

        List<String> catalogs = null;
        try {
            catalogs = listCatalogs();
        } catch (Exception e) {
            //ok to catch exception here
        }
        boolean hasCatalogs = catalogs != null && !catalogs.isEmpty();

        if (StringUtils.isNotBlank(schema) || StringUtils.isNotBlank(tableName)) {

            //try using the catalog
            try (final Connection conn = KerberosUtil.getConnectionWithOrWithoutKerberos(ds, kerberosTicketConfiguration)) {
                if (hasCatalogs) {
                    for (final String catalog : catalogs) {
                        try (final ResultSet result = getTables(conn, catalog, schemaPattern, tableNamePattern)) {
                            while (result != null && result.next()) {
                                addTableToList(result, tables);
                            }
                        }
                    }
                } else {
                    try (final ResultSet result = getTables(conn, null, schemaPattern, tableNamePattern)) {
                        while (result != null && result.next()) {
                            addTableToList(result, tables);
                        }
                    }
                }


            } catch (final SQLException e) {
                throw new SchemaParserException("Unable to obtain table list", e);
            }

        } else {

            try {
                if (hasCatalogs) {
                    try (final Connection conn = KerberosUtil.getConnectionWithOrWithoutKerberos(ds, kerberosTicketConfiguration)) {
                        for (final String catalog : catalogs) {
                            try (final ResultSet result = getTables(conn, catalog, "%", tableNamePattern)) {
                                while (result != null && result.next()) {
                                    addTableToList(result, tables);
                                }
                            }
                        }
                    }
                } else {
                    List<String> schemas = listSchemas();
                    try (final Connection conn = KerberosUtil.getConnectionWithOrWithoutKerberos(ds, kerberosTicketConfiguration)) {
                        for (final String dbSchema : schemas) {
                            try (final ResultSet result = getTables(conn, null, dbSchema, tableNamePattern)) {
                                while (result != null && result.next()) {
                                    addTableToList(result, tables);
                                }
                            }
                        }
                    }
                }
            } catch (final SQLException e) {
                throw new SchemaParserException("Unable to obtain table list", e);
            }
        }

        return tables;
    }


    /**
     * Lists the tables in the specified catalog or schema
     * Some databases use the catalog (i.e. MySQL), some don't (i.e. Teradata)
     * This should work for all cases.
     *
     * @param catalog the catalog name
     * @return the list of table names prepended with the schema or catalog name, like: {@code <schema>.<table>}
     * @throws RuntimeException if a database access error occurs
     */
    @Nonnull
    public List<String> listTables(@Nullable final String catalog) {
        final List<String> tables = new ArrayList<>();

        try {
            try (final Connection conn = KerberosUtil.getConnectionWithOrWithoutKerberos(ds, kerberosTicketConfiguration)) {
                if (conn.getMetaData().supportsCatalogsInIndexDefinitions()) {
                    try (final ResultSet result = getTables(conn, catalog, "%", "%")) {
                        while (result != null && result.next()) {
                            addTableToList(result, tables);
                        }
                    }
                } else {
                    try (final ResultSet result = getTables(conn, null, catalog, "%")) {
                        while (result != null && result.next()) {
                            addTableToList(result, tables);
                        }
                    }
                }
            }
        } catch (final SQLException e) {
            throw new SchemaParserException("Unable to obtain table list", e);
        }

        return tables;
    }

    /**
     * Gets the schema for the specified table.
     *
     * @param schema the schema name
     * @param table  the table name
     * @return the table schema
     * @throws IllegalArgumentException if the table name is empty
     * @throws RuntimeException         if a database access error occurs
     */
    @Nullable
    public TableSchema describeTable(@Nullable final String schema, @Nonnull final String table) {
        Validate.isTrue(!StringUtils.isEmpty(table), "Table expected");

        try (final Connection conn = KerberosUtil.getConnectionWithOrWithoutKerberos(ds, kerberosTicketConfiguration)) {
            String queryCatalog = schema;
            String querySchema = schema;
            if (conn.getMetaData().supportsCatalogsInIndexDefinitions()) {
                querySchema = "%";
            } else if (conn.getMetaData().supportsSchemasInIndexDefinitions()) {
                queryCatalog = null;
            }

            try (final ResultSet result = getTables(conn, queryCatalog, querySchema, table)) {
                while (result != null && result.next()) {
                    final String cat = result.getString(1);
                    final String schem = result.getString(2);
                    final String tableName = result.getString(3);
                    if (table.equalsIgnoreCase(tableName) && (queryCatalog != null || schema == null || schem == null || schema.equalsIgnoreCase(schem))) {
                        final DefaultTableSchema tableSchema = new DefaultTableSchema();
                        tableSchema.setFields(listColumns(conn, cat, schem, tableName, true));
                        tableSchema.setName(tableName);
                        tableSchema.setSchemaName(StringUtils.isBlank(schem) ? cat : schem);
                        return tableSchema;
                    }
                }
            }
            try (final ResultSet result = getTables(conn, (queryCatalog == null) ? schema : null, "%".equals(querySchema) ? schema : "%", table)) {
                while (result != null && result.next()) {
                    final String cat = result.getString(1);
                    final String schem = result.getString(2);
                    final String tableName = result.getString(3);
                    if (table.equalsIgnoreCase(tableName) && (queryCatalog != null || schema == null || schem == null || schema.equalsIgnoreCase(schem))) {
                        final DefaultTableSchema tableSchema = new DefaultTableSchema();
                        tableSchema.setFields(listColumns(conn, cat, schem, tableName, false));
                        tableSchema.setName(tableName);
                        tableSchema.setSchemaName(StringUtils.isBlank(schem) ? cat : schem);
                        return tableSchema;
                    }
                }
            }
        } catch (final SQLException e) {
            throw new SchemaParserException("Unable to describe schema [" + schema + "] table [" + table + "]", e);
        }

        return null;
    }

    @Nonnull
    private Set<String> listPrimaryKeys(@Nonnull Connection conn, @Nullable String schema, @Nonnull String tableName) throws SQLException {
        HashSet<String> primaryKeys = new HashSet<>();
        try (ResultSet rs = conn.getMetaData().getPrimaryKeys(null, schema, tableName)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String cat = rs.getString(CATALOG_COLUMN);
                if (StringUtils.isNotBlank(cat) && StringUtils.isNotBlank(schema)) {
                    //this db supports Catalogs.  Ensure the cat matches the supplied schema
                    if (schema.equalsIgnoreCase(cat)) {
                        primaryKeys.add(columnName);
                    }
                } else {
                    primaryKeys.add(columnName);
                }
            }
        } catch (SQLException e) {
            //attempt to use the catalog instead of the schema
            try (ResultSet rs = conn.getMetaData().getPrimaryKeys(schema, null, tableName)) {
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    primaryKeys.add(columnName);
                }
            } catch (SQLException e2) {
                log.info("Failed to list primary keys of {}.{}", schema, tableName);
            }
        }
        return primaryKeys;
    }

    @Nonnull
    private List<Field> listColumns(@Nonnull Connection conn, @Nullable String catalog, @Nullable String schema, @Nonnull String tableName, boolean schemaIsCat) throws SQLException {
        List<Field> fields;
        Set<String> pkSet = listPrimaryKeys(conn, (schema != null) ? schema : catalog, tableName);
        try (ResultSet columns = conn.getMetaData().getColumns(catalog, schema, tableName, null)) {
            fields = columnsResultSetToField(columns, pkSet, schema, schemaIsCat);
        }
        if (fields.isEmpty()) {
            //if empty try the schema as the catalog (for MySQL db)
            try (ResultSet columns = conn.getMetaData().getColumns(schema, null, tableName, null)) {
                fields = columnsResultSetToField(columns, pkSet, schema, schemaIsCat);
            }
        }
        return fields;
    }

    @Nonnull
    private List<Field> columnsResultSetToField(@Nullable ResultSet columns, @Nonnull Set<String> pkSet, @Nullable String schema, boolean schemaIsCat) throws SQLException {
        List<Field> fields = new ArrayList<>();
        if (columns != null) {
            while (columns.next()) {
                String cat = columns.getString(CATALOG_COLUMN);
                if (StringUtils.isNotBlank(cat) && StringUtils.isNotBlank(schema)) {
                    //this db supports Catalogs.  Ensure the cat matches the supplied schema
                    if (schemaIsCat && !schema.equalsIgnoreCase(cat)) {
                        continue;
                    }
                }

                DefaultField field = new DefaultField();
                field.setName(columns.getString("COLUMN_NAME"));
                Integer dataType = columns.getInt("DATA_TYPE");
                field.setNativeDataType(ParserHelper.toNativeType(dataType));
                field.setDerivedDataType(ParserHelper.sqlTypeToHiveType(dataType));
                field.setDescription(columns.getString("REMARKS"));
                String isNullableString = columns.getString("IS_NULLABLE");
                if ("NO".equals(isNullableString)) {
                    field.setNullable(false);
                }
                if (pkSet.contains(field.getName())) {
                    field.setPrimaryKey(true);
                }
                fields.add(field);
            }
        }
        return fields;

    }
}
