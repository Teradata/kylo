/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.schema;

import com.thinkbiganalytics.db.model.schema.Field;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.kerberos.KerberosUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
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
        try (Connection conn = ds.getConnection()) {
            ResultSet rs = conn.getMetaData().getSchemas();
            while (rs.next()) {
                String schema = rs.getString("TABLE_SCHEM");
                schemas.add(schema);
            }
            return schemas;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to list schemas", e);
        }
    }

    public List<String> listCatalogs() {
        Vector<String> catalogs = new Vector<>();
        try (Connection conn = ds.getConnection()) {
            ResultSet rs = conn.getMetaData().getCatalogs();
            while (rs.next()) {
                String cat = rs.getString("TABLE_CAT");
                catalogs.add(cat);
            }
            return catalogs;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to list catalogs", e);
        }
    }

    /**
     * Lists the tables in the specified schema that match the specified table pattern.
     *
     * @param conn the JDBC connection
     * @param catalog the catalog name pattern, or {@code null}
     * @param schema the schema name pattern, or {@code null}
     * @param tableName the table name pattern   @return a result set containing the matching table metadata
     * @throws SQLException if a database access error occurs
     */
    @Nonnull
    private ResultSet getTables(@Nonnull final Connection conn, @Nullable String catalog, @Nullable final String schema, @Nonnull final String tableName) throws SQLException {
        return conn.getMetaData().getTables(catalog, schema, tableName, new String[]{"TABLE", "VIEW"});
    }

    /**
     * Lists the tables in the specified schema.
     *
     * @param schema the schema name, or {@code null}
     * @return the list of table names prepended with the schema name, like: {@code <schema>.<table>}
     * @throws RuntimeException if a database access error occurs
     */
    @Nonnull
    public List<String> listTables(@Nullable final String schema) {
        final String schemaPattern = (schema != null) ? schema : "%";
        final List<String> tables = new ArrayList<>();

        try (final Connection conn = ds.getConnection()) {
            for (final String catalog : listCatalogs()) {
                try (final ResultSet result = getTables(conn, catalog, "%", "%")) {
                    while (result.next()) {
                        final String tableName = result.getString("TABLE_NAME");
                        final String tableSchem = result.getString("TABLE_SCHEM");
                        final String tableCat = result.getString("TABLE_CAT");
                        tables.add((tableSchem != null ? tableSchem : tableCat) + "." + tableName);
                    }
                }
            }
        } catch (final SQLException e) {
            throw new RuntimeException("Unable to obtain table list", e);
        }

        return tables;
    }

    /**
     * Gets the schema for the specified table.
     *
     * @param schema the schema name
     * @param table the table name
     * @return the table schema
     * @throws IllegalArgumentException if the table name is empty
     * @throws RuntimeException if a database access error occurs
     */
    @Nullable
    public TableSchema describeTable(@Nullable final String schema, @Nonnull final String table) {
        Validate.isTrue(!StringUtils.isEmpty(table), "Table expected");

        final String catalog = StringUtils.isNotBlank(schema) ? listCatalogs().stream().filter(schema::equalsIgnoreCase).findFirst().orElse(null) : null;

        try (final Connection conn = kerberosTicketConfiguration.isKerberosEnabled() ? KerberosUtil.getConnectionWithOrWithoutKerberos(ds, kerberosTicketConfiguration) : ds.getConnection()) {
            try (final ResultSet result = getTables(conn, catalog, (catalog == null) ? schema : "%", table)) {
                while (result.next()) {
                    final String cat = result.getString(1);
                    final String schem = result.getString(2);
                    final String tableName = result.getString(3);
                    if (table.equalsIgnoreCase(tableName) && (schema == null || schem == null || schema.equalsIgnoreCase(schem))) {
                        final TableSchema tableSchema = new TableSchema();
                        tableSchema.setFields(listColumns(conn, schema, tableName));
                        tableSchema.setName(tableName);
                        tableSchema.setSchemaName(StringUtils.isBlank(schem) ? cat : schem);
                        return tableSchema;
                    }
                }
            }
        } catch (final SQLException e) {
            throw new RuntimeException("Unable to describe schema [" + schema + "] table [" + table + "]", e);
        }

        return null;
    }

    protected Set<String> listPrimaryKeys(Connection conn, String schema, String tableName) throws SQLException {
        HashSet<String> primaryKeys = new HashSet<>();
        try {
            ResultSet rs = conn.getMetaData().getPrimaryKeys(null, schema, tableName);
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                primaryKeys.add(columnName);
            }
        } catch (SQLException e) {
            //   e.printStackTrace();
            //attempt to use the catalog instead of the schema
            try {
                ResultSet rs = conn.getMetaData().getPrimaryKeys(schema, null, tableName);
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    primaryKeys.add(columnName);
                }
            } catch (SQLException e2) {

            }
        }
        return primaryKeys;
    }

    protected List<Field> listColumns(Connection conn, String schema, String tableName) throws SQLException {
        List<Field> fields = new Vector<>();
        Set<String> pkSet = listPrimaryKeys(conn, schema, tableName);
        ResultSet columns = conn.getMetaData().getColumns(null, schema, tableName, null);
        fields = columnsResultSetToField(columns, pkSet);
        if (fields.isEmpty()) {
            //if empty try the schema as the catalog (for MySQL db)
            columns = conn.getMetaData().getColumns(schema, null, tableName, null);
            fields = columnsResultSetToField(columns, pkSet);
        }

        return fields;
    }

    private List<Field> columnsResultSetToField(ResultSet columns, Set<String> pkSet) throws SQLException {
        List<Field> fields = new Vector<>();
        if (columns != null) {
            while (columns.next()) {
                Field field = new Field();
                field.setName(columns.getString("COLUMN_NAME"));
                Integer dataType = columns.getInt("DATA_TYPE");
                field.setDataType(Field.sqlTypeToDataType(dataType));
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
