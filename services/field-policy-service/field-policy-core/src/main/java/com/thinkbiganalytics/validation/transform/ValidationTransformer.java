package com.thinkbiganalytics.validation.transform;

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
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;
import com.thinkbiganalytics.policy.validation.ValidationPolicy;

/**
 * Transformation class to convert domain {@link ValidationPolicy} to/from ui objects {@link FieldValidationRule}
 */
public interface ValidationTransformer {

    /**
     * Convert from the domain object to the user interface object
     *
     * @param standardizationRule the domain level validation rule
     * @return the user interface object representing the validation rule
     */
    FieldValidationRule toUIModel(ValidationPolicy standardizationRule);

    /**
     * convert from the User interface to the domain object
     *
     * @param rule the user interface validation rule
     * @return the domain validation object
     */
    ValidationPolicy fromUiModel(FieldValidationRule rule)
        throws PolicyTransformException;


}
