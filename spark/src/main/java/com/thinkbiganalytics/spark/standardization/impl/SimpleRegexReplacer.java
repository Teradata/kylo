/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark.standardization.impl;

import com.thinkbiganalytics.spark.standardization.StandardizationPolicy;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Replace regex pattern in text with replacement
 */
public class SimpleRegexReplacer implements StandardizationPolicy {

    private Pattern pattern;
    private String replacement;
    boolean valid;

    public SimpleRegexReplacer(String regex, String replace) {
        try {
            this.pattern = Pattern.compile(regex);
            this.replacement = replace;
            valid = true;
        } catch (PatternSyntaxException e) {
            System.out.println("Invalid regex [" + e + "].No substitution will be performed.");
        }
    }

    @Override
    public String convertValue(String value) {
        if (!valid) return value;
        return pattern.matcher(value).replaceAll(replacement);
    }
}
