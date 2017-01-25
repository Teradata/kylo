package com.thinkbiganalytics.nifi.provenance;

import java.io.Serializable;

/**
 * Created by sr186054 on 12/28/16.
 */
public enum KyloProcessorFlowType implements Serializable {

    @Deprecated
    CRITICAL_FAILURE("Critical Failure", "If this processor is triggered it will fail the job in Kylo", true),
    @Deprecated
    NON_CRITICAL_FAILURE("Non Critical Failure", "If this processor is triggered it will fail the step execution in in Kylo, but the job will not fail.", true),
    FAILURE("Failure", "If this processor is triggered it will fail the job in Kylo."),
    WARNING("Warning", "If this processor is triggered it will mark the step execution as a warning, but the job will not fail in Kylo."),
    NORMAL_FLOW("Normal", "This is the default state for all processors unless specified otherwise");

    private String displayName;
    private String description;
    private boolean deprecated;

    KyloProcessorFlowType(String displayName, String desc) {
        this.displayName = displayName;
        this.description = desc;
    }

    KyloProcessorFlowType(String displayName, String desc, boolean deprecated) {
        this.displayName = displayName;
        this.description = desc;
        this.deprecated = true;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
