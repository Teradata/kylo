package com.thinkbiganalytics.jobrepo.query.builder;

/**
 * Build Joins for a Query
 */
public class DefaultQueryJoinBuilder implements QueryJoinBuilder {

    private QueryBuilder queryBuilder;
    private String joinAlias;
    private String joinClause;
    private Query joinQuery;
    private JOIN_TYPE joinType;

    protected DefaultQueryJoinBuilder(QueryBuilder queryBuilder, Query query, JOIN_TYPE joinType) {
        this.queryBuilder = queryBuilder;
        this.joinType = joinType;
        this.joinQuery = query;
    }

    public static QueryJoinBuilder innerJoin(QueryBuilder queryBuilder, Query query) {
        QueryJoinBuilder builder = new DefaultQueryJoinBuilder(queryBuilder, query, JOIN_TYPE.INNER);
        return builder;
    }

    public static QueryJoinBuilder leftJoin(QueryBuilder queryBuilder, Query query) {
        return new DefaultQueryJoinBuilder(queryBuilder, query, JOIN_TYPE.LEFT);
    }

    @Override
    public QueryBuilder buildJoin() {
        Query query = new DefaultQuery();
        String join = " INNER JOIN ";
        if (JOIN_TYPE.LEFT.equals(this.joinType)) {
            join = " LEFT JOIN ";
        }
        String queryFragment = join + "( " + this.joinQuery.getQuery() + ") " + this.joinAlias + " on " + this.joinClause + " ";
        query.setQuery(queryFragment);
        query.setNamedParameters(this.joinQuery.getNamedParameters());
        this.queryBuilder.addJoin(query);
        return queryBuilder;
    }

    @Override
    public QueryJoinBuilder as(String as) {
        this.joinAlias = as;
        return this;
    }

    @Override
    public QueryBuilder on(String on) {
        this.joinClause = on;
        return buildJoin();
    }


}
