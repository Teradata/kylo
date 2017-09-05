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
import com.thinkbiganalytics.policy.PolicyPropertyTypes;
import com.thinkbiganalytics.policy.PropertyLabelValue;

import org.apache.commons.lang3.StringUtils;

/**
 * validates a string is not null, optionally allowing the string to be empty
 */
@Validator(name = "Not Null", description = "Validate a value is not null")
public class NotNullValidator implements ValidationPolicy {


    @PolicyProperty(name = "EMPTY_STRING", value = "false", displayName = "Allow Empty String Values",
                    hint = "If the value is a String, are empty strings not null?", type = PolicyPropertyTypes.PROPERTY_TYPE.select,
                    labelValues = {@PropertyLabelValue(label = "Yes", value = "true"),
                                   @PropertyLabelValue(label = "No", value = "false")})
    private boolean allowEmptyString = false;

    @PolicyProperty(name = "TRIM_STRING", value = "true", displayName = "Trim String Values",
                    hint = " If the value is a String, should it be trimmed before checking for null?",
                    type = PolicyPropertyTypes.PROPERTY_TYPE.select, labelValues = {@PropertyLabelValue(label = "Yes", value = "true"),
                                                                                    @PropertyLabelValue(label = "No",
                                                                                                        value = "false")})
    private boolean trimString = true;

    public NotNullValidator(@PolicyPropertyRef(name = "EMPTY_STRING") boolean allowEmptyString,
                            @PolicyPropertyRef(name = "TRIM_STRING") boolean trimString) {
        this.allowEmptyString = allowEmptyString;
        this.trimString = trimString;
    }

    @Override
    public boolean validate(Object value) {

        if (value == null) {
            return false;
        }
        if (!allowEmptyString && value instanceof String) {
            String svalue = (trimString ? StringUtils.trim((String) value) : (String) value);
            return (!StringUtils.isEmpty(svalue));
        }
        return true;
    }


    public boolean isAllowEmptyString() {
        return allowEmptyString;
    }

    public boolean isTrimString() {
        return trimString;
    }
}
