package com.thinkbiganalytics.feedmgr.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.thinkbiganalytics.nifi.rest.model.NifiProcessorSchedule;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;

import java.util.List;

/**
 * Created by sr186054 on 2/22/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedSchedule extends NifiProcessorSchedule {

    List<PreconditionRule> preconditions;


    public List<PreconditionRule> getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(List<PreconditionRule> preconditions) {
        this.preconditions = preconditions;
    }


    public boolean hasPreconditions() {
        return this.preconditions != null && !this.preconditions.isEmpty();
    }
}
