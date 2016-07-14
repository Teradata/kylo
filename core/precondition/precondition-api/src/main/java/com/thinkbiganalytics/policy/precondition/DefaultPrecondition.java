package com.thinkbiganalytics.policy.precondition;

import java.util.Set;

/**
 * Created by sr186054 on 7/13/16.
 */
public class DefaultPrecondition implements Precondition {


    private Set<PreconditionGroup> preconditionObligations;

    public DefaultPrecondition(Set<PreconditionGroup> preconditionObligations) {
        this.preconditionObligations = preconditionObligations;
    }

    @Override
    public Set<PreconditionGroup> getPreconditionObligations() {
        return preconditionObligations;
    }

    public void setPreconditionObligations(Set<PreconditionGroup> preconditionObligations) {
        this.preconditionObligations = preconditionObligations;
    }
}
