/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark.standardization.impl;

import com.thinkbiganalytics.spark.standardization.StandardizationPolicy;

/**
 * Upper case
 */
public class UppercaseStandardizer implements StandardizationPolicy {

    private static final UppercaseStandardizer instance = new UppercaseStandardizer();

    private UppercaseStandardizer() {
        super();
    }

    @Override
    public String convertValue(String value) {
        return value.toUpperCase();
    }

    public static UppercaseStandardizer instance() {
        return instance;
    }
}
