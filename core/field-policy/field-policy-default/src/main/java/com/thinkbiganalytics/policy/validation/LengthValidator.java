/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.validation;


import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;

import org.apache.commons.lang3.Validate;

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
