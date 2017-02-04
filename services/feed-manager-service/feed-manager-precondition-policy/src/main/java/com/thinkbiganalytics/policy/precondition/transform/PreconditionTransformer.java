package com.thinkbiganalytics.policy.precondition.transform;

/*-
 * #%L
 * thinkbig-feed-manager-precondition-policy
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

import com.thinkbiganalytics.policy.PolicyTransformException;
import com.thinkbiganalytics.policy.precondition.Precondition;
import com.thinkbiganalytics.policy.rest.model.PreconditionRule;

/**
 * Transform a user interface {@link PreconditionRule} to/from the domain {@link com.thinkbiganalytics.policy.precondition.Precondition}
 */
public interface PreconditionTransformer {

    PreconditionRule toUIModel(Precondition standardizationRule);

    Precondition fromUiModel(PreconditionRule rule)
        throws PolicyTransformException;


}
