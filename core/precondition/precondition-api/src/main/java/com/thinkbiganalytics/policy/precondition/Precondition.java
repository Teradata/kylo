package com.thinkbiganalytics.policy.precondition;

import java.util.Set;

/**
 * A set of Metrics with conditions
 */
public interface Precondition {

    public Set<PreconditionGroup> getPreconditionObligations();
}
