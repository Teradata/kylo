package com.thinkbiganalytics.jobrepo.repository.dao;

import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.ConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryFactory;
import com.thinkbiganalytics.jobrepo.query.rowmapper.LongRowMapper;
import com.thinkbiganalytics.jobrepo.query.rowmapper.ObjectRowMapper;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitutionFactory;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sr186054 on 8/28/15.
 */
public class BaseQueryDao implements InitializingBean {
    private static Logger LOG = LoggerFactory.getLogger(BaseQueryDao.class);
    @Autowired
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    @Autowired
    protected JdbcTemplate jdbcTemplate;

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.namedParameterJdbcTemplate);
    }


    public <T extends ConstructedQuery> T getQuery(Class<T> queryClass){
        return QueryFactory.getQuery(getDatabaseType(), queryClass);
    }

    public DatabaseType getDatabaseType(){
        DatabaseType databaseType = DatabaseType.MYSQL;
        try {
            databaseType = DatabaseType.fromMetaData(this.jdbcTemplate.getDataSource());
        }catch(MetaDataAccessException e){
            LOG.error("Unable to get DatabaseType from Metadata... reverting to standard Mysql type");
        }
        return databaseType;
    }

    public List<Object> selectDistinctColumnValues(ConstructedQuery baseQuery, String column) {
        Query query = baseQuery.buildQuery();

        String queryString = query.getQueryWithoutOrderBy();
        String query2 = "SELECT DISTINCT x." + column + " " +
                "FROM (" + queryString + ") x";

        List<Object> objects = namedParameterJdbcTemplate.query(query2, query.getNamedParameters(), new ObjectRowMapper());
        return objects;
    }


    public Long selectCount(AbstractConstructedQuery baseQuery) {
        Query query = baseQuery.buildQuery();
        String queryString = query.getQueryWithoutOrderBy();
        String query2 = "SELECT COUNT(*) " +
                "FROM (" + queryString + ") x ";

        Long count = namedParameterJdbcTemplate.queryForObject(query2, query.getNamedParameters(), new LongRowMapper());
        return count;
    }



    public <T> List<T> findList(final AbstractConstructedQuery query){
        return findList(query,0,null);
    }



    public <T> List<T> findList(final AbstractConstructedQuery baseQuery, final Integer start, final Integer limit) {

       final String limitAndOffset = DatabaseQuerySubstitutionFactory.getDatabaseSubstitution(baseQuery.getDatabaseType()).limitAndOffset(limit,start);

        ResultSetExtractor<List<T>> extractor = new ResultSetExtractor<List<T>>() {
            private List<T> list = new ArrayList<T>();

            public List<T> extractData(ResultSet rs) throws SQLException, DataAccessException {
                int rowNum = 0;
                Integer _limit = limit;
                if(StringUtils.isBlank(limitAndOffset)) {

                    for (rowNum = 0; rowNum < start && rs.next(); ++rowNum) {
                        ;
                    }
                    if (_limit == null || _limit < 0) {
                        _limit = Integer.MAX_VALUE - start;
                    }
                    while (rowNum < start + _limit && rs.next()) {
                        RowMapper<T> rowMapper = baseQuery.getRowMapper();
                        this.list.add(rowMapper.mapRow(rs, rowNum));
                        ++rowNum;
                    }

                }
                else {

                    while(rowNum < _limit && rs.next()){
                        RowMapper<T> rowMapper = baseQuery.getRowMapper();
                        this.list.add(rowMapper.mapRow(rs, rowNum));
                        ++rowNum;
                    }


                }



                return this.list;
            }
        };

        Query query = baseQuery.buildQuery();
        //apply the limit and offset to the query
        String queryString  = query.getQuery()+" "+limitAndOffset;
        //String queryString = query.getQuery();
      //     LOG.info("FINAL QUERY IS "+queryString);
        List<T> results = namedParameterJdbcTemplate.query(queryString, query.getNamedParameters(), extractor);
        return results;
    }

}
