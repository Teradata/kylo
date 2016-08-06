/*
 * Copyright (c) 2016. Teradata Inc.
 */

package com.thinkbiganalytics.policy.validation;


/**
 * Validates email address
 */
@Validator(name = "Email", description = "Valid email address")
public class EmailValidator extends RegexValidator implements ValidationPolicy<String> {

    private static final EmailValidator instance = new EmailValidator();

    private EmailValidator() {
        super(
            "^([\\w\\d\\-\\.]+)@{1}(([\\w\\d\\-]{1,67})|([\\w\\d\\-]+\\.[\\w\\d\\-]{1,67}))\\.(([a-zA-Z\\d]{2,4})(\\.[a-zA-Z\\d]{2})?)$");
    }

    public static EmailValidator instance() {
        return instance;
    }

}
