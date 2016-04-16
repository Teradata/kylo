package com.thinkbiganalytics.jobrepo.query.feed;

import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.rowmapper.JobExecutionFeedRowMapper;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.support.QueryColumnFilterSqlString;
import org.springframework.jdbc.core.RowMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Query for the Feeds Section of the Pipeline Controller
 *
 */
public class FeedQueryWithLatestFeedColumn extends FeedQuery {

    public FeedQueryWithLatestFeedColumn(DatabaseType databaseType) {
        super(databaseType);
    }

    public QueryBuilder getQueryBuilder(){
    QueryBuilder q = super.getQueryBuilder();
            q.addSelectColumn("CASE WHEN latestFeeds.JOB_EXECUTION_ID is not null then 'true'else'false' end as IS_LATEST")
                    .leftJoin(getLatestFeedQuery()).as("latestFeeds").on("latestFeeds.JOB_EXECUTION_ID = e.JOB_EXECUTION_ID ");
        return q;

}

    public Query getLatestFeedQuery(){
        List<ColumnFilter> filters = new ArrayList<>();
        filters.add(new QueryColumnFilterSqlString("STATUS","STARTING","!="));
        MaxFeedQuery q = new MaxFeedQuery(getDatabaseType());
        q.setColumnFilterList(filters);
        return q.buildQuery();
    }

    @Override
    public RowMapper getRowMapper() {
        return new JobExecutionFeedRowMapper();
    }


}
