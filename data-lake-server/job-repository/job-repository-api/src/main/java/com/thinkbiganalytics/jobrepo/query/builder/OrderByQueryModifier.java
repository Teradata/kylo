package com.thinkbiganalytics.jobrepo.query.builder;


import com.thinkbiganalytics.jobrepo.query.support.OrderBy;

/**
 * Modify the Order By column before it gets applied to the SQL query
 */
public interface OrderByQueryModifier {

  public void modifyOrderByQueryName(OrderBy orderBy);

}
