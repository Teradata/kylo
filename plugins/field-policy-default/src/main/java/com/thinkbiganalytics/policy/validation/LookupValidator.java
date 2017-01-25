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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validates value exists in a set
 */
@Validator(name = "Lookup", description = "Must be contained in the list")
public class LookupValidator implements ValidationPolicy<String> {

    private static final Logger log = LoggerFactory.getLogger(LookupValidator.class);

    @PolicyProperty(name = "List", hint = "Comma separated list of values", required = true)
    private String lookupList;

    private Set<String> lookupValues = new HashSet<>();

    public LookupValidator(String... values) {
        lookupValues = new HashSet<>(Arrays.asList(values));
    }

    public LookupValidator(@PolicyPropertyRef(name = "List") String values) {
        log.info("Lookup Validator for {} ", values);
        this.lookupList = values;
        String[] arr = StringUtils.split(values, ",");
        if (arr != null) {
            lookupValues = new HashSet<>(Arrays.asList(arr));
        } else {
            log.error("Lookup Validator error NULL array ");
        }
    }


    @Override
    public boolean validate(String value) {
        return lookupValues.contains(value);
    }

    public String getLookupList() {
        return lookupList;
    }

    public Set<String> getLookupValues() {
        return lookupValues;
    }
}

