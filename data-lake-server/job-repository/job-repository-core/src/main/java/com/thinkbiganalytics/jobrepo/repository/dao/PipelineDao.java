package com.thinkbiganalytics.jobrepo.repository.dao;

import com.thinkbiganalytics.jobrepo.query.rowmapper.ObjectArrayRowMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * Created by sr186054 on 8/20/15.
 */
@Named
public class PipelineDao  implements InitializingBean {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.jdbcTemplate);
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String,Object>> queryForList(String sql){
        ColumnMapRowMapper m = new ColumnMapRowMapper();
        List<Map<String,Object>> rows = getJdbcTemplate().queryForList(sql);
        return rows;
    }

    public List<Object[]> queryForObjectArray(String sql){
        List<Object[]> list = getJdbcTemplate().query(sql, new ObjectArrayRowMapper());

        return list;
    }

    public List<Object[]> queryForObjectArray(String sql,Map<Class,Class>conversionClass){
        List<Object[]> list = getJdbcTemplate().query(sql, new ObjectArrayRowMapper());

        return list;
    }

    public <T> List<T>  queryForList(String sql, Class<T> clazz){
        List<T> result = (List<T>) getJdbcTemplate().queryForList(sql, clazz);
        return result;
    }

}
