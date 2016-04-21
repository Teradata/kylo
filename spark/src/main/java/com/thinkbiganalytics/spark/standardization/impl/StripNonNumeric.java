/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark.standardization.impl;

/**
 * Strips any non-digit value other than decimal
 */
public class StripNonNumeric extends SimpleRegexReplacer {

    private static final StripNonNumeric instance = new StripNonNumeric();

    private StripNonNumeric() {
        super("[^\\d.]", "");
    }

    @Override
    public String convertValue(String value) {
        return super.convertValue(value);
    }

    public static StripNonNumeric instance() {
        return instance;
    }
}
