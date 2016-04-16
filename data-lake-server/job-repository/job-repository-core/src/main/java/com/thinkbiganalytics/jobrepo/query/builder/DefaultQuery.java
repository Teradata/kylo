package com.thinkbiganalytics.jobrepo.query.builder;

import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;

import java.util.HashMap;
import java.util.Map;

/**
 * The Query that gets built by the QueryBuilder.
 * This is used by the BaseDao to query the database with the associated Bind Parameters
 * @see QueryBuilder
 *
 */
public class DefaultQuery implements Query {

    private DatabaseType databaseType;

    private String queryWithoutOrderBy;

    private String query;
    private Map<String, Object> namedParameters = new HashMap<String, Object>();

    public DefaultQuery() {

    }
    public DefaultQuery(String query) {
        this.query = query;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    @Override
    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    @Override
    public Map<String, Object> getNamedParameters() {
        return namedParameters;
    }

    @Override
    public void addNamedParameters(Map<String, Object> namedParameters){
        if(namedParameters != null && !namedParameters.isEmpty()){
            this.namedParameters.putAll(namedParameters);
        }
    }

    @Override
    public String getQueryWithoutOrderBy() {
        return queryWithoutOrderBy;
    }

    @Override
    public void setQueryWithoutOrderBy(String queryWithoutOrderBy) {
        this.queryWithoutOrderBy = queryWithoutOrderBy;
    }

    @Override
    public void setNamedParameters(Map<String, Object> namedParameters) {
        this.namedParameters = namedParameters;
    }
}
