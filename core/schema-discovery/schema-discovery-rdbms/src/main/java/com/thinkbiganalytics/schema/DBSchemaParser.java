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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.jdbc.support.MetaDataAccessException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

public class DBSchemaParser {

    private static final Logger log = LoggerFactory.getLogger(DBSchemaParser.class);

    private DataSource ds;
    private KerberosTicketConfiguration kerberosTicketConfiguration;

    public DBSchemaParser(DataSource ds, KerberosTicketConfiguration kerberosTicketConfiguration) {
        this.ds = ds;
        this.kerberosTicketConfiguration = kerberosTicketConfiguration;
    }

    public List<String> listSchemas() {
        Vector<String> schemas = new Vector<>();
        try (final Connection conn = KerberosUtil.getConnectionWithOrWithoutKerberos(ds, kerberosTicketConfiguration)) {
        try (ResultSet rs = conn.getMetaData().getSchemas()) {
            while (rs.next()) {
                String schema = rs.getString("TABLE_SCHEM");
                schemas.add(schema);
            }
            return schemas;
        }
        } catch (SQLException e) {
            throw new SchemaParserException("Unable to list schemas", e);
        }
    }

    private List<String> listCatalogs() {
        Vector<String> catalogs = new Vector<>();
        try (final Connection conn = KerberosUtil.getConnectionWithOrWithoutKerberos(ds, kerberosTicketConfiguration)) {
        try ( ResultSet rs = conn.getMetaData().getCatalogs()) {
            while (rs.next()) {
                String cat = rs.getString("TABLE_CAT");
                catalogs.add(cat);
            }
            return catalogs;
        }
        } catch (SQLException e) {
            throw new SchemaParserException("Unable to list catalogs", e);
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

    private void addTableToList(final ResultSet result, final List<String> tables) throws SQLException {
        final String tableName = result.getString("TABLE_NAME");
        final String tableSchem = result.getString("TABLE_SCHEM");
        final String tableCat = result.getString("TABLE_CAT");
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
        }
        catch (Exception e) {
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


            try  {
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
                    List<String> schemas =listSchemas();
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

        String catalog = null;
        if (StringUtils.isNotBlank(schema)) {
            final Iterator<String> catalogIter = listCatalogs().iterator();
            while (catalog == null && catalogIter.hasNext()) {
                final String next = catalogIter.next();
                if (schema.equalsIgnoreCase(next)) {
                    catalog = next;
                }
            }
        }

        try (final Connection conn = KerberosUtil.getConnectionWithOrWithoutKerberos(ds, kerberosTicketConfiguration)) {
            try (final ResultSet result = getTables(conn, catalog, (catalog == null) ? schema : "%", table)) {
                while (result != null && result.next()) {
                    final String cat = result.getString(1);
                    final String schem = result.getString(2);
                    final String tableName = result.getString(3);
                    if (table.equalsIgnoreCase(tableName) && (schema == null || schem == null || schema.equalsIgnoreCase(schem))) {
                        final DefaultTableSchema tableSchema = new DefaultTableSchema();
                        tableSchema.setFields(listColumns(conn, schema, tableName));
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

    private Set<String> listPrimaryKeys(Connection conn, String schema, String tableName) throws SQLException {
        HashSet<String> primaryKeys = new HashSet<>();
        try (ResultSet rs = conn.getMetaData().getPrimaryKeys(null, schema, tableName)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                primaryKeys.add(columnName);
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

    private List<Field> listColumns(Connection conn, String schema, String tableName) throws SQLException {
        List<Field> fields;
        Set<String> pkSet = listPrimaryKeys(conn, schema, tableName);
        try (ResultSet columns = conn.getMetaData().getColumns(null, schema, tableName, null)) {
            fields = columnsResultSetToField(columns, pkSet);
        }
        if (fields.isEmpty()) {
            //if empty try the schema as the catalog (for MySQL db)
            try (ResultSet columns = conn.getMetaData().getColumns(schema, null, tableName, null)) {
                fields = columnsResultSetToField(columns, pkSet);
            }
        }
        return fields;
    }

    private List<Field> columnsResultSetToField(ResultSet columns, Set<String> pkSet) throws SQLException {
        List<Field> fields = new Vector<>();
        if (columns != null) {
            while (columns.next()) {
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
