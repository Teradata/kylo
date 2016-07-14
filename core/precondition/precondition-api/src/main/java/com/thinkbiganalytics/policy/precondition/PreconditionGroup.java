package com.thinkbiganalytics.policy.precondition;

import com.thinkbiganalytics.metadata.sla.api.Metric;

import java.util.Set;

/**
 * A Holder of Precondition metrics that are grouped together by a Condition
 */
public interface PreconditionGroup {

    Set<Metric> getMetrics();

    String getCondition();

}
