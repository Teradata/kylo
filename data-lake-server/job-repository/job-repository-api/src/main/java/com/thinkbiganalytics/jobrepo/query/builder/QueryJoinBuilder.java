package com.thinkbiganalytics.jobrepo.query.builder;

/**
 * Created by sr186054 on 4/14/16.
 */
public interface QueryJoinBuilder {

  QueryBuilder buildJoin();

  QueryJoinBuilder as(String as);

  QueryBuilder on(String on);

  enum JOIN_TYPE {
    INNER, LEFT
  }
}
