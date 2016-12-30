package com.thinkbiganalytics.common.constants;

/**
 * Created by sr186054 on 12/28/16.
 */
public enum KyloProcessorFlowType {

    CRITICAL_FAILURE("Critical Failure", "If this processor is triggered it will fail the job in Kylo"),
    NON_CRITICAL_FAILURE("Non Critical Failure", "If this processor is triggered it will indicate the failure in Kylo, but the job will not fail."),
    NORMAL_FLOW("Normal", "This is the default state for all processors unless specified otherwise"),
    WARNING("Warning", "If this processor is triggered it will show as a Warning in Kylo, but job will not fail");

    private String displayName;
    private String description;

    KyloProcessorFlowType(String displayName, String desc) {
        this.displayName = displayName;
        this.description = desc;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
