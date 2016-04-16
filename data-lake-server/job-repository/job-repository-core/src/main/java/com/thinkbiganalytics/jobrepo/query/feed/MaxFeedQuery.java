package com.thinkbiganalytics.jobrepo.query.feed;

import com.thinkbiganalytics.jobrepo.query.AbstractConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.builder.ColumnFilterQueryModifier;
import com.thinkbiganalytics.jobrepo.query.builder.OrderByQueryModifier;
import com.thinkbiganalytics.jobrepo.query.builder.Query;
import com.thinkbiganalytics.jobrepo.query.builder.QueryBuilder;
import com.thinkbiganalytics.jobrepo.query.support.ColumnFilter;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;
import com.thinkbiganalytics.jobrepo.query.support.FeedQueryUtil;
import com.thinkbiganalytics.jobrepo.query.support.OrderBy;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;

/**
 * Created by sr186054 on 8/28/15.
 */
public class MaxFeedQuery extends AbstractConstructedQuery implements FeedQueryConstants{
    public MaxFeedQuery(DatabaseType databaseType) {
        super(databaseType);
    }


    @Override
    public Query buildQuery() {
        return getQueryBuilder().buildWithQueryModifiers(new ColumnFilterQueryModifier() {
            @Override
            public void modifyFilterQueryValue(ColumnFilter columnFilter) {
                String name = columnFilter.getName();
                String strVal = columnFilter.getStringValue();

                    columnFilter.setTableAlias("e");
                    if (("STRING_VAL".equals(name) || (QUERY_FEED_NAME_COLUMN.equals(name)) && !QUERY_ALL_VALUE.equalsIgnoreCase(strVal))) {
                        columnFilter.setQueryName("STRING_VAL");
                        columnFilter.setTableAlias("feedName");
                    }
            }
        }, new OrderByQueryModifier() {
            @Override
            public void modifyOrderByQueryName(OrderBy orderBy) {
                if (orderBy != null) {
                    String column = orderBy.getColumnName();
                    orderBy.setTableAlias("e");
                    if (QUERY_FEED_NAME_COLUMN.equalsIgnoreCase(column)) {
                        orderBy.setQueryName("STRING_VAL");
                        orderBy.setTableAlias("feedName");
                    }
                }
            }
        });
    }


    @Override
    public QueryBuilder getQueryBuilder() {
        return newQueryBuilder()
                .select("select feedName.STRING_VAL as FEED_NAME, MAX(e.JOB_EXECUTION_ID) JOB_EXECUTION_ID  " )
                .from("FROM BATCH_JOB_EXECUTION e  " +
                        FeedQueryUtil.feedQueryJoin("e", "feedName") )
                .groupBy("feedName.STRING_VAL ");


    }

    @Override
    public RowMapper getRowMapper() {
        return new ColumnMapRowMapper();
    }
}
