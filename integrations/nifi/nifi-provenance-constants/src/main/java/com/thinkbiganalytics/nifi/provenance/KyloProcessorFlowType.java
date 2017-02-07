package com.thinkbiganalytics.nifi.provenance;

/*-
 * #%L
 * thinkbig-nifi-provenance-constants
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;

/**
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
