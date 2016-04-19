/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.schema;

import com.thinkbiganalytics.db.model.schema.Field;
import com.thinkbiganalytics.db.model.schema.TableSchema;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.sql.DataSource;

public class DBSchemaParser {

    private DataSource ds;

    public DBSchemaParser(DataSource ds) {
        this.ds = ds;
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
            throw new RuntimeException("Unable to obtain list schemas", e);
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
            throw new RuntimeException("Unable to obtain list catalogs", e);
        }
    }

    private ResultSet getTables(Connection conn, String schema, String tableName) throws SQLException {
        return conn.getMetaData().getTables(schema, "%", tableName, new String[]{"TABLE", "VIEW"});
    }



    public List<String> listTables(String schema) {
        List<String> schemas = new ArrayList<>();
        if(StringUtils.isBlank(schema)){
            schemas = listSchemas();
            if(schemas == null || schemas.isEmpty()){
                schemas = listCatalogs();
            }
        }
        else {
            schemas.add(schema);
        }

        Vector<String> tables = new Vector<>();
        for(String tableSchema: schemas) {
            try (Connection conn = ds.getConnection()) {
                ResultSet result = getTables(conn, tableSchema, "%");
                while (result.next()) {
                    String tableName = result.getString("TABLE_NAME");
                    String tableSchem = result.getString("TABLE_SCHEM");
                    String tableCat = result.getString("TABLE_CAT");
                    String schem = tableSchem != null ? tableSchem : tableCat;
                    tableName = schem+"."+tableName;
                    tables.add(tableName);
                }

            } catch (SQLException e) {
                throw new RuntimeException("Unable to obtain list schemas", e);
            }
        }
        return tables;
    }


    public List<String> listCurrentTables(String schema) {

        Vector<String> tables = new Vector<>();
        try (Connection conn = ds.getConnection()) {
            ResultSet result = getTables(conn, schema, null);
            while (result.next()) {
                String tableName = result.getString("TABLE_NAME");
                tables.add(tableName);
            }
            return tables;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to obtain list schemas", e);
        }
    }


    public TableSchema describeTable(String schema, String table) {
        Validate.isTrue(!StringUtils.isEmpty(table), "Table expected");

        TableSchema tableSchema = null;
        try (Connection conn = ds.getConnection()) {

            ResultSet result = getTables(conn, schema, table);
            while (result.next()) {
                String tableName = result.getString(3);
                if (table.equalsIgnoreCase(tableName)) {
                    tableSchema = new TableSchema();
                    String catalog = result.getString(1);
                    String schem = result.getString(2);
                    tableSchema.setSchemaName(StringUtils.isBlank(schem)? catalog : schem);
                    tableSchema.setName(tableName);
                    tableSchema.setFields(listColumns(conn, schema, tableName));
                    return tableSchema;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to obtain list schemas", e);
        }
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
             }
             catch (SQLException e2) {

             }
        }
        return primaryKeys;
    }

    protected List<Field> listColumns(Connection conn, String schema, String tableName) throws SQLException {
        List<Field> fields = new Vector<>();
        Set<String> pkSet = listPrimaryKeys(conn, schema, tableName);
        ResultSet columns = conn.getMetaData().getColumns(null, schema, tableName, null);
        fields = columnsResultSetToField(columns,pkSet);
        if(fields.isEmpty()){
            //if empty try the schema as the catalog (for MySQL db)
            columns = conn.getMetaData().getColumns(schema, null, tableName, null);
            fields = columnsResultSetToField(columns,pkSet);
        }

        return fields;
    }

    private List<Field> columnsResultSetToField(ResultSet columns,  Set<String> pkSet ) throws SQLException {
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
