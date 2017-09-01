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


import com.thinkbiganalytics.discovery.model.DefaultQueryResult;
import com.thinkbiganalytics.discovery.model.DefaultQueryResultColumn;
import com.thinkbiganalytics.discovery.schema.QueryResult;
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;
import com.thinkbiganalytics.discovery.schema.TableSchema;
import com.thinkbiganalytics.discovery.util.ParserHelper;
import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.kerberos.KerberosUtil;
import com.thinkbiganalytics.schema.DBSchemaParser;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Executes Hive queries and retrieves Hive metadata.
 *
 * <p>Acquires a Kerberos ticket as needed to execute the Hive queries. The HTTP transport mode for Hive requires a Kerberos ticket whenever interacting with the Hive JDBC driver.</p>
 */
public class HiveService {

    private static final Logger log = LoggerFactory.getLogger(HiveService.class);

    @Inject
    @Qualifier("hiveJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Inject
    @Qualifier("kerberosHiveConfiguration")
    private KerberosTicketConfiguration kerberosHiveConfiguration;

    private DBSchemaParser schemaParser = null;

    public DataSource getDataSource() {
        return jdbcTemplate.getDataSource();
    }

    public DBSchemaParser getDBSchemaParser() {
        if (schemaParser == null) {
            schemaParser = new DBSchemaParser(getDataSource(), kerberosHiveConfiguration);
        }
        return schemaParser;
    }

    public List<String> getSchemaNames() {
        return KerberosUtil.runWithOrWithoutKerberos(() -> getDBSchemaParser().listSchemas(), kerberosHiveConfiguration);
    }

    public List<String> getTables(String schema) {
        return KerberosUtil.runWithOrWithoutKerberos(() -> getDBSchemaParser().listTables(schema, null), kerberosHiveConfiguration);
    }

    /**
     * returns a list of all the scheam.tablename for a given schema
     */
    public List<String> getTablesForImpersonatedUser(String schema) {
        QueryResult tables = query("show tables in " + schema);
        return tables.getRows().stream().flatMap(row -> row.entrySet().stream()).map(e -> schema + "." + e.getValue().toString()).collect(Collectors.toList());
    }


    /**
     * returns a list of all the schema.tablename
     */
    public List<String> getAllTablesForImpersonatedUser() {
        long start = System.currentTimeMillis();
        List<String> allTables = new ArrayList<>();
        QueryResult result = query("show databases");
        List<Object> databases = result.getRows().stream().flatMap(row -> row.entrySet().stream()).map(e -> e.getValue()).collect(Collectors.toList());
        databases.stream().forEach(database -> allTables.addAll(getTablesForImpersonatedUser(database.toString())));
        log.debug("time to get all tables " + (System.currentTimeMillis() - start) + " ms");
        return allTables;
    }

    public boolean testConnection() throws SQLException {
        return ((RefreshableDataSource) this.getDataSource()).testConnection();

    }

    /**
     * Describes the given Table
     */
    public TableSchema getTableSchema(String schema, String table) {
        return KerberosUtil.runWithOrWithoutKerberos(() -> getDBSchemaParser().describeTable(schema, table), kerberosHiveConfiguration);
    }

    public QueryResult browse(String schema, String table, String where, Integer limit) throws DataAccessException {

        if (where == null) {
            where = "";
        }
        String query = "SELECT * from " + HiveUtils.quoteIdentifier(schema, table) + " " + where + " LIMIT " + limit;
        return browse(query);
    }


    public QueryResult browse(String query) throws DataAccessException {
        return query(query);

    }

    // TODO: Temporary until we determine how we want to ensure DDL isn't sent through
    private String safeQuery(String query) {
        return "SELECT kylo_.* FROM (" + query + ") kylo_ LIMIT 1000";
    }

    private boolean validateQuery(String query) {
        String[] validSql = new String[]{"show", "select", "desc", "describe"};
        String testQuery = StringUtils.trimToEmpty(query).toLowerCase();
        if (StringUtils.isNotEmpty(testQuery) && Arrays.stream(validSql).anyMatch(start -> testQuery.startsWith(start))) {
            return true;
        }
        return false;
    }

    public QueryResult query(String query) throws DataAccessException {
        final DefaultQueryResult queryResult = new DefaultQueryResult(query);
        final List<QueryResultColumn> columns = new ArrayList<>();
        final Map<String, Integer> displayNameMap = new HashMap<>();
        if (!validateQuery(query)) {
            throw new DataRetrievalFailureException("Invalid Query: " + query);
        }
        return KerberosUtil.runWithOrWithoutKerberos(() -> {
            try {
                //  Setting in order to query complex formats like parquet
                jdbcTemplate.execute("set hive.optimize.index.filter=false");
                jdbcTemplate.query(query, new RowMapper<Map<String, Object>>() {
                    @Override
                    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        if (columns.isEmpty()) {
                            ResultSetMetaData rsMetaData = rs.getMetaData();
                            for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                                String colName = rsMetaData.getColumnName(i);
                                DefaultQueryResultColumn column = new DefaultQueryResultColumn();
                                column.setField(rsMetaData.getColumnName(i));
                                String displayName = rsMetaData.getColumnLabel(i);
                                column.setHiveColumnLabel(displayName);
                                //remove the table name if it exists
                                displayName = StringUtils.contains(displayName, ".") ? StringUtils.substringAfterLast(displayName, ".") : displayName;
                                Integer count = 0;
                                if (displayNameMap.containsKey(displayName)) {
                                    count = displayNameMap.get(displayName);
                                    count++;
                                }
                                displayNameMap.put(displayName, count);
                                column.setDisplayName(displayName + "" + (count > 0 ? count : ""));

                                column.setTableName(StringUtils.substringAfterLast(rsMetaData.getColumnName(i), "."));
                                column.setDataType(ParserHelper.sqlTypeToHiveType(rsMetaData.getColumnType(i)));
                                columns.add(column);
                            }
                            queryResult.setColumns(columns);
                        }
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (QueryResultColumn column : columns) {
                            row.put(column.getDisplayName(), rs.getObject(column.getHiveColumnLabel()));
                        }
                        queryResult.addRow(row);
                        return row;
                    }
                });

            } catch (DataAccessException dae) {
                dae.printStackTrace();
                throw dae;
            }
            return queryResult;
        }, kerberosHiveConfiguration);
    }


}
