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


/**
 * Strips any non-digit value other than decimal and signs
 */
@Standardizer(name = "Strip Non Numeric", description = "Remove any characters that are not numeric")
public class StripNonNumeric extends SimpleRegexReplacer {

    private static final StripNonNumeric instance = new StripNonNumeric();

    private StripNonNumeric() {
        super("[^-+\\d.]", "");
    }

    public static StripNonNumeric instance() {
        return instance;
    }

    @Override
    public String convertValue(String value) {
        return super.convertValue(value);
    }
}
