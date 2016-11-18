package com.thinkbiganalytics.hive.service;


import com.thinkbiganalytics.db.model.query.QueryResult;
import com.thinkbiganalytics.db.model.query.QueryResultColumn;
import com.thinkbiganalytics.db.model.schema.Field;
import com.thinkbiganalytics.db.model.schema.TableSchema;
import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.schema.DBSchemaParser;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Created by sr186054 on 2/11/16.
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

    public DataSource getDataSource(){
        return jdbcTemplate.getDataSource();
    }
    public DBSchemaParser getDBSchemaParser(){
        if(schemaParser == null) {
            schemaParser = new DBSchemaParser(getDataSource(), kerberosHiveConfiguration);
        }
        return schemaParser;
    }

    public List<String> getSchemaNames() {
        return getDBSchemaParser().listSchemas();
    }

    public List<String> getTables(String schema) {
        List<String> tables = getDBSchemaParser().listTables(schema);
        return tables;
    }

    /**
     * returns a list of schemanName.TableName
     * @return
     */
    private List<String> getAllTables(){
        List<String> allTables = new ArrayList<>();
        List<String> schemas = getSchemaNames();
        if(schemas != null) {
            for (String schema : schemas) {
                List<String> tables = getTables(schema);
                if(tables != null) {
                    for (String table :tables) {
                        allTables.add(schema+"."+table);
                    }
                }
            }
        }
        return allTables;
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
        databases.stream().forEach(database -> allTables.addAll(getTables(database.toString())));
        log.debug("time to get all tables " + (System.currentTimeMillis() - start) + " ms");
        return allTables;
    }

    public boolean testConnection() throws SQLException {
        return ((RefreshableDataSource) this.getDataSource()).testConnection();

    }

    /**
     * returns a list of populated TableSchema objects
     * @return
     */
    public List<TableSchema> getAllTableSchemas(){
        List<TableSchema> allTables = new ArrayList<>();
        List<String> schemas = getSchemaNames();
        if(schemas != null) {
            for (String schema : schemas) {
                List<String> tables = getTables(schema);
                if(tables != null) {
                    for (String table :tables) {
                        allTables.add(getTableSchema(schema,table));
                    }
                }
            }
        }
        return allTables;
    }

    /**
     * Describes the given Table
     * @param schema
     * @param table
     * @return
     */
    public TableSchema getTableSchema(String schema, String table) {
        return  getDBSchemaParser().describeTable(schema, table);
    }



    public List<Field> getFields(String schema, String table) {
        TableSchema tableSchema = getTableSchema(schema, table);
        if(tableSchema != null) {
           return tableSchema.getFields();
        }
        return null;
    }



    public  QueryResult browse(String schema, String table, String where, Integer limit) throws DataAccessException{

        if(where == null){
            where = "";
        }
        String query = "SELECT * from " + HiveUtils.quoteIdentifier(schema, table) + " " + where + " LIMIT " + limit;
        return browse(query);
    }



    public QueryResult browse(String query) throws DataAccessException{
        return query(query);

    }

    // TODO: Temporary until we determine how we want to ensure DDL isn't sent through
    private String safeQuery(String query) {
        return "SELECT kylo_.* FROM ("+query+") kylo_ LIMIT 1000";
    }


    public QueryResult query(String query) throws DataAccessException{
       final QueryResult queryResult = new QueryResult(query);
        final List<QueryResultColumn> columns = new ArrayList<>();
        final Map<String,Integer> displayNameMap = new HashMap<>();
        if(query != null && !query.toLowerCase().startsWith("show")) {
            query = safeQuery(query);
        }
        try {
            jdbcTemplate.query(query, new RowMapper<Map<String, Object>>() {
                @Override
                public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    if (columns.isEmpty()) {
                        ResultSetMetaData rsMetaData = rs.getMetaData();
                        for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
                            String colName = rsMetaData.getColumnName(i);
                            QueryResultColumn column = new QueryResultColumn();
                            column.setField(rsMetaData.getColumnName(i));
                            String displayName = rsMetaData.getColumnLabel(i);
                            column.setHiveColumnLabel(displayName);
                            //remove the table name if it exists
                            displayName = StringUtils.substringAfterLast(displayName, ".");
                            Integer count = 0;
                            if (displayNameMap.containsKey(displayName)) {
                                count = displayNameMap.get(displayName);
                                count++;
                            }
                            displayNameMap.put(displayName, count);
                            column.setDisplayName(displayName + "" + (count > 0 ? count : ""));

                            //HiveResultSetMetaData object doesnt support accesss to the rsMetadata.getSchemaName or getTableName())
                            //    column.setDatabaseName(rsMetaData.getSchemaName(i));
                            column.setTableName(StringUtils.substringAfterLast(rsMetaData.getColumnName(i), "."));
                            column.setDataType(Field.sqlTypeToDataType(rsMetaData.getColumnType(i)));
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

    }


    public class SchemaTable {
        private String schema;
        private String table;

        public SchemaTable(String schema, String table) {
            this.schema = schema;
            this.table = table;
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }
    }

}
