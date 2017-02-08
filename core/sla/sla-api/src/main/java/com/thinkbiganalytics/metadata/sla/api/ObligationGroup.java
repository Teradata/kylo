/**
 *
 */
package com.thinkbiganalytics.metadata.sla.api;

/*-
 * #%L
 * thinkbig-sla-api
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

import java.util.List;

/**
 * Groups together a set of obligations under an assessment rule governing how these obligations
 * contribute to the overall assessment of an SLA.
 */
public interface ObligationGroup {


    Condition getCondition();

    List<Obligation> getObligations();

    ServiceLevelAgreement getAgreement();

    /**
     * Describes how this group will contribute to the overall assessment success of the containing SLA
     */
    enum Condition {
        REQUIRED,       // Indicates successful assessment of this group is required for SLA success
        SUFFICIENT,
        OPTIONAL
    }
}
