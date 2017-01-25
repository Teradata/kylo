package com.thinkbiganalytics.policy.precondition;

import com.thinkbiganalytics.metadata.rest.model.sla.ObligationGroup;

import java.util.Set;

/**
 * Indicates the given class is a Precondition.
 * it must build up a set of {@link ObligationGroup} objects that include the Metrics that will be used when evaluating the Precondition
 */
public interface Precondition {

    /**
     * Build up a set of {@link ObligationGroup} with percondition metrics
     *
     * @return a set of {@link ObligationGroup} that include {@link com.thinkbiganalytics.metadata.sla.api.Metric} for precondition evaluation
     */
    Set<ObligationGroup> buildPreconditionObligations();
}
