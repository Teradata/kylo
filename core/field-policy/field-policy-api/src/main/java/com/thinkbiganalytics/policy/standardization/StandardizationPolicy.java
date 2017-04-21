package com.thinkbiganalytics.policy.standardization;

/*-
 * #%L
 * thinkbig-field-policy-api
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


import com.thinkbiganalytics.policy.BaseFieldPolicy;

import java.io.Serializable;

/**
 * Provides cleansing or data standardization returning a new value from the provided value
 * Perform custom standardization logic to transform/cleanse the data before saving to the datalake.
 * The class should be annotated with a {@link Standardizer} and any fields needed to be captured by the end user should be annotated with the {@link com.thinkbiganalytics.policy.PolicyProperty}
 *
 * @see Standardizer class annotation
 */
public interface StandardizationPolicy extends BaseFieldPolicy, Serializable {

    /**
     * Convert a incoming {@code value} to new value.
     *
     * @param value the value to transform/cleanse
     * @return the new value
     */
    String convertValue(String value);

    /**
     * Whether a policy accepts a type of value
     *
     * @param value the value to transform/cleanse
     * @return true/false indicting whether the policy can accept the type of value
     */
    Boolean accepts(Object value);

    /**
     * Convert an incoming {@code value} to new value
     *
     * @param value the value to transform/cleanse
     * @return the new value
     */
    Object convertRawValue(Object value);

}
