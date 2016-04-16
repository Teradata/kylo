package com.thinkbiganalytics.jobrepo.query.rowmapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by sr186054 on 8/20/15.
 */
public class ObjectArrayRowMapper  implements RowMapper<Object[]> {
    public ObjectArrayRowMapper() {
    }

    private Integer columnCount = null;

    public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
        if(columnCount == null) {
            ResultSetMetaData rsmd = rs.getMetaData();
            columnCount = rsmd.getColumnCount();
        }
        Object[] row = new Object[columnCount];


        for(int i = 1; i <= columnCount; ++i) {
            Object obj = this.getColumnValue(rs, i);
            row[i-1] = convertToBigInteger(obj);
        }

        return row;
    }


    private Object convertToBigInteger(Object o){
        if(o instanceof Long){
            BigInteger bi = BigInteger.valueOf((Long)o);
            return bi;
        }
        else if(o instanceof Integer){
            BigInteger bi = BigInteger.valueOf(((Integer)o).intValue());
            return bi;
        }
        else {
            return o;
        }
    }


    protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index);
    }
}