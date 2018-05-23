package com.thinkbiganalytics.ingest;

/*-
 * #%L
 * kylo-nifi-core-processors
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

import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.util.ColumnSpec;
import com.thinkbiganalytics.util.TableRegisterConfiguration;
import com.thinkbiganalytics.util.TableType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generates and execute SQL queries for creating tables.
 */
public class TableRegisterSupport {

    private static final Logger logger = LoggerFactory.getLogger(TableRegisterSupport.class);

    private Connection conn;

    private TableRegisterConfiguration config;

    public TableRegisterSupport(Connection conn, TableRegisterConfiguration configuration) {
        Validate.notNull(conn, "connection required");
        Validate.notNull(configuration, "configuration required");
        this.conn = conn;
        this.config = configuration;
    }

    public TableRegisterSupport(Connection conn) {
        this(conn, new TableRegisterConfiguration());
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
            result = false;
        }
        return result;
    }

    /**
     * copy the columnSpecs and reset the datatypes to match that of the feed column specs
     *
     * @param feedColumnSpecs
     * @param columnSpecs
     * @return
     */
    protected ColumnSpec[] adjustInvalidColumnSpec(ColumnSpec[] feedColumnSpecs, ColumnSpec[] columnSpecs) {
        //find the source data types from the _feed table that match these columns and replace the data types
        Map<String, ColumnSpec> feedColumnSpecMap = Arrays.asList(feedColumnSpecs).stream().collect(Collectors.toMap(ColumnSpec::getName, Function.identity()));
        List<ColumnSpec> invalidColumnSpecs = Arrays.asList(columnSpecs).stream().map(c -> {
            ColumnSpec copy = new ColumnSpec(c);
            if (StringUtils.isNotBlank(copy.getOtherColumnName()) && feedColumnSpecMap.containsKey(copy.getOtherColumnName())) {
                ColumnSpec feedSpec = feedColumnSpecMap.get(copy.getOtherColumnName());
                copy.setDataType(feedSpec.getDataType());
            }
            return copy;
        }).collect(Collectors.toList());
        return invalidColumnSpecs.toArray(new ColumnSpec[invalidColumnSpecs.size()]);
    }


    /**
     * Registers the specified table by generating and executing a {@code CREATE TABLE} query.
     *
     * @param source                the name of the database
     * @param tableEntity           the name of the table
     * @param feedColumnSpecs       the column specification for the feed table
     * @param feedFormatOptions     the format for the feed table
     * @param targetFormatOptions   the format for the target table
     * @param partitions            the partitions for the target table
     * @param columnSpecs           the columns for the table
     * @param feedTableProperties       the properties for the table (feed or target only)
     * @param tableType             the type of table
     * @param registerDatabase      {@code true} to create the database if it does not exist, or {@code false} to require an existing database
     **/
    public boolean registerTable(String source, String tableEntity, ColumnSpec[] feedColumnSpecs, String feedFormatOptions, String targetFormatOptions, ColumnSpec[] partitions, ColumnSpec[]
        columnSpecs, String feedTableProperties, String targetTableProperties, TableType tableType, boolean registerDatabase) {
        return registerTable(source, tableEntity, feedColumnSpecs, feedFormatOptions, targetFormatOptions, partitions,columnSpecs, feedTableProperties, targetTableProperties,
                             tableType, registerDatabase, null);
    }

    /**
     * Registers the specified table by generating and executing a {@code CREATE TABLE} query.
     *
     * @param source                the name of the database
     * @param tableEntity           the name of the table
     * @param feedColumnSpecs       the column specification for the feed table
     * @param feedFormatOptions     the format for the feed table
     * @param targetFormatOptions   the format for the target table
     * @param partitions            the partitions for the target table
     * @param columnSpecs           the columns for the table
     * @param feedTableProperties       the properties for the table (feed or target only)
     * @param tableType             the type of table
     * @param registerDatabase      {@code true} to create the database if it does not exist, or {@code false} to require an existing database
     * @param feedTableOverride     utilizes the provided ddl for the feed table
     * @return {@code true} if the table was registered, or {@code false} if there was an error
     */
    public boolean registerTable(String source, String tableEntity, ColumnSpec[] feedColumnSpecs, String feedFormatOptions, String targetFormatOptions, ColumnSpec[] partitions, ColumnSpec[]
            columnSpecs, String feedTableProperties, String targetTableProperties, TableType tableType, boolean registerDatabase, String feedTableOverride) {
        Validate.notNull(conn);

        // Register the database
        if (registerDatabase && !registerDatabase(source)) {
            return false;
        }

        // Use override
        if (tableType.equals(TableType.FEED) && !StringUtils.isEmpty(feedTableOverride)) {
            return createTable(feedTableOverride);
        }

        //_invalid and _feed tables should use the schema provided from the Source 'feedColumnSpecs'.
        //_valid and the final feed table should use the target schema
        ColumnSpec[] useColumnSpecs = ((tableType == TableType.FEED) ? feedColumnSpecs : columnSpecs);

        //if invalid use the feed column specs and update the data types on the _invalid table
        if (tableType == TableType.INVALID) {
            useColumnSpecs = adjustInvalidColumnSpec(feedColumnSpecs, columnSpecs);
        }

        String tableProperties = tablePropertiesForType(tableType, feedTableProperties, targetTableProperties);

        // Register the table
        String ddl = createDDL(source, tableEntity, useColumnSpecs, partitions, feedFormatOptions, targetFormatOptions, tableProperties, tableType);
        return createTable(ddl);
    }

    protected boolean createTable(String ddl) {
        Validate.notNull(conn);

        try (final Statement st = conn.createStatement()) {
            logger.info("Executing {}", ddl);
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
            st.execute("use " + HiveUtils.quoteIdentifier(source));
            ResultSet rs = st.executeQuery("show tables like '" + tableEntity + "*'");
            while (rs.next()) {
                tables.add(rs.getString(1));
                logger.info("Found existing table " + rs.getString(1));
            }
        } catch (final SQLException e) {
            throw new RuntimeException("Failed to query tables", e);
        }
        return tables;
    }

    /**
     * Derive the STORED AS clause for the table
     *
     * @param rawSpecification    the clause for the raw specification
     * @param targetSpecification the target specification
     */
    public String formatOptionsForType(TableType tableType, String rawSpecification, String targetSpecification) {

        switch (tableType) {
            case FEED:
                return rawSpecification;
            default:
                return targetSpecification;
        }
    }

    private String tablePropertiesForType(TableType tableType, String feedTableProperties, String targetTableProperties) {
        String tblProperties = "";
        switch (tableType) {
            case FEED:
                tblProperties = feedTableProperties;
                break;
            default:
                tblProperties = tableType.deriveTableProperties(targetTableProperties);
                break;
        }
        return tblProperties;
    }

    public boolean registerStandardTables(String source, String tableEntity, ColumnSpec[] feedColumnSpecs, String feedFormatOptions, String targetFormatOptions, ColumnSpec[] partitions, ColumnSpec[]
        columnSpecs, String feedTblProperties, String targetTblProperties, String feedTblOverride) {
        boolean result = true;
        registerDatabase(source);
        Set<String> existingTables = fetchExisting(source, tableEntity);
        for (TableType tableType : TableType.values()) {
            if (!existingTables.contains(tableType.deriveTablename(tableEntity))) {
                result = registerTable(source, tableEntity, feedColumnSpecs, feedFormatOptions, targetFormatOptions, partitions, columnSpecs, feedTblProperties, targetTblProperties, tableType,
                                       false, feedTblOverride) && result;
            }
        }
        return result;
    }

    public boolean registerStandardTables(String source, String tableEntity, ColumnSpec[] feedColumnSpecs, String feedFormatOptions, String targetFormatOptions, ColumnSpec[] partitions, ColumnSpec[]
            columnSpecs, String feedTblProperties, String targetTblProperties) {
        return registerStandardTables(source, tableEntity, feedColumnSpecs, feedFormatOptions, targetFormatOptions, partitions, columnSpecs, feedTblProperties, targetTblProperties, null);
    }

    /**
     * Returns the Hive query for creating the specified database.
     *
     * @param source the database name
     * @return the Hive query
     */
    protected String createDatabaseDDL(@Nonnull final String source) {
        return "CREATE DATABASE IF NOT EXISTS " + HiveUtils.quoteIdentifier(source);
    }

    protected String createDDL(String source, String entity, ColumnSpec[] columnSpecs, ColumnSpec[] partitions, String feedFormatOptions,String targetFormatOptions,
                               String tableProperties,
                               TableType tableType) {

        String tableName;
        String columnsSQL;
        String partitionSQL;
        String locationSQL;
        String tblPropertiesSQL;
        String formatOptionsSQL = formatOptionsForType(tableType, feedFormatOptions, targetFormatOptions);

        switch (tableType) {
            case PROFILE:
                tableName = TableType.PROFILE.deriveQualifiedName(source, entity);
                columnsSQL = " `columnname` string,`metrictype` string,`metricvalue` string";
                partitionSQL = TableType.PROFILE.derivePartitionSpecification(null);
                locationSQL = TableType.PROFILE.deriveLocationSpecification(config.pathForTableType(TableType.PROFILE), source, entity);
                tblPropertiesSQL = "";
                break;
            default:
                tableName = tableType.deriveQualifiedName(source, entity);
                partitionSQL = tableType.derivePartitionSpecification(partitions);
                columnsSQL = tableType.deriveColumnSpecification(columnSpecs, partitions, feedFormatOptions);
                locationSQL = tableType.deriveLocationSpecification(config.pathForTableType(tableType), source, entity);
                tblPropertiesSQL = tableType.deriveTableProperties(tableProperties);
                break;
        }

        return createDDL(tableName, columnsSQL, partitionSQL, formatOptionsSQL, locationSQL, tblPropertiesSQL, tableType.isExternal());
    }

    protected String createDDL(String tableName, String columnsSQL, String partitionSQL, String formatOptionsSQL, String locationSQL,  String tablePropertiesSQL,
                               boolean external) {
        StringBuilder sb = new StringBuilder();
        String externalString = (external ? " EXTERNAL " : " ");
        sb.append("CREATE").append(externalString).append("TABLE IF NOT EXISTS ")
                .append(tableName)
                .append(" (").append(columnsSQL).append(") ");

        if (!StringUtils.isEmpty(partitionSQL)) {
            sb.append(" ").append(partitionSQL);
        }
        if (!StringUtils.isEmpty(formatOptionsSQL)) {
            sb.append(" ").append(formatOptionsSQL);
        }
        sb.append(locationSQL);
        if (!StringUtils.isEmpty(tablePropertiesSQL)) {
            sb.append(" ").append(tablePropertiesSQL);
        }
        return sb.toString();
    }

    /**
     * Drops the specified Hive table.
     * <p>
     * <p>The identifier is expected to already be quoted, if necessary.</p>
     *
     * @param identifier the identifier for the table
     * @return {@code true} on success or {@code false} on failure
     */
    public boolean dropTable(@Nonnull final String identifier) {
        Validate.notNull(conn);

        final String sql = "DROP TABLE IF EXISTS " + identifier;

        try (final Statement st = conn.createStatement()) {
            st.execute(sql);
            return true;
        } catch (final SQLException e) {
            logger.error("Failed to drop tables SQL {}", sql, e);
            return false;
        }
    }

    /**
     * Drops the specified Hive tables.
     *
     * @param source           the category system name or the database name
     * @param entity           the feed system name or the table prefix
     * @param tableTypes       the standard table types to drop
     * @param additionalTables the identifiers of additional tables to drop
     * @return {@code true} on success or {@code false} on failure
     */
    public boolean dropTables(@Nonnull final String source, @Nonnull final String entity, @Nonnull final Set<TableType> tableTypes, @Nonnull final Set<String> additionalTables) {
        // Drop standard tables
        for (final TableType tableType : tableTypes) {
            final String identifier = tableType.deriveQualifiedName(source, entity);
            if (!dropTable(identifier)) {
                return false;
            }
        }

        // Drop additional tables
        for (final String identifier : additionalTables) {
            if (!dropTable(identifier)) {
                return false;
            }
        }

        // Return success
        return true;
    }
}
