/*
 * Copyright (c) 2016. Teradata Inc.
 */
package com.thinkbiganalytics.components;

import com.thinkbiganalytics.util.ColumnSpec;
import com.thinkbiganalytics.util.TableType;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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


    public boolean registerTable(String source, String tableEntity, String formatOptions, ColumnSpec[] partitions, ColumnSpec[] columnSpecs, TableType tableType) {
        String ddl = "";
        boolean result = true;
        Validate.notNull(conn);
        try (final Statement st = conn.createStatement()) {

            ddl = createDDL(source, tableEntity, columnSpecs, partitions, formatOptions, tableType);
            st.execute(ddl);

        } catch (final SQLException e) {
            logger.error("Failed to create tables DDL {}", ddl, e);
            return false;
        }
        return result;
    }

    public boolean registerStandardTables(String source, String tableEntity, String formatOptions, ColumnSpec[] partitions, ColumnSpec[] columnSpecs) {
        String ddl = "";
        boolean result = true;
        registerDatabase(source);
        TableType[] tableTypes = new TableType[]{TableType.FEED, TableType.INVALID, TableType.VALID, TableType.MASTER};
        for (TableType tableType : tableTypes) {
            result = registerTable(source, tableEntity, formatOptions, partitions, columnSpecs, tableType);
            if (!result) break;
        }
        return result;
    }

    protected String createDatabaseDDL(String source) {
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE DATABASE IF NOT EXISTS `").append(source).append("`");
        return sb.toString();
    }

    protected String createDDL(String source, String entity, ColumnSpec[] columnSpecs, ColumnSpec[] partitions, String formatOptions, TableType tableType) {
        String tableName = tableType.deriveQualifiedName(source, entity);
        String partitionSQL = tableType.derivePartitionSpecification(partitions);
        String columnsSQL = tableType.deriveColumnSpecification(columnSpecs, partitions);
        String locationSQL = tableType.deriveLocationSpecification(source, entity);
        String formatOptionsSQL = tableType.deriveFormatSpecification(formatOptions);

        StringBuffer sb = new StringBuffer();
        sb.append("CREATE EXTERNAL ");
        sb.append("TABLE IF NOT EXISTS `")
                .append(tableName)
                .append("`").append("(").append(columnsSQL).append(") ");

        if (!StringUtils.isEmpty(partitionSQL)) {
            sb.append(partitionSQL);
        }
        if (!StringUtils.isEmpty(formatOptionsSQL)) {
            sb.append(formatOptionsSQL);
        }
        sb.append(locationSQL);
        return sb.toString();
    }
}
