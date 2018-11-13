package com.thinkbiganalytics.hive.service;

/*-
 * #%L
 * thinkbig-thrift-proxy-core
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


import com.thinkbiganalytics.discovery.model.DefaultDatabaseMetadata;
import com.thinkbiganalytics.discovery.model.DefaultField;
import com.thinkbiganalytics.discovery.model.DefaultTableSchema;
import com.thinkbiganalytics.discovery.schema.DatabaseMetadata;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.JdbcCatalog;
import com.thinkbiganalytics.discovery.schema.JdbcSchema;
import com.thinkbiganalytics.discovery.schema.JdbcTable;
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.jdbc.util.DatabaseType;
import com.thinkbiganalytics.schema.DefaultJdbcSchema;
import com.thinkbiganalytics.schema.DefaultJdbcTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 */
@Service("hiveMetastoreService")
public class HiveMetastoreService {

    private static final Logger log = LoggerFactory.getLogger(HiveMetastoreService.class);

    private final JdbcTemplate hiveMetatoreJdbcTemplate;
    private final HiveService hiveService;
    private DatabaseType metastoreDatabaseType = null;
    private boolean userImpersonationEnabled;

    @Autowired
    public HiveMetastoreService(@Qualifier("hiveMetatoreJdbcTemplate") final JdbcTemplate hiveMetatoreJdbcTemplate, final HiveService hiveService) {
        this.hiveMetatoreJdbcTemplate = hiveMetatoreJdbcTemplate;
        this.hiveService = hiveService;
    }

    @Value("${hive.userImpersonation.enabled:#{false}}")
    public void setUserImpersonationEnabled(final boolean userImpersonationEnabled) {
        this.userImpersonationEnabled = userImpersonationEnabled;
    }

    public DataSource getDataSource() {

        return hiveMetatoreJdbcTemplate.getDataSource();
    }

    private DatabaseType getMetastoreDatabaseType() {
        if (metastoreDatabaseType == null) {
            try {
                metastoreDatabaseType = DatabaseType.fromMetaData(getDataSource());
                return metastoreDatabaseType;
            } catch (MetaDataAccessException e) {
                log.error("Unable to determine Metastore Database Type.  Using default type of " + DatabaseType.MYSQL + ". " + e.getMessage(), e);
            }
            return DatabaseType.MYSQL;
        }
        return metastoreDatabaseType;
    }

    @Nonnull
    public List<JdbcCatalog> listCatalogs(@Nullable final String pattern, @Nullable final Pageable pageable) {
        return Collections.emptyList();
    }

    @Nonnull
    public List<JdbcSchema> listSchemas(@Nullable final String catalog, @Nullable final String pattern, @Nullable final Pageable pageable) {
        return hiveService.getSchemaNames().stream()
            .map(DefaultJdbcSchema::new)
            .collect(Collectors.toList());
    }

    @Nonnull
    public List<JdbcTable> listTables(@Nullable final String catalog, @Nullable final String schema, @Nullable final String pattern, @Nullable final Pageable pageable) {
        String query = "SELECT d.NAME as \"DATABASE_NAME\", t.TBL_NAME, t.TBL_TYPE, p.PARAM_VALUE"
                       + " FROM TBLS t"
                       + " JOIN DBS d on d.DB_ID = t.DB_ID"
                       + " LEFT JOIN TABLE_PARAMS p on p.TBL_ID = t.TBL_ID AND p.PARAM_KEY = \"comment\""
                       + " WHERE d.NAME LIKE ? "
                       + " AND t.TBL_NAME LIKE ? "
                       + " ORDER BY \"DATABASE_NAME\", t.TBL_NAME";
        if (DatabaseType.POSTGRES.equals(getMetastoreDatabaseType())) {
            query = "SELECT d.\"NAME\" as \"DATABASE_NAME\", t.\"TBL_NAME\", t.\"TBL_TYPE\", p.\"PARAM_VALUE\""
                    + " FROM \"TBLS\" t"
                    + " JOIN \"DBS\" d on d.\"DB_ID\" = t.\"DB_ID\" "
                    + " LEFT JOIN \"TABLE_PARAMS\" p ON p.\"TBL_ID\" = t.\"TBL_ID\" and p.\"PARAM_KEY\" = 'comment'"
                    + " WHERE d.\"NAME\" LIKE ? "
                    + " AND t.\"TBL_NAME\" LIKE ? "
                    + " ORDER BY d.\"NAME\", t.\"TBL_NAME\"";
        }

        final List<JdbcTable> tables = hiveMetatoreJdbcTemplate.query(
            query,
            ps -> {
                ps.setString(1, schema == null ? "%" : schema);
                ps.setString(2, pattern == null ? "%" : "%" + pattern + "%");
            },
            (rs, i) -> {
                final DefaultJdbcTable jdbcTable = new DefaultJdbcTable(rs.getString("TBL_NAME"), rs.getString("TBL_TYPE"));
                jdbcTable.setIdentifierQuoteString("`");
                jdbcTable.setRemarks(rs.getString("PARAM_VALUE"));
                jdbcTable.setSchema(rs.getString("DATABASE_NAME"));
                return jdbcTable;
            });

        if (userImpersonationEnabled) {
            return tables.stream()
                .filter(jdbcTable -> hiveService.isTableAccessibleByImpersonatedUser(jdbcTable.getSchema() + "." + jdbcTable.getName()))
                .collect(Collectors.toList());
        } else {
            return tables;
        }
    }

    public List<DatabaseMetadata> getTableColumns(List<String> tablesFilter) throws DataAccessException {

        String query = "SELECT d.NAME as \"DATABASE_NAME\", t.TBL_NAME, c.COLUMN_NAME "
                       + "FROM COLUMNS_V2 c "
                       + "JOIN  SDS s on s.CD_ID = c.CD_ID "
                       + "JOIN  TBLS t ON s.SD_ID = t.SD_ID "
                       + "JOIN  DBS d on d.DB_ID = t.DB_ID "
                       + "ORDER BY d.NAME, t.TBL_NAME";

        if (DatabaseType.POSTGRES.equals(getMetastoreDatabaseType())) {
            query = "SELECT d.\"NAME\" as \"DATABASE_NAME\", t.\"TBL_NAME\", c.\"COLUMN_NAME\" "
                    + "FROM \"COLUMNS_V2\" c "
                    + "JOIN  \"SDS\" s on s.\"CD_ID\" = c.\"CD_ID\" "
                    + "JOIN  \"TBLS\" t ON s.\"SD_ID\" = t.\"SD_ID\" "
                    + "JOIN  \"DBS\" d on d.\"DB_ID\" = t.\"DB_ID\" "
                    + "ORDER BY d.\"NAME\", t.\"TBL_NAME\"";
        }

        List<DatabaseMetadata> metadata = hiveMetatoreJdbcTemplate.query(query, new RowMapper<DatabaseMetadata>() {
            @Override
            public DatabaseMetadata mapRow(ResultSet rs, int i) throws SQLException {
                DefaultDatabaseMetadata row = new DefaultDatabaseMetadata();
                row.setDatabaseName(rs.getString("DATABASE_NAME"));
                row.setColumnName(rs.getString("COLUMN_NAME"));
                row.setTableName(rs.getString("TBL_NAME"));
                return row;
            }
        });

        return tablesFilter == null ? metadata : filterDatabaseMetadata(metadata, tablesFilter);

    }

    private List<DatabaseMetadata> filterDatabaseMetadata(List<DatabaseMetadata> allTables, List<String> tablesFilter) {
        List<DatabaseMetadata> results = new ArrayList<>();
        allTables.forEach(metadata -> {
            if (tablesFilter.contains(metadata.getDatabaseName() + "." + metadata.getTableName())) {
                results.add(metadata);
            }
        });
        return results;
    }

    /**
     * Finds all tables matching the specified criteria.
     *
     * @param schema schema name exactly as it appears in Hive
     * @param table  search string for table names
     * @return fully-qualified table identifiers
     */
    @Nonnull
    public List<String> getAllTables(@Nullable final String schema, @Nullable final String table) {
        return listTables(null, schema, table, null).stream()
            .map(jdbcTable -> jdbcTable.getSchema() + "." + jdbcTable.getName())
            .collect(Collectors.toList());
    }

    public List<TableSchema> getTableSchemas() throws DataAccessException {

        String query = "SELECT d.NAME as \"DATABASE_NAME\", t.TBL_NAME, c.COLUMN_NAME, c.TYPE_NAME "
                       + "FROM COLUMNS_V2 c "
                       + "JOIN  SDS s on s.CD_ID = c.CD_ID "
                       + "JOIN  TBLS t ON s.SD_ID = t.SD_ID "
                       + "JOIN  DBS d on d.DB_ID = t.DB_ID "
                       + "ORDER BY d.NAME, t.TBL_NAME";
        if (DatabaseType.POSTGRES.equals(getMetastoreDatabaseType())) {
            query = "SELECT d.\"NAME\" as \"DATABASE_NAME\", t.\"TBL_NAME\", c.\"COLUMN_NAME\",c.\"TYPE_NAME\" "
                    + "FROM \"COLUMNS_V2\" c "
                    + "JOIN  \"SDS\" s on s.\"CD_ID\" = c.\"CD_ID\" "
                    + "JOIN  \"TBLS\" t ON s.\"SD_ID\" = t.\"SD_ID\" "
                    + "JOIN  \"DBS\" d on d.\"DB_ID\" = t.\"DB_ID\" ";


        }
        final List<TableSchema> metadata = new ArrayList<>();
        final Map<String, Map<String, TableSchema>> databaseTables = new HashMap<>();

        hiveMetatoreJdbcTemplate.query(query, new RowMapper<Object>() {
            @Override
            public TableSchema mapRow(ResultSet rs, int i) throws SQLException {
                String dbName = rs.getString("DATABASE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                String tableName = rs.getString("TBL_NAME");
                String columnType = rs.getString("TYPE_NAME");
                if (!databaseTables.containsKey(dbName)) {
                    databaseTables.put(dbName, new HashMap<String, TableSchema>());
                }
                Map<String, TableSchema> tables = databaseTables.get(dbName);
                if (!tables.containsKey(tableName)) {
                    DefaultTableSchema schema = new DefaultTableSchema();
                    schema.setName(tableName);
                    schema.setSchemaName(dbName);
                    schema.setFields(new ArrayList<Field>());
                    tables.put(tableName, schema);
                    metadata.add(schema);
                }
                TableSchema schema = tables.get(tableName);
                DefaultField field = new DefaultField();
                field.setName(columnName);
                field.setNativeDataType(columnType);
                field.setDerivedDataType(columnType);
                schema.getFields().add(field);
                return schema;
            }
        });

        return metadata;

    }

    public TableSchema getTable(String schema, String table) throws DataAccessException {

        // Must use JDBC metadata for user impersonation
        if (userImpersonationEnabled) {
            return hiveService.getTableSchema(schema, table);
        }

        String query = "SELECT d.NAME as \"DATABASE_NAME\", t.TBL_NAME, c.COLUMN_NAME, c.TYPE_NAME "
                       + "FROM COLUMNS_V2 c "
                       + "JOIN  SDS s on s.CD_ID = c.CD_ID "
                       + "JOIN  TBLS t ON s.SD_ID = t.SD_ID "
                       + "JOIN  DBS d on d.DB_ID = t.DB_ID "
                       + "WHERE t.TBL_NAME='" + table + "' and d.NAME='" + schema + "' "
                       + "ORDER BY d.NAME, t.TBL_NAME, c.INTEGER_IDX";
        if (DatabaseType.POSTGRES.equals(getMetastoreDatabaseType())) {
            query = "SELECT d.\"NAME\" as \"DATABASE_NAME\", t.\"TBL_NAME\", c.\"COLUMN_NAME\",c.\"TYPE_NAME\" "
                    + "FROM \"COLUMNS_V2\" c "
                    + "JOIN  \"SDS\" s on s.\"CD_ID\" = c.\"CD_ID\" "
                    + "JOIN  \"TBLS\" t ON s.\"SD_ID\" = t.\"SD_ID\" "
                    + "JOIN  \"DBS\" d on d.\"DB_ID\" = t.\"DB_ID\" "
                    + "WHERE t.\"TBL_NAME\"='" + table + "' and d.\"NAME\"='" + schema + "' "
                    + "ORDER BY d.\"NAME\", t.\"TBL_NAME\", c.\"INTEGER_IDX\"";


        }
        final DefaultTableSchema metadata = new DefaultTableSchema();

        hiveMetatoreJdbcTemplate.query(query, new RowMapper<Object>() {
            @Override
            public TableSchema mapRow(ResultSet rs, int i) throws SQLException {
                String dbName = rs.getString("DATABASE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                String tableName = rs.getString("TBL_NAME");
                String columnType = rs.getString("TYPE_NAME");

                metadata.setName(tableName);
                metadata.setSchemaName(dbName);

                DefaultField field = new DefaultField();
                field.setName(columnName);
                field.setNativeDataType(columnType);
                field.setDerivedDataType(columnType);
                metadata.getFields().add(field);
                return metadata;
            }
        });

        return metadata;

    }

}
