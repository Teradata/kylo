package com.thinkbiganalytics.standardization.transform;

/*-
 * #%L
 * thinkbig-field-policy-core
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
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.standardization.StandardizationPolicy;

/**
 * Transformation class to convert domain {@link StandardizationPolicy} to/from ui objects {@link FieldStandardizationRule}
 */
public interface StandardizationTransformer {

    /**
     * Convert from the domain object to the user interface object
     *
     * @param standardizationRule the domain level standardizer
     * @return the user interface object
     */
    FieldStandardizationRule toUIModel(StandardizationPolicy standardizationRule);

    /**
     * convert from the User interface to the domain object
     *
     * @param rule the ui rule
     * @return the domain standardizer
     */
    StandardizationPolicy fromUiModel(FieldStandardizationRule rule)
        throws PolicyTransformException;


}
