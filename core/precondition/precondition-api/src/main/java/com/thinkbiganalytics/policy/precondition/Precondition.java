package com.thinkbiganalytics.policy.precondition;

import com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup;

import java.util.Set;

/**
 * A set of Metrics with conditions
 */
public interface Precondition {

    Set<ObligationGroup> buildPreconditionObligations();
}
