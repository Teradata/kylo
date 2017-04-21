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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Validator(name = "Validate Characters", description = "Validate common character functions such as upper/lower/alpha numeric.. ")
public class CharacterValidator implements ValidationPolicy<String> {

    private static final Logger log = LoggerFactory.getLogger(CharacterValidator.class);


    @PolicyProperty(name = "Validation Type", hint = "The type of character validation to perform", type = PolicyPropertyTypes.PROPERTY_TYPE.select, required = true,
                    labelValues = {@PropertyLabelValue(label = "Uppercase", value = "UPPERCASE"),
                                   @PropertyLabelValue(label = "Lowercase", value = "LOWERCASE"),
                                   @PropertyLabelValue(label = "AlphaNumeric", value = "ALPHA_NUMERIC"),
                                   @PropertyLabelValue(label = "Alpha", value = "ALPHA"),
                                   @PropertyLabelValue(label = "Numeric", value = "NUMERIC")})
    private String validationType;


    public CharacterValidator(@PolicyPropertyRef(name = "Validation Type") String validationType) {
        this.validationType = validationType;
    }


    @Override
    public boolean validate(String value) {

        if (StringUtils.isBlank(validationType)) {
            return true;
        }
        String trimmedValue = StringUtils.deleteWhitespace(value);
        if ("UPPERCASE".equalsIgnoreCase(validationType)) {
            return trimmedValue.toUpperCase().equals(trimmedValue);
        } else if ("LOWERCASE".equalsIgnoreCase(validationType)) {
            return trimmedValue.toLowerCase().equals(trimmedValue);
        } else if ("ALPHA_NUMERIC".equalsIgnoreCase(validationType)) {
            return StringUtils.isAlphanumeric(trimmedValue);
        } else if ("ALPHA".equalsIgnoreCase(validationType)) {
            return StringUtils.isAlpha(trimmedValue);
        } else if ("NUMERIC".equalsIgnoreCase(validationType)) {
            return StringUtils.isNumeric(trimmedValue);
        }
        return true;
    }


}

