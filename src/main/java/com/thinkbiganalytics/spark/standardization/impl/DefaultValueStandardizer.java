/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark.standardization.impl;

import com.thinkbiganalytics.spark.standardization.StandardizationPolicy;
import org.apache.commons.lang3.StringUtils;

/**
 * Applies a default value in place of null or empty
 */
public class DefaultValueStandardizer implements StandardizationPolicy, AcceptsEmptyValues {

    private String defaultStr;

    public DefaultValueStandardizer(String defaultStr) {
        this.defaultStr = defaultStr;
    }

    @Override
    public String convertValue(String value) {
        return StringUtils.defaultString(value, defaultStr);
    }
}
