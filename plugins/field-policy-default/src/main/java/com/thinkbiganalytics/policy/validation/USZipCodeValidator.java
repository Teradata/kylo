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


/**
 * Validates US zip codes
 */
@Validator(name = "US Zip", description = "Validate US Zip")
public class USZipCodeValidator extends RegexValidator implements ValidationPolicy<String> {

    private static final USZipCodeValidator instance = new USZipCodeValidator();

    private USZipCodeValidator() {
        super("[0-9]{5}([- /]?[0-9]{4})?$");
    }

    public static USZipCodeValidator instance() {
        return instance;
    }

}
