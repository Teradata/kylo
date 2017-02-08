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
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.jdbc.util.DatabaseType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 */
@Service("hiveMetastoreService")
public class HiveMetastoreService {

    private static final Logger log = LoggerFactory.getLogger(HiveMetastoreService.class);


    @Inject
    @Qualifier("hiveMetatoreJdbcTemplate")
    private JdbcTemplate hiveMetatoreJdbcTemplate;
    private DatabaseType metastoreDatabaseType = null;

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

    public List<String> getAllTables() throws DataAccessException {

        String query = "SELECT d.NAME as \"DATABASE_NAME\", t.TBL_NAME FROM TBLS t JOIN DBS d on d.DB_ID = t.DB_ID ORDER BY d.NAME, t.TBL_NAME";
        if (DatabaseType.POSTGRES.equals(getMetastoreDatabaseType())) {
            query = "SELECT d.\"NAME\" as \"DATABASE_NAME\", t.\"TBL_NAME\" FROM \"TBLS\" t JOIN \"DBS\" d on d.\"DB_ID\" = t.\"DB_ID\" ORDER BY d.\"NAME\", t.\"TBL_NAME\"";
        }
        List<String> allTables = hiveMetatoreJdbcTemplate.query(query, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int i) throws SQLException {
                String dbName = rs.getString("DATABASE_NAME");
                String tableName = rs.getString("TBL_NAME");
                return dbName + "." + tableName;
            }
        });
        return allTables;
    }


    public List<TableSchema> getTableSchemas() throws DataAccessException {

        String query = "SELECT d.NAME as \"DATABASE_NAME\", t.TBL_NAME, c.COLUMN_NAME c.TYPE_NAME "
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

}
