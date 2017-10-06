package com.thinkbiganalytics.policy.standardization;

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

import org.apache.commons.lang3.StringUtils;

/**
 * Applies a default value in place of null or empty
 */
@Standardizer(name = "Default Value", description = "Applies a default value if null")
public class DefaultValueStandardizer implements StandardizationPolicy, AcceptsEmptyValues {

    @PolicyProperty(name = "Default Value", hint = "If the value is null it will use this supplied value", required = true)
    private String defaultStr;

    public DefaultValueStandardizer(@PolicyPropertyRef(name = "Default Value") String defaultStr) {
        this.defaultStr = defaultStr;
    }

    @Override
    public String convertValue(String value) {
        return (StringUtils.isEmpty(value) ? defaultStr : value);
    }

    public String getDefaultStr() {
        return defaultStr;
    }

    public Boolean accepts (Object value) {
        return (value == null || value instanceof String);
    }

    public Object convertRawValue(Object value) {
        if (accepts(value)) {
            if (value == null ||(value instanceof String && StringUtils.isEmpty((String)value))) {
                return String.valueOf(defaultStr);
            }
            else {
                return convertValue(value.toString());
            }
        }
        return value;
    }
}
