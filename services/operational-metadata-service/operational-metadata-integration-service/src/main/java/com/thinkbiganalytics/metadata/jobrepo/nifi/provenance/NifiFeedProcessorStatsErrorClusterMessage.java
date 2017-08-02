package com.thinkbiganalytics.metadata.jobrepo.nifi.provenance;

import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorErrors;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Created by sr186054 on 8/1/17.
 */
public class NifiFeedProcessorStatsErrorClusterMessage implements Serializable {
    private Set<? extends NifiFeedProcessorErrors> errors;

    public NifiFeedProcessorStatsErrorClusterMessage(Set<? extends NifiFeedProcessorErrors> errors) {
        this.errors = errors;
    }

    public Set<? extends NifiFeedProcessorErrors> getErrors() {
        return errors;
    }

}
