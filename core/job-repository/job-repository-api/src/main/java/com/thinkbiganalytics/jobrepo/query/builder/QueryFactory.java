package com.thinkbiganalytics.jobrepo.query.builder;

import com.thinkbiganalytics.jobrepo.query.ConstructedQuery;
import com.thinkbiganalytics.jobrepo.query.support.DatabaseType;

import java.lang.reflect.Constructor;

/**
 * Factory to return the Query for a specific Database Type (i.e. MySql, Postgres etc)
 */
public class QueryFactory {

  public static <T> T getQuery(DatabaseType databaseType, Class<? extends ConstructedQuery> queryClass) {
    T query = null;
    try {
      //get constructor that takes a String as argument
      Constructor constructor = queryClass.getConstructor(DatabaseType.class);

      query = (T)
          constructor.newInstance(databaseType);

    } catch (Exception e) {

    }
    return query;
  }
}
