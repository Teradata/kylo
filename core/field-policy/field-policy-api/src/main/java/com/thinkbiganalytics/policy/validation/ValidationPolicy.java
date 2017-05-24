package com.thinkbiganalytics.policy.validation;

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
 * Perform custom validation logic while moving data into the datalake, validating each value.
 * The class should be annotated with a {@link Validator} and any fields needed to be captured by the end user should be annotated with the {@link com.thinkbiganalytics.policy.PolicyProperty}
 *
 * @see Validator class annotation
 */
public interface ValidationPolicy<T> extends BaseFieldPolicy, Serializable {

    /**
     * Perform custom validation logic return a {@code true} if the passed in {@code value} is valid, {@code false} if it is invalid.
     *
     * @param value the value to validate
     * @return {@code true} if the passed in {@code value} is valid, {@code false} if it is invalid
     */
    boolean validate(T value);


}
