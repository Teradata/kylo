/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark.validation.impl;

import com.thinkbiganalytics.spark.validation.Validator;

public class RangeValidator implements Validator<Number> {

    private Number min;
    private Number max;

    public RangeValidator(Number min, Number max) {
        super();
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean validate(Number value) {
        return true;
    }
}
