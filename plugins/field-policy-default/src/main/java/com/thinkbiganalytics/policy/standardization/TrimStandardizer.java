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


import org.apache.commons.lang3.StringUtils;

/**
 * Trim the leading and trailing spaces on a String
 */
@Standardizer(name = "Trim", description = "Trim the leading and trailing spaces on a string")
public class TrimStandardizer implements StandardizationPolicy {

    private static final TrimStandardizer instance = new TrimStandardizer();

    private TrimStandardizer() {
        super();
    }

    public static TrimStandardizer instance() {
        return instance;
    }

    @Override
    public String convertValue(String value) {
        return StringUtils.trim(value.toString());
    }

    public Boolean accepts (Object value) {
        return (value instanceof String);
    }

    public Object convertRawValue(Object value) {
        if (accepts(value)) {
                return convertValue((String)value);
            }
        return value;
    }
}
