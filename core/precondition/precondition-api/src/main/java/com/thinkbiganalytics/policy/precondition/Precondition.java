package com.thinkbiganalytics.policy.precondition;

/*-
 * #%L
 * thinkbig-precondition-api
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
