package com.thinkbiganalytics.policy.validation;

/*-
 * #%L
 * thinkbig-field-policy-default
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


import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;

import org.apache.commons.lang3.Validate;

/**
 * Validates a string is within a min/max lenght
 */
@Validator(name = "Length", description = "Validate String falls between desired length")
public class LengthValidator implements ValidationPolicy<String> {

    @PolicyProperty(name = "Max Length")
    private int maxLength;
    @PolicyProperty(name = "Min Length")
    private int minLength;

    public LengthValidator(@PolicyPropertyRef(name = "Min Length") int min, @PolicyPropertyRef(name = "Max Length") int max) {
        Validate.isTrue(min >= 0, "Minimum must be > 0");
        this.minLength = min;
        this.maxLength = max;
    }

    @Override
    public boolean validate(String value) {
        int len = value.length();
        return (len <= maxLength && len >= minLength);
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getMinLength() {
        return minLength;
    }
}
