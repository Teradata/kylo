/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.spark.validation.impl;

import com.thinkbiganalytics.spark.validation.Validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexValidator implements Validator<String> {

    private Pattern pattern;
    private boolean valid;

    public RegexValidator(String regex) {
        try {
            this.pattern = Pattern.compile(regex);
            valid = true;
        } catch (PatternSyntaxException e) {
            System.out.println("Invalid regex [" + e + "]. All values will be valid.");
        }
    }

    @Override
    public boolean validate(String value) {
        if (!valid) return true;
        Matcher matcher = pattern.matcher(value);
        return (matcher.matches());
    }
}
