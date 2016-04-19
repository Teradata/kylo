package com.thinkbiganalytics.hive.service;


import com.thinkbiganalytics.db.model.schema.DatabaseMetadata;
import com.thinkbiganalytics.db.model.schema.Field;
import com.thinkbiganalytics.db.model.schema.TableSchema;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
 * Created by sr186054 on 2/17/16.
 */
@Service("hiveMetastoreService")
public class HiveMetastoreService {

    @Inject
    @Qualifier("hiveMetatoreJdbcTemplate")
    private JdbcTemplate hiveMetatoreJdbcTemplate;

    public DataSource getDataSource(){

      return hiveMetatoreJdbcTemplate.getDataSource();
    }



    public  List<DatabaseMetadata> getTableColumns() throws DataAccessException{

        List<DatabaseMetadata> metadata = new ArrayList<>();

        String query = "SELECT d.NAME as \"DATABASE_NAME\", t.TBL_NAME, c.COLUMN_NAME FROM COLUMNS_V2 c JOIN TBLS t ON c.CD_ID=t.TBL_ID JOIN DBS d on d.DB_ID = t.DB_ID ORDER BY d.NAME, t.TBL_NAME";
       metadata = hiveMetatoreJdbcTemplate.query(query, new RowMapper<DatabaseMetadata>() {
            @Override
            public DatabaseMetadata mapRow(ResultSet rs, int i) throws SQLException {
                DatabaseMetadata row = new DatabaseMetadata();
                row.setDatabaseName(rs.getString("DATABASE_NAME"));
                row.setColumnName(rs.getString("COLUMN_NAME"));
                row.setTableName(rs.getString("TBL_NAME"));
                return row;
            }
        });


        return metadata;

    }

    public List<String> getAllTables() throws DataAccessException{
        List<String> allTables = new ArrayList<>();
        String query = "SELECT d.NAME as \"DATABASE_NAME\", t.TBL_NAME FROM TBLS t JOIN DBS d on d.DB_ID = t.DB_ID ORDER BY d.NAME, t.TBL_NAME";

        allTables = hiveMetatoreJdbcTemplate.query(query, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int i) throws SQLException {
                String dbName = rs.getString("DATABASE_NAME");
                String tableName = rs.getString("TBL_NAME");
                return dbName+"."+tableName;
            }
        });
        return allTables;
    }


    public  List<TableSchema> getTableSchemas() throws DataAccessException{

        String query = "SELECT d.NAME as \"DATABASE_NAME\", t.TBL_NAME, c.COLUMN_NAME, c. TYPE_NAME FROM COLUMNS_V2 c JOIN TBLS t ON c.CD_ID=t.TBL_ID JOIN DBS d on d.DB_ID = t.DB_ID";
       final List<TableSchema> metadata = new ArrayList<>();
        final Map<String,Map<String,TableSchema>> databaseTables = new HashMap<>();

        hiveMetatoreJdbcTemplate.query(query, new RowMapper<Object>() {
            @Override
            public TableSchema mapRow(ResultSet rs, int i) throws SQLException {
                String dbName = rs.getString("DATABASE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                String tableName = rs.getString("TBL_NAME");
                String columnType = rs.getString("TYPE_NAME");
                if(!databaseTables.containsKey(dbName)){
                    databaseTables.put(dbName,new HashMap<String,TableSchema>());
                }
                Map<String,TableSchema>tables = databaseTables.get(dbName);
                if(!tables.containsKey(tableName)){
                    TableSchema schema = new TableSchema();
                    schema.setName(tableName);
                    schema.setSchemaName(dbName);
                    schema.setFields(new ArrayList<Field>());
                    tables.put(tableName, schema);
                    metadata.add(schema);
                }
                TableSchema schema = tables.get(tableName);
                Field field = new Field();
                field.setName(columnName);
                field.setDataType(columnType);
                schema.getFields().add(field);
                return schema;
            }
        });

        return metadata;

    }

}
