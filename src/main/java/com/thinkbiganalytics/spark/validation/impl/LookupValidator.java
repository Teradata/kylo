/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark.validation.impl;

import com.thinkbiganalytics.spark.validation.Validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validates value exists in a set
 */
public class LookupValidator implements Validator<String> {
    private Set<String> lookupValues = new HashSet<>();

    public LookupValidator(String... values) {
        lookupValues = new HashSet<>(Arrays.asList(values));
    }

    @Override
    public boolean validate(String value) {
        return lookupValues.contains(value);
    }
}

