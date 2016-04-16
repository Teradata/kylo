package com.thinkbiganalytics.jobrepo.query.feed;

import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.substitution.DatabaseQuerySubstitutionFactory;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by sr186054 on 9/17/15.
 */
public class FeedAverageRunTimesQuery extends AbstractConstructedQuery {


    public FeedAverageRunTimesQuery(DatabaseType databaseType) {
        super(databaseType);
    }


    @Override
    public Query buildQuery() {
        return getQueryBuilder().build();
    }

    @Override
    public QueryBuilder getQueryBuilder() {
        return newQueryBuilder()
                .select(" SELECT p2.STRING_VAL as FEED_NAME,  AVG(" + DatabaseQuerySubstitutionFactory.AVG_JOB_EXECUTION_RUN_TIME_TEMPLATE_STRING + ") as AVG_RUN_TIME")
                .from("  BATCH_JOB_EXECUTION  e " +
                                "inner JOIN BATCH_JOB_EXECUTION_PARAMS AS p1 ON e.JOB_EXECUTION_ID = p1.JOB_EXECUTION_ID " +
                                "and p1.STRING_VAL = 'FEED' " +
                                "inner JOIN BATCH_JOB_EXECUTION_PARAMS AS p2 ON  e.JOB_EXECUTION_ID = p2.JOB_EXECUTION_ID " +
                                "and p2.KEY_NAME   = 'feed' " +
                                "WHERE  e.STATUS       = 'COMPLETED'   " +
                                "AND e.EXIT_CODE != 'NOOP'")
                .groupBy("p2.STRING_VAL");

    }

    @Override
    public RowMapper getRowMapper() {
        return new RowMapper() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

                Map mapOfColValues =  new LinkedCaseInsensitiveMap(2);


                String feedName = rs.getString("FEED_NAME");
                Double runTime = rs.getDouble("AVG_RUN_TIME");

                mapOfColValues.put("FEED_NAME", feedName);
                mapOfColValues.put("AVG_RUN_TIME", runTime);


                return mapOfColValues;


            }
        };

    }
}
