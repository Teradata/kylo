/*
 * Copyright (c) 2016. Teradata Inc.
 */

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
 * Masks all digits except the last four
 */
@Standardizer(name = "Mask Credit Card", description = "Preserves last 4 digits")
public class MaskLeavingLastFourDigitStandardizer extends SimpleRegexReplacer implements StandardizationPolicy {

    private static final MaskLeavingLastFourDigitStandardizer instance = new MaskLeavingLastFourDigitStandardizer();

    private MaskLeavingLastFourDigitStandardizer() {
        super("\\d", "X");
    }

    public static MaskLeavingLastFourDigitStandardizer instance() {
        return instance;
    }

    @Override
    public String convertValue(String value) {
        if (value.length() > 4) {
            return super.convertValue(value.substring(0, value.length() - 4)) + value.substring(value.length() - 4);
        }
        return value;
    }

}
