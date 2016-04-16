package com.thinkbiganalytics.jobrepo.query.rowmapper;

/**
 * Created by sr186054 on 8/14/15.
 */
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ObjectRowMapper implements RowMapper {

    private List<String> columnNames = new ArrayList<String>();

    private void fetchColumnMetaData(ResultSet rs)  throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int i = 0;
        while(i<rsmd.getColumnCount()){
            i++;
            String columnName = rsmd.getColumnName(i);
            int columnType = rsmd.getColumnType(i);
            String tableName = rsmd.getTableName(i);
            columnNames.add(columnName);
        }
    }

    @Override
    public Object mapRow(ResultSet resultSet, int i) throws SQLException {
       if(columnNames.isEmpty())
       {
           fetchColumnMetaData(resultSet);
       }

        Object o = resultSet.getObject(1);
        return o;

    }
}
