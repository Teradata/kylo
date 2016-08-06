/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.standardization;


/**
 * Upper case
 */
@Standardizer(name = "Uppercase", description = "Convert string to uppercase")
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
