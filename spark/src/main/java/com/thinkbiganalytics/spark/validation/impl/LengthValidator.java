/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark.validation.impl;

import com.thinkbiganalytics.spark.validation.Validator;
import org.apache.commons.lang.Validate;

public class LengthValidator implements Validator<String> {

    private int maxLength;
    private int minLength;

    public LengthValidator(int min, int max) {
        Validate.isTrue(min >=0, "Minimum must be > 0");
        this.minLength = min;
        this.maxLength = max;
    }

    @Override
    public boolean validate(String value) {
        int len = value.length();
        return (len <= maxLength && len >= minLength);
    }
}
