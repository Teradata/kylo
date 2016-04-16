package com.thinkbiganalytics.jobrepo.query.builder;

import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;

import java.util.Map;

/**
 * Created by sr186054 on 4/13/16.
 */
public interface Query {
    String getQuery();

    void setQuery(String query);

    DatabaseType getDatabaseType();

    void setDatabaseType(DatabaseType databaseType);

    Map<String, Object> getNamedParameters();

    void addNamedParameters(Map<String, Object> namedParameters);

    String getQueryWithoutOrderBy();

    void setQueryWithoutOrderBy(String queryWithoutOrderBy);

    void setNamedParameters(Map<String, Object> namedParameters);
}
