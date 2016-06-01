/*
 * Copyright (c) 2016. Teradata Inc.
 */
package com.thinkbiganalytics.ingest;

import com.thinkbiganalytics.util.ColumnSpec;
import com.thinkbiganalytics.util.TableType;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class TableRegisterSupport {

    public static Logger logger = LoggerFactory.getLogger(TableRegisterSupport.class);

    private Connection conn;

    public TableRegisterSupport(Connection conn) {
        Validate.notNull(conn);
        this.conn = conn;
    }

    protected TableRegisterSupport() {
    }


    public boolean registerDatabase(String source) {
        String ddl = "";
        boolean result = true;
        Validate.notNull(conn);
        try (final Statement st = conn.createStatement()) {
            ddl = createDatabaseDDL(source);
            st.execute(ddl);

        } catch (final SQLException e) {
            logger.error("Failed to create tables DDL {}", ddl, e);
            return false;
        }
        return result;
    }


    public boolean registerTable(String source, String tableEntity, String feedFormatOptions, String targetFormatOptions, ColumnSpec[] partitions, ColumnSpec[] columnSpecs, String
        targetTableProperties, TableType tableType) {
        Validate.notNull(conn);

        String ddl = createDDL(source, tableEntity, columnSpecs, partitions, feedFormatOptions, targetFormatOptions, targetTableProperties, tableType);
        return createTable(ddl);
    }

    protected boolean createTable(String ddl) {
        Validate.notNull(conn);

        try (final Statement st = conn.createStatement()) {
            st.execute(ddl);
            return true;
        } catch (final SQLException e) {
            logger.error("Failed to create tables DDL {}", ddl, e);
            return false;
        }
    }

    public Set<String> fetchExisting(String source, String tableEntity) {
        HashSet<String> tables = new HashSet<>();
        try (final Statement st = conn.createStatement()) {
            st.execute("use " + source);
            ResultSet rs = st.executeQuery("show tables like '" + tableEntity + "*'");
            while (rs.next()) {
                tables.add(rs.getString(1));
                logger.info("Found existing table " + rs.getString(1));
            }
            return tables;
        } catch (final SQLException e) {
            throw new RuntimeException("Failed to query tables", e);
        }
    }

    public boolean registerProfileTable(String source, String tableEntity) {

        String tableName = TableType.PROFILE.deriveQualifiedName(source, tableEntity);
        String columnSQL = " `columnname` string,`metrictype` string,`metricvalue` string";
        String formatSQL = TableType.PROFILE.deriveFormatSpecification(null, null);
        String partitionSQL = TableType.PROFILE.derivePartitionSpecification(null);
        String locationSQL = TableType.PROFILE.deriveLocationSpecification(source, tableEntity);

        String ddl = createDDL(tableName, columnSQL, partitionSQL, formatSQL, locationSQL, "");
        return createTable(ddl);
    }

    public boolean registerStandardTables(String source, String tableEntity, String feedFormatOptions, String targetFormatOptions, ColumnSpec[] partitions, ColumnSpec[] columnSpecs, String tblProperties) {
        boolean result = true;
        registerDatabase(source);
        Set<String> existingTables = fetchExisting(source, tableEntity);
        TableType[] tableTypes = new TableType[]{TableType.FEED, TableType.INVALID, TableType.VALID, TableType.MASTER};
        for (TableType tableType : tableTypes) {
            if (!existingTables.contains(tableType.deriveTablename(tableEntity))) {
                result = registerTable(source, tableEntity, feedFormatOptions, targetFormatOptions, partitions, columnSpecs, tblProperties, tableType) && result;
            }
        }
        if (!existingTables.contains(TableType.PROFILE.deriveTablename(tableEntity))) {
            result = registerProfileTable(source, tableEntity) && result;
        }
        return result;
    }

    protected String createDatabaseDDL(String source) {
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE DATABASE IF NOT EXISTS `").append(source).append("`");
        return sb.toString();
    }

    protected String createDDL(String source, String entity, ColumnSpec[] columnSpecs, ColumnSpec[] partitions, String feedFormatOptions, String targetFormatOptions, String targetTableProperties,
                               TableType tableType) {
        String tableName = tableType.deriveQualifiedName(source, entity);
        String partitionSQL = tableType.derivePartitionSpecification(partitions);
        String columnsSQL = tableType.deriveColumnSpecification(columnSpecs, partitions);
        String locationSQL = tableType.deriveLocationSpecification(source, entity);
        String formatOptionsSQL = tableType.deriveFormatSpecification(feedFormatOptions, targetFormatOptions);
        String tblPropertiesSQL = tableType.deriveTableProperties(targetTableProperties);

        return createDDL(tableName, columnsSQL, partitionSQL, formatOptionsSQL, locationSQL, tblPropertiesSQL);
    }

    protected String createDDL(String tableName, String columnsSQL, String partitionSQL, String formatOptionsSQL, String locationSQL, String targetTablePropertiesSQL) {
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE EXTERNAL ");
        sb.append("TABLE IF NOT EXISTS `")
            .append(tableName.trim())
            .append("`").append(" (").append(columnsSQL).append(") ");

        if (!StringUtils.isEmpty(partitionSQL)) {
            sb.append(" ").append(partitionSQL);
        }
        if (!StringUtils.isEmpty(formatOptionsSQL)) {
            sb.append(" ").append(formatOptionsSQL);
        }
        sb.append(locationSQL);
        if (!StringUtils.isEmpty(targetTablePropertiesSQL)) {
            sb.append(" ").append(targetTablePropertiesSQL);
        }
        return sb.toString();
    }
}
